<script setup lang="ts">
import { computed, ref, onMounted, markRaw } from 'vue'
import { ShoppingCart, Goblet, Promotion, Cherry, Mug, RefreshRight } from '@element-plus/icons-vue'
import { recommendApi } from '@/api/recommend'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import type { Product } from '@/api/product'
import type { RecommendationItem } from '@/api/recommend'
import { resolveProductImageUrl } from '@/utils/productImageResolver'
import { getReliableRecommendationReason } from '@/utils/recommendationReason'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const categories = [
  { id: 1, label: '绿茶专区', value: '绿茶', description: '清鲜甘爽，日常轻饮', icon: markRaw(Goblet) },
  { id: 2, label: '红茶专区', value: '红茶', description: '醇厚暖甜，午后首选', icon: markRaw(Promotion) },
  { id: 3, label: '乌龙专区', value: '乌龙茶', description: '花果香气，层次饱满', icon: markRaw(Cherry) },
  { id: 4, label: '花茶专区', value: '花茶', description: '轻柔芬芳，适配轻养生', icon: markRaw(Mug) }
]

const recommendationPool = ref<RecommendationItem[]>([])
const recommendationPage = ref(0)
const refreshing = ref(false)
const recommendationPageSize = 4

const recommendedItems = computed(() => {
  if (!recommendationPool.value.length) return []
  const start = recommendationPage.value * recommendationPageSize
  return recommendationPool.value.slice(start, start + recommendationPageSize)
})

const shuffleProducts = (products: RecommendationItem[]) => {
  const cloned = [...products]
  for (let i = cloned.length - 1; i > 0; i -= 1) {
    const randomIndex = Math.floor(Math.random() * (i + 1))
    ;[cloned[i], cloned[randomIndex]] = [cloned[randomIndex], cloned[i]]
  }
  return cloned
}

const getReasonText = (item: RecommendationItem) => {
  return getReliableRecommendationReason(item)
}

const loadRecommendations = async () => {
  try {
    const response = await recommendApi.getHomeRecommendationItems(12)
    const items = (response.data || []).filter((item) => item?.product?.productId)
    recommendationPool.value = shuffleProducts(items)
    recommendationPage.value = 0
  } catch {
    ElMessage.warning('推荐商品加载失败')
    recommendationPool.value = []
    recommendationPage.value = 0
  }
}

const refreshRecommendations = async () => {
  if (refreshing.value) return
  refreshing.value = true
  try {
    const nextPage = recommendationPage.value + 1
    const maxPage = Math.max(1, Math.ceil(recommendationPool.value.length / recommendationPageSize))
    if (recommendationPool.value.length >= recommendationPageSize * 2 && nextPage < maxPage) {
      recommendationPage.value = nextPage
    } else if (recommendationPool.value.length > recommendationPageSize) {
      recommendationPool.value = shuffleProducts(recommendationPool.value)
      recommendationPage.value = 0
    } else {
      await loadRecommendations()
    }
  } finally {
    refreshing.value = false
  }
}

const viewCategory = (category: { label: string; value: string }) => {
  ElMessage.success(`进入${category.label}`)
  router.push({ path: '/products', query: { category: category.value } })
}

const exploreProducts = () => {
  router.push('/products')
}

const viewProductDetail = (productId: number) => {
  router.push(`/product/${productId}`)
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

onMounted(async () => {
  await loadRecommendations()
})
</script>

<template>
  <div class="home">
    <div class="page-container">
      <el-card class="hero-card">
        <div class="hero-art" aria-hidden="true">
          <div class="paper-grain"></div>
          <div class="ink-wash"></div>
          <div class="tea-ripple"></div>
          <div class="leaf-shadow leaf-a"></div>
          <div class="leaf-shadow leaf-b"></div>
          <div class="seal">沏刻</div>
        </div>
        <div class="hero-content">
          <span class="hero-pill">春茶上新 · 山场直采</span>
          <p class="hero-brand">BrewNow</p>
          <h1>一盏好茶，从源头到茶席</h1>
          <p>甄选龙井、祁红、单丛与花茶，沉稳茶绿色视觉系统，支持消费者、商家与管理端协同体验。</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="exploreProducts">
              <el-icon><ShoppingCart /></el-icon>
              进入茶品商城
            </el-button>
            <el-button plain size="large" @click="router.push('/about')">查看平台介绍</el-button>
          </div>
          <div class="hero-highlights">
            <span>原产地茶叶</span>
            <span>48小时发货</span>
            <span>支持礼盒定制</span>
          </div>
        </div>
      </el-card>

      <el-row :gutter="20" class="category-section">
        <el-col :xs="24" :sm="12" :md="6" v-for="category in categories" :key="category.id">
          <el-card class="category-card" @click="viewCategory(category)">
            <el-icon class="category-icon"><component :is="category.icon" /></el-icon>
            <h3>{{ category.label }}</h3>
            <p>{{ category.description }}</p>
          </el-card>
        </el-col>
      </el-row>

      <div class="hot-products-section">
        <div class="section-header">
          <h2>精选商品</h2>
          <el-button
            text
            size="small"
            :loading="refreshing"
            class="refresh-recommend"
            @click="refreshRecommendations"
          >
            <el-icon><RefreshRight /></el-icon>
            换一批
          </el-button>
        </div>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" class="product-col" v-for="item in recommendedItems" :key="item.product.productId">
            <ProductCard
              :product="item.product"
              :description="getReasonText(item)"
              :meta-tag="item.product.category"
              :meta-text="item.product.originPlace || item.product.brand || ''"
              @select="viewProductDetail(item.product.productId!)"
              @addCart="addToCart"
              @buyNow="buyNow"
            />
          </el-col>
        </el-row>
      </div>

    </div>
  </div>
</template>

<style scoped>
.home {
  width: 100%;
  background: transparent;
}

.page-container {
  width: min(1240px, 92vw);
  margin: 20px auto 28px;
}

.hot-products-section {
  margin-top: 8px;
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

.product-col {
  display: flex;
  margin-bottom: 20px;
}

.hero-card {
  margin-bottom: 24px;
  position: relative;
  isolation: isolate;
  border-radius: 28px;
  overflow: hidden;
  color: #0f2b20;
  background:
    radial-gradient(1200px 520px at 16% 18%, rgba(221, 195, 142, 0.32), rgba(255, 255, 255, 0) 60%),
    radial-gradient(900px 460px at 86% 26%, rgba(71, 120, 92, 0.18), rgba(255, 255, 255, 0) 62%),
    linear-gradient(135deg, #faf5ea 0%, #fdfaf3 38%, #f5edde 100%);
  border: 1px solid rgba(173, 137, 76, 0.22);
  box-shadow:
    0 8px 18px rgba(23, 58, 47, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.hero-art {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.paper-grain {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 12% 22%, rgba(0, 0, 0, 0.025), rgba(0, 0, 0, 0) 48%),
    radial-gradient(circle at 72% 64%, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0) 52%),
    repeating-linear-gradient(
      0deg,
      rgba(255, 255, 255, 0.0) 0px,
      rgba(255, 255, 255, 0.0) 3px,
      rgba(0, 0, 0, 0.01) 4px
    );
  mix-blend-mode: multiply;
  opacity: 0.28;
  filter: blur(0.2px);
}

.ink-wash {
  position: absolute;
  inset: -20%;
  background:
    radial-gradient(closest-side at 18% 38%, rgba(20, 61, 47, 0.08), rgba(20, 61, 47, 0) 70%),
    radial-gradient(closest-side at 72% 32%, rgba(14, 43, 32, 0.06), rgba(14, 43, 32, 0) 72%),
    radial-gradient(closest-side at 60% 76%, rgba(185, 123, 37, 0.08), rgba(185, 123, 37, 0) 74%);
  transform: rotate(-6deg);
  opacity: 0.5;
}

.tea-ripple {
  position: absolute;
  width: min(520px, 52vw);
  height: min(520px, 52vw);
  right: -140px;
  top: -120px;
  border-radius: 999px;
  background:
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.55), rgba(255, 255, 255, 0) 55%),
    conic-gradient(
      from 210deg,
      rgba(46, 98, 79, 0.16),
      rgba(185, 123, 37, 0.14),
      rgba(46, 98, 79, 0.1)
    );
  filter: blur(0.2px);
  opacity: 0.46;
  animation: rippleFloat 10s ease-in-out infinite;
}

.leaf-shadow {
  position: absolute;
  width: 420px;
  height: 280px;
  border-radius: 60% 40% 55% 45%;
  background: radial-gradient(circle at 40% 40%, rgba(31, 74, 59, 0.08), rgba(31, 74, 59, 0) 65%);
  filter: blur(0.8px);
  opacity: 0.42;
  transform: rotate(14deg);
  animation: leafDrift 12s ease-in-out infinite;
}

.leaf-a {
  left: -140px;
  bottom: -120px;
}

.leaf-b {
  left: 40%;
  bottom: -170px;
  width: 520px;
  height: 320px;
  opacity: 0.3;
  transform: rotate(-10deg);
  animation-duration: 14s;
}

.seal {
  position: absolute;
  right: 26px;
  bottom: 22px;
  width: 58px;
  height: 58px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: rgba(255, 246, 234, 0.95);
  background:
    linear-gradient(135deg, rgba(165, 46, 34, 0.92), rgba(123, 30, 22, 0.92));
  box-shadow: 0 8px 14px rgba(23, 58, 47, 0.1);
  transform: rotate(8deg);
  opacity: 0.9;
}

.hero-content {
  padding: 42px 34px;
  position: relative;
  z-index: 1;
}

.hero-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 8px 14px;
  border: 1px solid rgba(46, 98, 79, 0.22);
  background: rgba(255, 255, 255, 0.55);
  font-size: 13px;
  letter-spacing: 0.06em;
  margin-bottom: 14px;
}

.hero-brand {
  letter-spacing: 0.18em;
  margin-bottom: 8px;
  opacity: 0.78;
}

.hero-content h1 {
  font-size: clamp(32px, 4.8vw, 48px);
  margin-bottom: 12px;
  line-height: 1.12;
}

.hero-content p {
  max-width: 720px;
  margin-bottom: 18px;
  opacity: 0.86;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-highlights {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-highlights span {
  border-radius: 16px;
  padding: 7px 12px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(173, 137, 76, 0.18);
  font-size: 13px;
}

@keyframes rippleFloat {
  0% { transform: translate3d(0, 0, 0) rotate(0deg); }
  50% { transform: translate3d(-10px, 10px, 0) rotate(12deg); }
  100% { transform: translate3d(0, 0, 0) rotate(0deg); }
}

@keyframes leafDrift {
  0% { transform: translate3d(0, 0, 0) rotate(14deg); }
  50% { transform: translate3d(12px, -8px, 0) rotate(18deg); }
  100% { transform: translate3d(0, 0, 0) rotate(14deg); }
}

.category-section {
  margin-bottom: 24px;
}

.category-card {
  text-align: left;
  cursor: pointer;
  min-height: 200px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(250, 247, 241, 0.96));
  transition: transform 0.2s ease;
}

.category-card:hover {
  transform: translateY(-6px);
}

.category-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-size: 24px;
  color: #1f4a3b;
  background: rgba(46, 98, 79, 0.1);
}

.category-card h3 {
  margin: 12px 0 8px;
  color: #173a2f;
}

.category-card p {
  color: #6f6759;
  line-height: 1.75;
}

@media (max-width: 768px) {
  .hero-content {
    padding: 30px 22px;
  }
}
</style>
