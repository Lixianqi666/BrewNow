<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { recommendApi, type RecommendationItem } from '@/api/recommend'
import { getReliableRecommendationReason } from '@/utils/recommendationReason'

const props = withDefaults(defineProps<{
  title?: string
  limit?: number
  pageSize?: number
  reasonFallback?: string
}>(), {
  title: '猜你喜欢',
  limit: 12,
  pageSize: 4,
  reasonFallback: ''
})

const emit = defineEmits<{
  select: [productId: number]
  addCart: [product: RecommendationItem['product']]
  buyNow: [product: RecommendationItem['product']]
}>()

const recommendationPool = ref<RecommendationItem[]>([])
const recommendationPage = ref(0)
const refreshing = ref(false)

const visibleItems = computed(() => {
  if (!recommendationPool.value.length) return []
  const start = recommendationPage.value * props.pageSize
  return recommendationPool.value.slice(start, start + props.pageSize)
})

const shuffleProducts = (products: RecommendationItem[]) => {
  const cloned = [...products]
  for (let i = cloned.length - 1; i > 0; i -= 1) {
    const randomIndex = Math.floor(Math.random() * (i + 1))
    ;[cloned[i], cloned[randomIndex]] = [cloned[randomIndex], cloned[i]]
  }
  return cloned
}

const loadRecommendations = async () => {
  try {
    const response = await recommendApi.getHomeRecommendationItems(props.limit)
    const items = (response.data || []).filter((item) => item?.product?.productId)
    recommendationPool.value = shuffleProducts(items)
    recommendationPage.value = 0
  } catch (error) {
    console.error('加载推荐商品失败:', error)
    recommendationPool.value = []
    recommendationPage.value = 0
    ElMessage.warning('推荐商品加载失败')
  }
}

const refreshRecommendations = async () => {
  if (refreshing.value) return
  refreshing.value = true
  try {
    const nextPage = recommendationPage.value + 1
    const maxPage = Math.max(1, Math.ceil(recommendationPool.value.length / props.pageSize))
    if (recommendationPool.value.length >= props.pageSize * 2 && nextPage < maxPage) {
      recommendationPage.value = nextPage
      return
    }
    if (recommendationPool.value.length > props.pageSize) {
      recommendationPool.value = shuffleProducts(recommendationPool.value)
      recommendationPage.value = 0
      return
    }
    await loadRecommendations()
  } finally {
    refreshing.value = false
  }
}

const getReasonText = (item: RecommendationItem) => {
  return getReliableRecommendationReason(item, props.reasonFallback)
}

onMounted(loadRecommendations)

defineExpose({
  refreshRecommendations,
  loadRecommendations
})
</script>

<template>
  <div v-if="visibleItems.length" class="recommend-shelf">
    <div class="section-header">
      <h2>{{ title }}</h2>
      <BaseButton
        text
        size="small"
        :loading="refreshing"
        class="refresh-recommend"
        @click="refreshRecommendations"
      >
        <el-icon><RefreshRight /></el-icon>
        换一批
      </BaseButton>
    </div>

    <el-row :gutter="20" class="recommend-grid">
      <el-col
        v-for="item in visibleItems"
        :key="item.product.productId"
        :xs="24"
        :sm="12"
        :md="6"
        class="recommend-col"
      >
        <ProductCard
          :product="item.product"
          :description="getReasonText(item)"
          :meta-tag="item.product.category"
          :meta-text="item.product.originPlace || item.product.brand || ''"
          @select="emit('select', item.product.productId!)"
          @addCart="emit('addCart', $event)"
          @buyNow="emit('buyNow', $event)"
        />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.recommend-shelf {
  margin-top: 30px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  color: #173a2f;
}

.refresh-recommend {
  color: #55715f;
}

.recommend-grid {
  align-items: stretch;
}

.recommend-col {
  display: flex;
  margin-bottom: 20px;
}

.recommend-col :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
}
</style>
