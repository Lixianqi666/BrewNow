<template>
  <div class="recommendation-stats">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <h2>推荐与行为统计</h2>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />
      <el-empty v-else-if="!stats" description="暂无推荐统计数据，请确认后端服务已启动" />
      <div v-else class="stats-content">
        <el-card shadow="never" class="sub-card control-card">
          <template #header>
            <div class="sub-header">分析控制区</div>
          </template>
          <div class="controls-wrap">
            <div class="control-item">
              <span class="control-label">TopK</span>
              <el-select v-model="selectedTopK" class="control-input" @change="loadStats">
                <el-option v-for="option in topKOptions" :key="option" :label="`Top ${option}`" :value="option" />
              </el-select>
            </div>
            <div class="control-item">
              <span class="control-label">行为过滤</span>
              <el-select v-model="behaviorFilter" class="control-input">
                <el-option v-for="option in behaviorFilterOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </div>
            <div class="control-item search-item">
              <span class="control-label">关键词</span>
              <el-input
                v-model.trim="searchKeyword"
                class="control-input"
                clearable
                placeholder="按用户ID或商品ID过滤"
              />
            </div>
            <div class="control-actions">
              <BaseButton plain :loading="loading" @click="loadStats">刷新数据</BaseButton>
              <BaseButton @click="exportCsv">导出 CSV</BaseButton>
            </div>
          </div>
        </el-card>

        <el-row :gutter="16" class="summary-grid">
          <el-col v-for="item in summaryCards" :key="item.label" :xs="12" :md="8" :xl="4">
            <div class="metric-card">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="summary-grid">
          <el-col v-for="item in diagnosticCards" :key="item.label" :xs="24" :sm="12" :xl="6">
            <div class="metric-card diagnostic-card">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="section-grid">
          <el-col :xs="24" :lg="12">
            <el-card shadow="never" class="sub-card">
              <template #header>行为分布环形图</template>
              <div ref="behaviorPieChartRef" class="chart-box" />
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card shadow="never" class="sub-card">
              <template #header>策略对比柱状图（Top {{ stats.evaluation.topK }}）</template>
              <div ref="strategyBarChartRef" class="chart-box" />
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="section-grid">
          <el-col :xs="24">
            <el-card shadow="never" class="sub-card">
              <template #header>推荐效果评估（Top {{ stats.evaluation.topK }}）</template>
              <el-table :data="evaluationRows" border>
                <el-table-column prop="name" label="方案" />
                <el-table-column prop="precision" label="Precision@K" width="140" />
                <el-table-column prop="recall" label="Recall@K" width="140" />
                <el-table-column prop="hitRate" label="HitRate@K" width="140" />
                <el-table-column prop="users" label="评估用户数" width="120" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="sub-card recent-card">
          <template #header>
            <div class="sub-header">
              <span>近期行为样本</span>
              <span class="sub-meta">过滤后 {{ filteredBehaviors.length }} 条</span>
            </div>
          </template>
          <el-table :data="paginatedBehaviors" border>
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="productId" label="商品ID" width="100" />
            <el-table-column prop="behaviorType" label="行为类型" width="120" />
            <el-table-column prop="behaviorWeight" label="权重" width="100" />
            <el-table-column prop="createdAt" label="时间" min-width="180" />
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="filteredBehaviors.length"
              layout="prev, pager, next"
            />
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { recommendApi, type RecommendationMetrics, type RecommendationStats } from '@/api/recommend'

type BehaviorFilter = 'ALL' | 'VIEW' | 'FAVORITE' | 'CART' | 'PURCHASE'

interface StrategyMetricRow {
  name: string
  precision: number
  recall: number
  hitRate: number
  users: number
}

const topKOptions = [5, 10, 20]
const behaviorFilterOptions: Array<{ label: string; value: BehaviorFilter }> = [
  { label: '全部', value: 'ALL' },
  { label: '浏览', value: 'VIEW' },
  { label: '收藏', value: 'FAVORITE' },
  { label: '加购', value: 'CART' },
  { label: '购买', value: 'PURCHASE' }
]
const pageSize = 8

const loading = ref(false)
const stats = ref<RecommendationStats | null>(null)
const selectedTopK = ref(10)
const behaviorFilter = ref<BehaviorFilter>('ALL')
const searchKeyword = ref('')
const currentPage = ref(1)

const behaviorPieChartRef = ref<HTMLElement | null>(null)
const strategyBarChartRef = ref<HTMLElement | null>(null)
let behaviorPieChart: echarts.ECharts | null = null
let strategyBarChart: echarts.ECharts | null = null

const formatDecimal = (value: number, digits = 4) => Number(value || 0).toFixed(digits)
const formatInteger = (value: number) => Number(value || 0).toLocaleString('zh-CN')
const formatPercent = (numerator: number, denominator: number) => {
  if (!denominator) return '0.0000%'
  return `${((numerator / denominator) * 100).toFixed(4)}%`
}

const getBehaviorCount = (type: Exclude<BehaviorFilter, 'ALL'>) => {
  if (!stats.value) return 0
  return Number(stats.value.behaviorTypeCounts?.[type] || 0)
}

const convertMetricRow = (name: string, metrics: RecommendationMetrics): StrategyMetricRow => ({
  name,
  precision: Number(metrics.precisionAtK || 0),
  recall: Number(metrics.recallAtK || 0),
  hitRate: Number(metrics.hitRateAtK || 0),
  users: Number(metrics.evaluatedUsers || 0)
})

const strategyRows = computed<StrategyMetricRow[]>(() => {
  if (!stats.value) return []
  return [
    convertMetricRow('Baseline', stats.value.evaluation.baseline),
    convertMetricRow('TimeDecay', stats.value.evaluation.timeDecay),
    convertMetricRow('SeasonAware', stats.value.evaluation.seasonAware)
  ]
})

const bestStrategy = computed(() => {
  if (!strategyRows.value.length) return '--'
  const best = strategyRows.value.reduce((previous, current) => {
    return current.hitRate > previous.hitRate ? current : previous
  })
  return `${best.name} (${formatDecimal(best.hitRate)})`
})

const loadStats = async () => {
  loading.value = true
  try {
    const response = await recommendApi.getRecommendationStats(selectedTopK.value)
    stats.value = (response.data as RecommendationStats) ?? null
    currentPage.value = 1
    await nextTick()
    renderCharts()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载推荐统计失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const summaryCards = computed(() => {
  if (!stats.value) return []
  return [
    { label: '行为总数', value: formatInteger(stats.value.totalBehaviors) },
    { label: '活跃用户', value: formatInteger(stats.value.activeUsers) },
    { label: '可推荐用户', value: formatInteger(stats.value.recommendableUsers) },
    { label: '在售茶品', value: formatInteger(stats.value.activeProducts) },
    { label: '收藏总量', value: formatInteger(stats.value.totalFavorites) },
    { label: '时间衰减 λ', value: formatDecimal(stats.value.evaluation.lambda) },
    { label: '当前季节', value: stats.value.evaluation.season }
  ]
})

const diagnosticCards = computed(() => {
  const viewCount = getBehaviorCount('VIEW')
  const cartCount = getBehaviorCount('CART')
  const purchaseCount = getBehaviorCount('PURCHASE')
  return [
    { label: '浏览 -> 加购转化率', value: formatPercent(cartCount, viewCount) },
    { label: '浏览 -> 购买转化率', value: formatPercent(purchaseCount, viewCount) },
    { label: '加购 -> 购买转化率', value: formatPercent(purchaseCount, cartCount) },
    { label: '最优策略', value: bestStrategy.value }
  ]
})

const evaluationRows = computed(() => {
  return strategyRows.value.map((item) => ({
    name: item.name,
    precision: formatDecimal(item.precision),
    recall: formatDecimal(item.recall),
    hitRate: formatDecimal(item.hitRate),
    users: formatInteger(item.users)
  }))
})

const filteredBehaviors = computed(() => {
  const sourceRows = stats.value?.recentBehaviors || []
  const keyword = searchKeyword.value.trim()
  return sourceRows.filter((row) => {
    const typeMatch = behaviorFilter.value === 'ALL' || row.behaviorType === behaviorFilter.value
    const keywordMatch =
      !keyword || String(row.userId).includes(keyword) || String(row.productId).includes(keyword)
    return typeMatch && keywordMatch
  })
})

const paginatedBehaviors = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredBehaviors.value.slice(start, start + pageSize)
})

const renderBehaviorPieChart = () => {
  if (!behaviorPieChartRef.value) return
  if (!behaviorPieChart) {
    behaviorPieChart = echarts.init(behaviorPieChartRef.value)
  }

  const behaviorData = [
    { name: 'VIEW', value: getBehaviorCount('VIEW') },
    { name: 'FAVORITE', value: getBehaviorCount('FAVORITE') },
    { name: 'CART', value: getBehaviorCount('CART') },
    { name: 'PURCHASE', value: getBehaviorCount('PURCHASE') }
  ]

  behaviorPieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (params: { name: string; value: number; percent: number }) => {
        return `${params.name}<br/>数量: ${formatInteger(params.value)}<br/>占比: ${params.percent.toFixed(2)}%`
      }
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'middle'
    },
    series: [
      {
        type: 'pie',
        radius: ['42%', '72%'],
        center: ['38%', '50%'],
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          formatter: '{b}: {d}%'
        },
        data: behaviorData,
        color: ['#6b8f3e', '#4f7d62', '#d08c26', '#ad4f19']
      }
    ]
  })
}

const renderStrategyBarChart = () => {
  if (!strategyBarChartRef.value) return
  if (!strategyBarChart) {
    strategyBarChart = echarts.init(strategyBarChartRef.value)
  }

  strategyBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      top: 0,
      data: ['Precision@K', 'Recall@K', 'HitRate@K']
    },
    grid: {
      left: 38,
      right: 20,
      top: 48,
      bottom: 24,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: strategyRows.value.map((item) => item.name)
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value: number) => Number(value).toFixed(2)
      }
    },
    series: [
      {
        name: 'Precision@K',
        type: 'bar',
        barMaxWidth: 36,
        data: strategyRows.value.map((item) => item.precision),
        itemStyle: { color: '#8c7b5b' }
      },
      {
        name: 'Recall@K',
        type: 'bar',
        barMaxWidth: 36,
        data: strategyRows.value.map((item) => item.recall),
        itemStyle: { color: '#6b8f3e' }
      },
      {
        name: 'HitRate@K',
        type: 'bar',
        barMaxWidth: 36,
        data: strategyRows.value.map((item) => item.hitRate),
        itemStyle: { color: '#d08c26' }
      }
    ]
  })
}

const renderCharts = () => {
  renderBehaviorPieChart()
  renderStrategyBarChart()
}

const csvEscape = (value: string | number) => {
  const raw = String(value)
  if (/[",\n]/.test(raw)) {
    return `"${raw.replace(/"/g, '""')}"`
  }
  return raw
}

const exportCsv = () => {
  if (!stats.value) {
    ElMessage.warning('暂无可导出的数据')
    return
  }

  const lines: string[][] = []
  lines.push(['关键统计'])
  summaryCards.value.forEach((item) => lines.push([item.label, item.value]))

  lines.push([])
  lines.push(['关键诊断指标'])
  diagnosticCards.value.forEach((item) => lines.push([item.label, item.value]))

  lines.push([])
  lines.push(['策略评估', `TopK=${stats.value.evaluation.topK}`])
  lines.push(['方案', 'Precision@K', 'Recall@K', 'HitRate@K', '评估用户数'])
  strategyRows.value.forEach((item) => {
    lines.push([
      item.name,
      formatDecimal(item.precision),
      formatDecimal(item.recall),
      formatDecimal(item.hitRate),
      formatInteger(item.users)
    ])
  })

  lines.push([])
  lines.push(['近期行为样本（过滤后）', `共${filteredBehaviors.value.length}条`])
  lines.push(['ID', '用户ID', '商品ID', '行为类型', '权重', '时间'])
  filteredBehaviors.value.forEach((item) => {
    lines.push([
      String(item.id),
      String(item.userId),
      String(item.productId),
      item.behaviorType,
      formatDecimal(item.behaviorWeight),
      item.createdAt
    ])
  })

  const csvContent = lines.map((line) => line.map(csvEscape).join(',')).join('\n')
  const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  const date = new Date().toISOString().slice(0, 10)
  anchor.href = url
  anchor.download = `recommendation-stats-top${selectedTopK.value}-${date}.csv`
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
  URL.revokeObjectURL(url)
  ElMessage.success('CSV 导出成功')
}

const handleResize = () => {
  behaviorPieChart?.resize()
  strategyBarChart?.resize()
}

watch([behaviorFilter, searchKeyword], () => {
  currentPage.value = 1
})

onMounted(async () => {
  await loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  behaviorPieChart?.dispose()
  strategyBarChart?.dispose()
  behaviorPieChart = null
  strategyBarChart = null
})
</script>

<style scoped>
.recommendation-stats {
  padding: 20px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
}

.card-header h2 {
  margin: 0;
}

.control-card {
  margin-bottom: 16px;
}

.controls-wrap {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 12px;
}

.control-item {
  grid-column: span 3;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.search-item {
  grid-column: span 4;
}

.control-label {
  font-size: 12px;
  color: #7a6a54;
}

.control-input {
  width: 100%;
}

.control-actions {
  grid-column: span 2;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  justify-content: flex-end;
}

.summary-grid,
.section-grid {
  margin-bottom: 16px;
}

.sub-card {
  border-radius: 16px;
}

.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.sub-meta {
  font-size: 12px;
  color: #7a6a54;
}

.recent-card {
  margin-top: 16px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 92px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #faf6ee;
}

.metric-card span {
  color: #7a6a54;
  font-size: 13px;
}

.metric-card strong {
  color: #3e2f1f;
  font-size: 22px;
  line-height: 1.2;
  word-break: break-word;
}

.diagnostic-card {
  background: #f4efe4;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .control-item {
    grid-column: span 4;
  }

  .search-item {
    grid-column: span 6;
  }

  .control-actions {
    grid-column: span 12;
    justify-content: flex-end;
  }
}

@media (max-width: 768px) {
  .recommendation-stats {
    padding: 12px;
  }

  .control-item,
  .search-item,
  .control-actions {
    grid-column: span 12;
  }

  .control-actions {
    justify-content: flex-start;
  }

  .metric-card {
    min-height: 80px;
    padding: 12px;
  }

  .metric-card strong {
    font-size: 18px;
  }

  .chart-box {
    height: 260px;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
