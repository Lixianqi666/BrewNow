<template>
  <div class="product-detail">
    <div class="page-container">
      <div v-if="isDetailLoading" class="loading-container">
        <el-skeleton animated>
          <template #template>
            <el-row :gutter="30">
              <el-col :span="12">
                <el-skeleton-item variant="image" style="width: 100%; height: 420px;" />
              </el-col>
              <el-col :span="12">
                <el-skeleton-item variant="h1" style="width: 80%; margin-bottom: 20px;" />
                <el-skeleton-item variant="text" style="margin-bottom: 10px;" />
                <el-skeleton-item variant="text" style="margin-bottom: 10px;" />
                <el-skeleton-item variant="h3" style="width: 50%; margin-bottom: 20px;" />
                <el-skeleton-item variant="button" style="width: 100%; height: 52px; margin-bottom: 12px;" />
                <el-skeleton-item variant="button" style="width: 100%; height: 52px;" />
              </el-col>
            </el-row>
          </template>
        </el-skeleton>
      </div>

      <div v-else-if="productDetail" class="product-content">
        <el-row :gutter="32">
          <el-col :xs="24" :sm="24" :md="11">
            <div class="product-image-section">
              <div class="main-image">
                <img
                  :src="resolveProductImageUrl(productDetail.productName, productDetail.imageUrl)"
                  :alt="productDetail.productName"
                  @error="handleProductImageError"
                />
              </div>
            </div>
          </el-col>

          <el-col :xs="24" :sm="24" :md="13">
            <div class="product-info-section">
              <h1 class="product-title">{{ productDetail.productName }}</h1>

              <div class="product-meta">
                <el-tag type="primary" size="small">{{ productDetail.category }}</el-tag>
                <el-tag v-if="productDetail.brand" type="info" size="small">{{ productDetail.brand }}</el-tag>
                <el-tag :type="productDetail.status === 'ACTIVE' ? 'success' : 'warning'" size="small">
                  {{ getStatusText(productDetail.status || '') }}
                </el-tag>
                <el-tag v-for="tag in parsedTags" :key="tag" effect="plain" type="success" size="small">
                  {{ tag }}
                </el-tag>
              </div>

              <div class="product-price">
                <span class="current-price">{{ formatPrice(productDetail.price) }}</span>
                <span class="stock-info">库存：{{ productDetail.stockQuantity }}件</span>
              </div>

              <div class="review-summary-card">
                <div class="review-score">
                  <strong>{{ reviewSummary.averageRating.toFixed(1) }}</strong>
                  <span>综合评分</span>
                </div>
                <div class="review-stars">
                  <el-rate :model-value="reviewSummary.averageRating" disabled show-score text-color="#b97b25" />
                  <p>{{ reviewSummary.reviewCount }} 条真实评价</p>
                </div>
              </div>

              <div v-if="reviewableOrderItemId" class="review-entry-bar">
                <span>你已购买该商品并可评价</span>
                <BaseButton type="primary" plain @click="openReviewDialog">写评价</BaseButton>
              </div>

              <div class="product-specs">
                <div class="spec-item" v-if="productDetail.brand">
                  <span class="spec-label">品牌：</span>
                  <span class="spec-value">{{ productDetail.brand }}</span>
                </div>
                <div class="spec-item" v-if="productDetail.originPlace">
                  <span class="spec-label">产地：</span>
                  <span class="spec-value">{{ productDetail.originPlace }}</span>
                </div>
                <div class="spec-item" v-if="productDetail.flavorProfile">
                  <span class="spec-label">风味：</span>
                  <span class="spec-value">{{ productDetail.flavorProfile }}</span>
                </div>
                <div class="spec-item" v-if="productDetail.compatibleDevices">
                  <span class="spec-label">产品规格：</span>
                  <span class="spec-value">{{ productDetail.compatibleDevices }}</span>
                </div>
                <div class="spec-item">
                  <span class="spec-label">分类：</span>
                  <span class="spec-value">{{ productDetail.category }}</span>
                </div>
              </div>

              <div class="purchase-section">
                <div class="quantity-selector">
                  <span class="quantity-label">数量：</span>
                  <el-input-number
                    v-model="purchaseQuantity"
                    :min="1"
                    :max="productDetail.stockQuantity"
                    size="default"
                  />
                </div>

                <div class="action-buttons">
                  <BaseButton
                    type="primary"
                    size="large"
                    :loading="isAddingToCart"
                    :disabled="productDetail.stockQuantity === 0 || productDetail.status !== 'ACTIVE'"
                    @click="handleAddToCart"
                  >
                    <el-icon><ShoppingCart /></el-icon>
                    加入购物车
                  </BaseButton>

                  <BaseButton
                    type="success"
                    size="large"
                    :disabled="productDetail.stockQuantity === 0 || productDetail.status !== 'ACTIVE'"
                    @click="handleBuyNow"
                  >
                    立即购买
                  </BaseButton>
                </div>

                <div class="secondary-actions">
                  <BaseButton
                    plain
                    class="favorite-button"
                    :type="isFavorite ? 'danger' : 'default'"
                    :disabled="!userStore.isConsumer"
                    @click="toggleFavorite"
                  >
                    <el-icon><Star /></el-icon>
                    {{ isFavorite ? '已收藏' : '收藏商品' }}
                  </BaseButton>
                  <BaseButton plain @click="goBack">返回列表</BaseButton>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="product-description">
          <el-card>
            <template #header>
              <h3>商品详情</h3>
            </template>
            <div class="description-content">
              <p v-if="productDetail.description">{{ productDetail.description }}</p>
              <p v-else class="no-description">暂无详细描述</p>
            </div>
          </el-card>
        </div>

        <div class="review-section">
          <el-card>
            <template #header>
              <div class="section-header">
                <h3>用户评价</h3>
                <div class="review-header-actions">
                  <span>{{ reviewSummary.reviewCount }} 条评价</span>
                  <BaseButton v-if="reviewableOrderItemId" type="primary" plain size="small" @click="openReviewDialog">评价该商品</BaseButton>
                </div>
              </div>
            </template>
            <el-empty v-if="reviews.length === 0" description="暂时还没有评价" />
            <div v-else class="review-list">
              <div v-for="review in reviews" :key="review.reviewId" class="review-item">
                <div class="review-user">
                  <el-avatar :src="review.avatarUrl || undefined">{{ review.username?.slice(0, 1) || '茶' }}</el-avatar>
                  <div>
                    <strong>{{ review.username || '匿名用户' }}</strong>
                    <el-rate :model-value="review.rating" disabled />
                  </div>
                </div>
                <p class="review-content">{{ review.content || '该用户未填写评论内容。' }}</p>
                <span class="review-time">{{ formatDate(review.createdAt) }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <div class="related-section">
          <el-card>
            <template #header>
              <div class="section-header related-header">
                <div class="related-title-block">
                  <h3>看了又看</h3>
                  <p>同类热销与近期高转化茶品</p>
                </div>
                <BaseButton
                  plain
                  size="small"
                  :disabled="relatedRecommendations.length < 2"
                  @click="refreshRelatedProducts"
                >
                  换一组
                </BaseButton>
              </div>
            </template>
            <el-row :gutter="18" class="related-grid">
              <el-col :xs="24" :sm="12" :md="8" class="related-col" v-for="item in relatedRecommendations" :key="item.product.productId">
                <ProductCard
                  :product="item.product"
                  :description="getRelatedReasonText(item)"
                  :meta-tag="item.product.category"
                  :meta-text="item.product.originPlace || item.product.brand || ''"
                  :show-actions="false"
                  @select="viewRelatedProduct(item.product.productId!)"
                />
              </el-col>
            </el-row>
          </el-card>
        </div>
      </div>

      <div v-else class="not-found">
        <el-empty description="商品不存在或已下架">
          <BaseButton type="primary" @click="goBack">返回商品列表</BaseButton>
        </el-empty>
      </div>

      <el-dialog v-model="reviewDialogVisible" title="发表评价" width="520px">
        <el-form label-width="88px">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              maxlength="300"
              show-word-limit
              placeholder="说说这款茶的香气、口感和整体体验"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <BaseButton @click="reviewDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</BaseButton>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import { productApi, type Product } from '@/api/product'
import { favoriteApi } from '@/api/favorite'
import { recommendApi } from '@/api/recommend'
import type { RecommendationItem } from '@/api/recommend'
import { reviewApi, type ProductReview, type ProductReviewSummary } from '@/api/review'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { formatPrice, formatDateTime } from '@/utils'
import { getReliableRecommendationReason } from '@/utils/recommendationReason'
import { resolveProductImageUrl, getDefaultProductImage } from '@/utils/productImageResolver'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isDetailLoading = ref(true)
const productDetail = ref<Product | null>(null)
const purchaseQuantity = ref(1)
const isAddingToCart = ref(false)
const isFavorite = ref(false)
const relatedRecommendations = ref<RecommendationItem[]>([])
const reviews = ref<ProductReview[]>([])
const reviewSummary = ref<ProductReviewSummary>({ averageRating: 0, reviewCount: 0 })
const reviewableOrderItemId = ref<number | null>(null)
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewForm = ref({
  rating: 5,
  content: ''
})

const defaultImage = getDefaultProductImage()

const productId = computed(() => Number(route.params.id))
const parsedTags = computed(() =>
  (productDetail.value?.teaTags || '')
    .split(/[，,]/)
    .map(tag => tag.trim())
    .filter(Boolean)
)

const getRelatedReasonText = (item: RecommendationItem) => {
  return getReliableRecommendationReason(item)
}

const fetchProductDetail = async () => {
  if (!productId.value) {
    router.push('/products')
    return
  }

  try {
    isDetailLoading.value = true
    const response = await productApi.getProductById(productId.value)
    productDetail.value = response.data || null
  } catch (error) {
    console.error('获取商品详情失败:', error)
    productDetail.value = null
    ElMessage.error('获取商品详情失败')
  } finally {
    isDetailLoading.value = false
  }
}

const fetchFavoriteStatus = async () => {
  if (!userStore.isLoggedIn || !userStore.isConsumer || !productId.value) {
    isFavorite.value = false
    return
  }
  try {
    const response = await favoriteApi.getFavoriteStatus(productId.value)
    isFavorite.value = !!response.data?.favorited
  } catch {
    isFavorite.value = false
  }
}

const fetchReviews = async () => {
  if (!productId.value) return
  try {
    const [reviewResponse, summaryResponse] = await Promise.all([
      reviewApi.getProductReviews(productId.value),
      reviewApi.getProductReviewSummary(productId.value)
    ])
    reviews.value = reviewResponse.data || []
    reviewSummary.value = summaryResponse.data || { averageRating: 0, reviewCount: 0 }
  } catch {
    reviews.value = []
    reviewSummary.value = { averageRating: 0, reviewCount: 0 }
  }
}

const fetchReviewableEntry = async () => {
  if (!productId.value || !userStore.isLoggedIn || !userStore.isConsumer) {
    reviewableOrderItemId.value = null
    return
  }
  try {
    const response = await reviewApi.getReviewableOrderItem(productId.value)
    reviewableOrderItemId.value = response.data?.canReview ? response.data?.orderItem?.orderItemId || null : null
  } catch {
    reviewableOrderItemId.value = null
  }
}

const fetchRelatedProducts = async () => {
  if (!productId.value) return
  try {
    const response = await recommendApi.getRelatedRecommendationItems(productId.value, 6)
    relatedRecommendations.value = (response.data || []).filter((item) => item?.product?.productId)
  } catch {
    relatedRecommendations.value = []
  }
}

const refreshRelatedProducts = () => {
  if (relatedRecommendations.value.length < 2) return
  relatedRecommendations.value = [...relatedRecommendations.value]
    .sort(() => Math.random() - 0.5)
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: '在售',
    INACTIVE: '下架',
    DISCONTINUED: '停产'
  }
  return statusMap[status] || status
}

const formatDate = (value?: string) => value ? formatDateTime(value).split(' ')[0] : ''

const handleProductImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  if (img.src.includes('data:image')) return
  img.src = defaultImage
}

const handleAddToCart = async () => {
  if (!productDetail.value) return
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
    isAddingToCart.value = true
    await cartStore.addToCart(productDetail.value.productId!, purchaseQuantity.value)
    ElMessage.success(`已添加 ${purchaseQuantity.value} 件商品到购物车`)
  } catch (error) {
    console.error('添加到购物车失败:', error)
    ElMessage.error('添加到购物车失败')
  } finally {
    isAddingToCart.value = false
  }
}

const handleBuyNow = () => {
  if (!productDetail.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('立即购买需先登录消费者账号')
    router.push('/login')
    return
  }
  if (!userStore.isConsumer) {
    ElMessage.warning('仅消费者账号可立即购买')
    return
  }
  if (productDetail.value.stockQuantity === 0) {
    ElMessage.error('商品库存不足')
    return
  }
  if (purchaseQuantity.value > productDetail.value.stockQuantity) {
    ElMessage.error('购买数量超过库存')
    return
  }

  const instantCheckoutItem = {
    productId: productDetail.value.productId,
    productName: productDetail.value.productName,
    price: productDetail.value.price,
    quantity: purchaseQuantity.value,
    imageUrl: resolveProductImageUrl(productDetail.value.productName, productDetail.value.imageUrl),
    category: productDetail.value.category,
    brand: productDetail.value.brand
  }

  sessionStorage.setItem('buyNowItem', JSON.stringify(instantCheckoutItem))
  sessionStorage.setItem('checkoutType', 'buyNow')
  router.push('/checkout')
}

const toggleFavorite = async () => {
  if (!productDetail.value?.productId) return
  if (!userStore.isLoggedIn || !userStore.isConsumer) {
    ElMessage.warning('请先登录消费者账号后再收藏')
    return
  }
  try {
    const response = await favoriteApi.toggleFavorite(productDetail.value.productId)
    isFavorite.value = !!response.data?.favorited
    ElMessage.success(isFavorite.value ? '已加入收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

const viewRelatedProduct = (id: number) => {
  router.push(`/product/${id}`)
}

const openReviewDialog = () => {
  if (!reviewableOrderItemId.value) {
    ElMessage.warning('当前没有可评价的已购订单')
    return
  }
  reviewForm.value = {
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewableOrderItemId.value) return
  reviewSubmitting.value = true
  try {
    await reviewApi.submitReview({
      orderItemId: reviewableOrderItemId.value,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    await Promise.all([fetchReviews(), fetchReviewableEntry()])
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    reviewSubmitting.value = false
  }
}

const goBack = () => {
  router.push('/products')
}

const loadPageData = async () => {
  await fetchProductDetail()
  await Promise.all([fetchFavoriteStatus(), fetchReviews(), fetchRelatedProducts(), fetchReviewableEntry()])
}

watch(() => route.params.id, loadPageData)
watch(
  () => [userStore.isLoggedIn, userStore.userType],
  () => {
    fetchFavoriteStatus()
    fetchReviewableEntry()
  }
)

onMounted(loadPageData)
</script>

<style scoped>
.product-detail {
  min-height: calc(100vh - 120px);
  background: linear-gradient(180deg, #f7f3eb 0%, #f3ece0 100%);
  padding: 20px 0 36px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading-container,
.product-content,
.product-description,
.review-section,
.related-section,
.not-found {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 22px;
  border: 1px solid rgba(200, 155, 82, 0.16);
  box-shadow: 0 16px 36px rgba(23, 58, 47, 0.1);
}

.loading-container,
.not-found {
  padding: 30px;
}

.product-content {
  padding: 30px;
  margin-bottom: 20px;
}

.product-description,
.review-section,
.related-section {
  margin-top: 20px;
}

.product-image-section {
  text-align: center;
}

.main-image {
  width: 100%;
  height: 440px;
  border: 1px solid rgba(200, 155, 82, 0.18);
  border-radius: 18px;
  overflow: hidden;
  background: radial-gradient(circle at top right, rgba(200, 155, 82, 0.2), transparent 36%), linear-gradient(135deg, #f3e6d2, #ddd0be);
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.product-info-section {
  padding-left: 12px;
}

.product-title {
  font-size: 32px;
  font-weight: 700;
  color: #173a2f;
  margin: 0 0 16px;
  line-height: 1.35;
}

.product-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.product-price {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.current-price {
  font-size: 38px;
  font-weight: 700;
  color: #b97b25;
}

.stock-info {
  color: #7a7468;
}

.review-summary-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 18px 20px;
  margin-bottom: 24px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fffaf2, #f5ecdc);
  border: 1px solid rgba(185, 123, 37, 0.14);
}

.review-score strong {
  display: block;
  color: #173a2f;
  font-size: 34px;
  line-height: 1;
}

.review-score span {
  display: block;
  margin-top: 6px;
  color: #7a7468;
}

.review-stars p {
  margin: 8px 0 0;
  color: #7a7468;
}

.product-specs {
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(180deg, #fffdf7, #f7efe1);
  border-radius: 16px;
  border: 1px solid rgba(200, 155, 82, 0.16);
}

.spec-item {
  display: flex;
  margin-bottom: 12px;
  align-items: center;
}

.spec-item:last-child {
  margin-bottom: 0;
}

.spec-label {
  font-weight: 600;
  color: #173a2f;
  min-width: 80px;
}

.spec-value {
  color: #5e594f;
}

.purchase-section {
  border-top: 1px solid rgba(200, 155, 82, 0.2);
  padding-top: 28px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  margin-bottom: 26px;
  gap: 14px;
}

.quantity-label {
  font-weight: 600;
  color: #173a2f;
}

.action-buttons {
  display: flex;
  gap: 18px;
  margin-bottom: 14px;
}

.action-buttons .el-button,
.secondary-actions .el-button {
  height: 52px;
  border-radius: 16px;
}

.action-buttons .el-button {
  flex: 1;
}

.secondary-actions {
  display: flex;
  gap: 14px;
}

.secondary-actions .el-button {
  flex: 1;
}

.description-content {
  line-height: 1.9;
  color: #5e594f;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #173a2f;
}

.related-header {
  gap: 16px;
}

.related-title-block h3 {
  margin: 0;
}

.related-title-block p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #7a7468;
}

.related-grid {
  align-items: stretch;
}

.related-col {
  display: flex;
  margin-bottom: 18px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.review-item {
  padding: 18px 0;
  border-bottom: 1px solid rgba(200, 155, 82, 0.14);
}

.review-item:last-child {
  border-bottom: none;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-content {
  margin: 12px 0 10px;
  line-height: 1.8;
  color: #4d463b;
}

.review-time {
  color: #8f8576;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-container {
    padding: 0 12px;
  }

  .product-content {
    padding: 18px;
  }

  .product-info-section {
    padding-left: 0;
    margin-top: 20px;
  }

  .product-title {
    font-size: 24px;
  }

  .product-price,
  .review-summary-card,
  .secondary-actions,
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .main-image {
    height: 320px;
  }

  .related-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
