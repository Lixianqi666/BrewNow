<template>
  <div class="product-list">
    <div class="page-container">
      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索茶叶名称或关键词..."
            :prefix-icon="Search"
            @change="handleSearch"
            clearable
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="selectedCategory"
            placeholder="选择茶类"
            clearable
            @change="handleCategoryChange"
          >
            <el-option label="全部茶类" value="" />
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="sortBy"
            placeholder="排序方式"
            @change="handleSort"
          >
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="最新发布" value="newest" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 商品网格展示区域 -->
    <div class="products-grid">
      <div v-if="products.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无商品数据">
          <el-button type="primary" @click="loadProducts">刷新</el-button>
        </el-empty>
      </div>

      <!-- Loading 骨架屏 -->
      <el-row :gutter="20" v-if="loading">
        <el-col
          v-for="i in 8"
          :key="`skeleton-${i}`"
          :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
          class="product-col"
        >
          <el-card class="product-card skeleton-card">
            <el-skeleton animated>
              <template #template>
                <div class="skeleton-image"></div>
                <div style="padding: 16px;">
                  <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 8px;" />
                  <el-skeleton-item variant="text" style="margin-bottom: 4px;" />
                  <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 16px;" />
                  <el-skeleton-item variant="h3" style="width: 40%; margin-bottom: 16px;" />
                  <el-skeleton-item variant="button" style="width: 100%; height: 32px; margin-bottom: 8px;" />
                  <el-skeleton-item variant="button" style="width: 100%; height: 32px;" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <!-- 实际商品数据 -->
      <el-row :gutter="20" v-else-if="products.length > 0">
        <el-col
          v-for="product in products"
          :key="product.productId"
          :xs="24" :sm="12" :md="8" :lg="6" :xl="6"
          class="product-col"
        >
          <ProductCard
            :product="product"
            :description="product.description || '暂无描述'"
            :meta-tag="product.category"
            :meta-text="product.originPlace || product.brand || ''"
            @select="viewProduct(product)"
            @addCart="addToCart"
            @buyNow="buyNow"
          />
        </el-col>
      </el-row>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, type Product } from '@/api/product'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { resolveProductImageUrl } from '@/utils/productImageResolver'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 分类映射（label用于展示茶类语义，value保留原分类值用于接口传参）
const categoryOptions = [
  { label: '绿茶', value: '绿茶' },
  { label: '红茶', value: '红茶' },
  { label: '乌龙茶', value: '乌龙茶' },
  { label: '白茶', value: '白茶' },
  { label: '花茶', value: '花茶' },
  { label: '普洱茶', value: '普洱茶' }
]

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const selectedCategory = ref('')
const sortBy = ref('default')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const products = ref<Product[]>([])

const estimateServerTotal = (page: number, size: number, currentPageLength: number) => {
  if (currentPageLength === 0) {
    return page === 1 ? 0 : (page - 1) * size
  }
  if (currentPageLength < size) {
    return (page - 1) * size + currentPageLength
  }
  // 后端列表接口暂未返回总数，满页时保守估算“至少还有下一页”
  return page * size + 1
}

// 加载商品数据
const loadProducts = async () => {
  loading.value = true
  try {
    const isClientPagination = Boolean(searchKeyword.value || selectedCategory.value)

    const response = isClientPagination
      ? await productApi.searchProducts(searchKeyword.value || undefined, selectedCategory.value || undefined)
      : await productApi.getAllProducts(currentPage.value, pageSize.value)

    const rawList: Product[] = Array.isArray(response.data)
      ? response.data
      : ((response.data as any)?.list || [])

    const productList = [...rawList]

    // 前端排序
    switch (sortBy.value) {
      case 'price_asc':
        productList.sort((a: Product, b: Product) => a.price - b.price)
        break
      case 'price_desc':
        productList.sort((a: Product, b: Product) => b.price - a.price)
        break
      case 'newest':
        productList.sort((a: Product, b: Product) => (b.productId || 0) - (a.productId || 0))
        break
    }

    if (isClientPagination) {
      total.value = productList.length
      const startIndex = (currentPage.value - 1) * pageSize.value
      products.value = productList.slice(startIndex, startIndex + pageSize.value)
    } else {
      products.value = productList
      total.value = estimateServerTotal(currentPage.value, pageSize.value, productList.length)
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败，请重试')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  loadProducts()
}

// 处理分类变化
const handleCategoryChange = () => {
  currentPage.value = 1
  loadProducts()
}

// 处理排序
const handleSort = () => {
  currentPage.value = 1
  loadProducts()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadProducts()
}

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

// 添加到购物车
const addToCart = async (product: Product) => {
  if (!product.productId) return

  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('加入购物车需先登录消费者账号')
    router.push('/login')
    return
  }

  // 仅消费者允许加入购物车
  if (!userStore.isConsumer) {
    ElMessage.warning('仅消费者账号可加入购物车')
    return
  }

  try {
    await cartStore.addToCart(product.productId, 1)
    ElMessage.success(`${product.productName} 已加入购物车`)
  } catch (error: any) {
    console.error('加入购物车失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('加入购物车失败，请重试')
    }
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

// 查看商品详情
const viewProduct = (product: Product) => {
  if (product.productId) {
    router.push(`/product/${product.productId}`)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  const queryCategory = route.query.category
  if (typeof queryCategory === 'string') {
    selectedCategory.value = queryCategory
  }
  loadProducts()
})
</script>

<style scoped>
.product-list {
  width: 100%;
  background: transparent;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-filter-section {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(8px);
  padding: 22px;
  border-radius: 20px;
  border: 1px solid rgba(200, 155, 82, 0.16);
  box-shadow: 0 16px 36px rgba(23, 58, 47, 0.1);
  margin-bottom: 20px;
}

.products-grid {
  min-height: 600px;
}

.product-col {
  display: flex;
  margin-bottom: 20px;
}

.pagination-section {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 骨架屏样式 */
.skeleton-card {
  min-height: 460px;
  border: 1px solid rgba(200, 155, 82, 0.18);
}

.skeleton-image {
  height: 200px;
  background: #f4ead9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 14px;
  }

  .search-filter-section .el-col {
    margin-bottom: 12px;
  }

  .search-filter-section .el-col:last-child {
    margin-bottom: 0;
  }
}
</style>
