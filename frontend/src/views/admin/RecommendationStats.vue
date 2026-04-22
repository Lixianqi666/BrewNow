<template>
  <div class="recommendation-stats">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <div>
            <h2>推荐与行为统计</h2>
          </div>
          <BaseButton plain :loading="loading" @click="loadStats">刷新数据</BaseButton>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="8" animated />
      <el-empty v-else-if="!stats" description="暂无推荐统计数据，请确认后端服务已启动" />
      <div v-else class="stats-content">
        <el-row :gutter="16" class="summary-grid">
          <el-col v-for="item in summaryCards" :key="item.label" :xs="12" :md="8" :xl="4">
            <div class="metric-card">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="section-grid">
          <el-col :xs="24" :lg="10">
            <el-card shadow="never" class="sub-card">
              <template #header>行为分布</template>
              <div class="behavior-list">
                <div v-for="entry in behaviorEntries" :key="entry.label" class="behavior-item">
                  <span>{{ entry.label }}</span>
                  <strong>{{ entry.value }}</strong>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="14">
            <el-card shadow="never" class="sub-card">
              <template #header>推荐效果评估（Top {{ stats.evaluation.topK }}）</template>
              <el-table :data="evaluationRows" border>
                <el-table-column prop="name" label="方案" />
                <el-table-column prop="precision" label="Precision@K" />
                <el-table-column prop="recall" label="Recall@K" />
                <el-table-column prop="hitRate" label="HitRate@K" />
                <el-table-column prop="users" label="评估用户数" />
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="sub-card recent-card">
          <template #header>近期行为样本</template>
          <el-table :data="stats.recentBehaviors" border>
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="productId" label="商品ID" width="100" />
            <el-table-column prop="behaviorType" label="行为类型" width="120" />
            <el-table-column prop="behaviorWeight" label="权重" width="100" />
            <el-table-column prop="createdAt" label="时间" min-width="180" />
          </el-table>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { recommendApi, type RecommendationStats } from '@/api/recommend'

const loading = ref(false)
const stats = ref<RecommendationStats | null>(null)

const loadStats = async () => {
  loading.value = true
  try {
    const response = await recommendApi.getRecommendationStats(10)
    stats.value = (response.data as RecommendationStats) ?? null
  } catch (error) {
    console.error('加载推荐统计失败:', error)
  } finally {
    loading.value = false
  }
}

const summaryCards = computed(() => {
  if (!stats.value) return []
  return [
    { label: '行为总数', value: stats.value.totalBehaviors },
    { label: '活跃用户', value: stats.value.activeUsers },
    { label: '可推荐用户', value: stats.value.recommendableUsers },
    { label: '在售茶品', value: stats.value.activeProducts },
    { label: '收藏总量', value: stats.value.totalFavorites },
    { label: '时间衰减 λ', value: stats.value.evaluation.lambda },
    { label: '当前季节', value: stats.value.evaluation.season }
  ]
})

const behaviorEntries = computed(() => {
  if (!stats.value) return []
  return Object.entries(stats.value.behaviorTypeCounts || {}).map(([label, value]) => ({ label, value }))
})

const evaluationRows = computed(() => {
  if (!stats.value) return []
  return [
    {
      name: '基线协同过滤',
      precision: stats.value.evaluation.baseline.precisionAtK,
      recall: stats.value.evaluation.baseline.recallAtK,
      hitRate: stats.value.evaluation.baseline.hitRateAtK,
      users: stats.value.evaluation.baseline.evaluatedUsers
    },
    {
      name: '时间衰减协同过滤',
      precision: stats.value.evaluation.timeDecay.precisionAtK,
      recall: stats.value.evaluation.timeDecay.recallAtK,
      hitRate: stats.value.evaluation.timeDecay.hitRateAtK,
      users: stats.value.evaluation.timeDecay.evaluatedUsers
    },
    {
      name: '季节增强协同过滤',
      precision: stats.value.evaluation.seasonAware.precisionAtK,
      recall: stats.value.evaluation.seasonAware.recallAtK,
      hitRate: stats.value.evaluation.seasonAware.hitRateAtK,
      users: stats.value.evaluation.seasonAware.evaluatedUsers
    }
  ]
})

onMounted(loadStats)
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
  justify-content: space-between;
  gap: 16px;
}

.card-header h2,
.card-header p {
  margin: 0;
}

.card-header p {
  margin-top: 6px;
  color: #7a6a54;
}

.summary-grid,
.section-grid {
  margin-bottom: 16px;
}

.sub-card {
  border-radius: 16px;
}

.behavior-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.behavior-item {
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 12px;
  background: #faf6ee;
}

.recent-card {
  margin-top: 16px;
}
</style>
