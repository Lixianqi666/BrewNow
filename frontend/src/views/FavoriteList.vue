<template>
  <div class="favorite-page">
    <div class="favorite-header">
      <div>
        <h1>我的收藏</h1>
        <p>集中查看你感兴趣的茶品，便于后续对比与下单。</p>
      </div>
      <el-button plain @click="loadFavorites" :loading="loading">刷新列表</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="!favoriteProducts.length" description="暂时还没有收藏的茶品" />

    <el-row v-else :gutter="20">
      <el-col v-for="product in favoriteProducts" :key="product.productId" :xs="24" :sm="12" :lg="6" class="product-col">
        <ProductCard
          :product="product"
          :description="product.flavorProfile || product.teaTags || product.description || '精选茶品，值得细品。'"
          :meta-tag="product.category"
          :meta-text="product.originPlace || product.brand || ''"
          @select="goDetail(product.productId!)"
          @addCart="addToCart"
          @buyNow="buyNow"
        >
          <template #meta-action>
            <el-button text type="danger" class="meta-action" @click.stop="toggleFavorite(product.productId!)">取消收藏</el-button>
          </template>
        </ProductCard>
      </el-col>
    </el-row>
    <div v-if="recommendItems.length" class="recommend-section">
      <div class="section-header">
        <h2>精选商品</h2>
        <el-button text size="small" class="refresh-recommend" @click="refreshRecommend">换一批</el-button>
      </div>
      <el-row :gutter="20">
        <el-col v-for="item in recommendItems" :key="item.product.productId" :xs="24" :sm="12" :lg="6" class="product-col">
          <ProductCard
            :product="item.product"
            :description="getReasonText(item)"
            :meta-tag="item.product.category"
            :meta-text="item.product.originPlace || item.product.brand || ''"
            @select="goDetail(item.product.productId!)"
            @addCart="addToCart"
            @buyNow="buyNow"
          />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { favoriteApi } from '@/api/favorite'
import { recommendApi, type RecommendationItem } from '@/api/recommend'
import type { Product } from '@/api/product'
import { resolveProductImageUrl } from '@/utils/productImageResolver'
import { getReliableRecommendationReason } from '@/utils/recommendationReason'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const loading = ref(false)
const favoriteProducts = ref<Product[]>([])
const recommendItems = ref<RecommendationItem[]>([])

const loadFavorites = async () => {
  loading.value = true
  try {
    const response = await favoriteApi.getFavorites()
    favoriteProducts.value = (response.data as Product[]) || []
    await refreshRecommend()
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    try {
      const fallback = await recommendApi.getHomeRecommendations(6)
      recommendItems.value = (fallback.data || []).map((product) => ({
        product,
        score: 0,
        strategy: 'POPULAR',
        reason: ''
      }))
    } catch (fallbackError) {
      console.error('加载推荐商品失败:', fallbackError)
      recommendItems.value = []
    }
  } finally {
    loading.value = false
  }
}

const refreshRecommend = async () => {
  try {
    const recommendResp = await recommendApi.getHomeRecommendationItems(6)
    recommendItems.value = (recommendResp.data as RecommendationItem[]) || []
  } catch (error) {
    console.error('加载推荐商品失败:', error)
    recommendItems.value = []
  }
}

const goDetail = (productId: number) => {
  router.push(`/product/${productId}`)
}

const toggleFavorite = async (productId: number) => {
  try {
    const response = await favoriteApi.toggleFavorite(productId)
    if (!response.data?.favorited) {
      favoriteProducts.value = favoriteProducts.value.filter(item => item.productId !== productId)
      ElMessage.success('已取消收藏')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
  }
}

const getReasonText = (item: RecommendationItem) => {
  return getReliableRecommendationReason(item)
}

const addToCart = async (product: Product) => {
  if (!product.productId) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('加入购物车需先登录消费者账号')
    router.push('/login')
    return
  }
  if (!userStore.isConsumer) {
    ElMessage.warning('仅消费者账号可加入购物车')
    return
  }
  try {
    await cartStore.addToCart(product.productId, 1)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error('添加到购物车失败:', error)
    ElMessage.error('添加到购物车失败')
  }
}

const buyNow = (product: Product) => {
  if (!product.productId) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('立即购买需先登录消费者账号')
    router.push('/login')
    return
  }
  if (!userStore.isConsumer) {
    ElMessage.warning('仅消费者账号可立即购买')
    return
  }
  if (product.stockQuantity === 0) {
    ElMessage.error('商品库存不足')
    return
  }
  const instantCheckoutItem = {
    productId: product.productId,
    productName: product.productName,
    price: product.price,
    quantity: 1,
    imageUrl: resolveProductImageUrl(product.productName, product.imageUrl),
    category: product.category,
    brand: product.brand
  }

  sessionStorage.setItem('buyNowItem', JSON.stringify(instantCheckoutItem))
  sessionStorage.setItem('checkoutType', 'buyNow')
  router.push('/checkout')
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorite-page {
  min-height: calc(100vh - 68px);
  padding: 28px;
  background: transparent;
}

.favorite-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.favorite-header h1 {
  margin: 0 0 8px;
}

.favorite-header p {
  margin: 0;
  color: #6d614f;
}

.product-col {
  display: flex;
  margin-bottom: 20px;
}

.meta-action {
  padding: 0;
}

.recommend-section {
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

@media (max-width: 768px) {
  .favorite-page {
    padding: 18px;
  }

  .favorite-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
