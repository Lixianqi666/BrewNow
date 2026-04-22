<template>
  <div class="merchant-products">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <h2>商品管理</h2>
          <BaseButton type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加商品
          </BaseButton>
        </div>
      </template>

      <div class="content">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-input
                v-model="searchForm.keyword"
                placeholder="请输入商品名称或型号"
                clearable
                @keyup.enter="searchProducts"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-select v-model="searchForm.category" placeholder="商品分类" clearable>
                <el-option label="绿茶" value="绿茶" />
                <el-option label="红茶" value="红茶" />
                <el-option label="乌龙茶" value="乌龙茶" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <BaseButton class="search-btn" type="primary" @click="searchProducts">搜索</BaseButton>
            </el-col>
          </el-row>
        </div>

        <!-- 商品列表 -->
        <div class="product-list">
          <el-table
            :data="productList"
            v-loading="loading"
            stripe
            class="compact-table"
            height="520"
          >
            <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="90" align="center">
              <template #default="scope">
                <el-tag :type="getCategoryType(scope.row.category)">
                  {{ getCategoryLabel(scope.row.category) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="originPlace" label="产地" width="110" show-overflow-tooltip align="center" />
            <el-table-column prop="price" label="价格" width="90" align="center">
              <template #default="scope">
                <span class="price">￥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="库存" width="70" align="center">
              <template #default="scope">
                <span :class="{ 'low-stock': scope.row.stockQuantity < 10 }">
                  {{ scope.row.stockQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 'ACTIVE' ? '上架' : '下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="teaTags" label="茶叶标签" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <div class="action-stack">
                  <div class="action-row">
                    <BaseButton
                      class="action-button"
                      type="warning"
                      size="small"
                      @click="editProduct(scope.row)"
                    >
                      编辑
                    </BaseButton>
                    <BaseButton
                      class="action-button"
                      :type="scope.row.status === 'ACTIVE' ? 'warning' : 'primary'"
                      size="small"
                      @click="toggleProductStatus(scope.row)"
                    >
                      {{ scope.row.status === 'ACTIVE' ? '下架' : '上架' }}
                    </BaseButton>
                  </div>
                  <BaseButton
                    class="action-button action-button--danger"
                    type="danger"
                    size="small"
                    @click="deleteProduct(scope.row)"
                  >
                    删除
                  </BaseButton>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <!-- 暂无数据 -->
        <el-empty v-if="!loading && productList.length === 0" description="暂无商品数据">
          <BaseButton type="primary" @click="showAddDialog">添加商品</BaseButton>
        </el-empty>
      </div>
    </el-card>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="50%"
      @close="resetForm"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="productName">
              <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属品牌">
              <el-input :model-value="merchantBrand" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="category">
              <el-select v-model="productForm.category" placeholder="请选择分类">
                <el-option label="绿茶" value="绿茶" />
                <el-option label="红茶" value="红茶" />
                <el-option label="乌龙茶" value="乌龙茶" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地" prop="originPlace">
              <el-input v-model="productForm.originPlace" placeholder="如：云南临沧 / 福建安溪" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number
                v-model="productForm.price"
                :min="0"
                :precision="2"
                :step="0.01"
                placeholder="请输入价格"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stockQuantity">
              <el-input-number
                v-model="productForm.stockQuantity"
                :min="0"
                placeholder="请输入库存"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="茶叶标签" prop="teaTags">
              <el-input v-model="productForm.teaTags" placeholder="如：回甘, 花香, 耐泡" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="口感特征" prop="flavorProfile">
              <el-input v-model="productForm.flavorProfile" placeholder="如：鲜爽甘醇 / 花果香明显" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预警库存" prop="warningStock">
              <el-input-number
                v-model="productForm.warningStock"
                :min="1"
                placeholder="库存预警阈值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品图片" prop="imageUrl">
          <div class="image-upload-box">
            <el-upload
              class="product-uploader"
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleProductImageChange"
              :disabled="imageUploading"
            >
              <img v-if="productForm.imageUrl" :src="productForm.imageUrl" class="product-image-preview" alt="商品图片预览" />
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span class="upload-label">点击上传商品图片</span>
              </div>
            </el-upload>
            <div class="upload-actions" v-if="productForm.imageUrl">
              <BaseButton text type="danger" @click="clearProductImage">移除图片</BaseButton>
            </div>
            <p class="upload-tip">支持 JPG / PNG / WEBP，建议上传清晰商品实拍图</p>
          </div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <!-- 状态管理已移至列表操作，不在编辑表单中显示 -->
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <BaseButton @click="dialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" @click="submitForm" :loading="submitting">
            {{ isEdit ? '更新' : '添加' }}
          </BaseButton>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { merchantApi, CategoryLabels } from '@/api/merchant'
import type { Product, MerchantProductsParams } from '@/api/merchant'
import { useUserStore } from '@/stores/user'
import { getMerchantBrandName } from '@/utils/merchant'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const imageUploading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const productFormRef = ref<FormInstance>()
const userStore = useUserStore()

const merchantBrand = computed(() => {
  return getMerchantBrandName(userStore.merchantInfo, userStore.userInfo?.account)
})

watch(
  () => userStore.merchantInfo?.merchantId || userStore.token,
  () => {
    pagination.currentPage = 1
    productList.value = []
    if (userStore.isLoggedIn) {
      loadProducts()
    }
  }
)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 商品列表
const productList = ref<Product[]>([])

// 商品表单
const productForm = reactive<Product>({
  productId: undefined,
  productName: '',
  brand: '',
  category: '',
  teaTags: '',
  originPlace: '',
  flavorProfile: '',
  price: 0,
  stockQuantity: 0,
  warningStock: 10,
  imageUrl: '',
  description: '',
  status: 'INACTIVE'
})

// 表单验证规则
const productRules: FormRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  originPlace: [
    { required: true, message: '请输入茶叶产地', trigger: 'blur' }
  ],
  teaTags: [
    { required: true, message: '请输入茶叶标签', trigger: 'blur' }
  ],
  flavorProfile: [
    { required: true, message: '请输入口感特征', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  stockQuantity: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' }
  ],
  warningStock: [
    { required: true, message: '请输入库存预警值', trigger: 'blur' },
    { type: 'number', min: 1, message: '预警库存必须大于等于1', trigger: 'blur' }
  ]
}

// 获取分类标签
const getCategoryLabel = (category: string) => {
  return CategoryLabels[category as keyof typeof CategoryLabels] || category || '未知'
}

// 获取分类标签类型
const getCategoryType = (category: string) => {
  const typeMap: Record<string, string> = {
    '绿茶': 'primary',
    '红茶': 'success',
    '乌龙茶': 'warning',
    '其他': 'info'
  }
  return typeMap[category] || 'info'
}

const syncMerchantBrand = () => {
  productForm.brand = merchantBrand.value
}

const handleProductImageChange = async (uploadFile: any) => {
  if (!userStore.isLoggedIn) {
    return
  }

  const rawFile: File | undefined = uploadFile?.raw
  if (!rawFile) return

  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('仅支持图片格式')
    return
  }

  if (rawFile.size > 5 * 1024 * 1024) {
    ElMessage.error('商品图片大小不能超过 5MB')
    return
  }

  imageUploading.value = true
  try {
    const response = await merchantApi.uploadProductImage(rawFile)
    if (response.code === 200) {
      const imageUrl = response.data?.imageUrl || ''
      if (!imageUrl) {
        ElMessage.error('商品图片上传失败')
        return
      }
      productForm.imageUrl = imageUrl
      ElMessage.success('商品图片上传成功')
    } else {
      ElMessage.error(response.message || '商品图片上传失败')
    }
  } catch (error) {
    console.error('商品图片上传失败', error)
    ElMessage.error('商品图片上传失败')
  } finally {
    imageUploading.value = false
  }
}

const clearProductImage = () => {
  productForm.imageUrl = ''
}

const getActionErrorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'string' && error) {
    return error
  }
  return fallback
}

const shouldRefreshProducts = (message: string) => {
  return message.includes('无权操作此商品') || message.includes('商品不存在')
}

// 加载商品列表
const loadProducts = async () => {
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const params: MerchantProductsParams = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      category: searchForm.category || undefined
    }

    const response = await merchantApi.getMerchantProducts(params)
    if (response.code === 200) {
      productList.value = response.data.list || []
      pagination.total = response.data.total || 0

      // 调试日志 - 检查状态值
      console.log('商品列表加载成功，状态检查:',
        productList.value.map(p => ({id: p.productId, merchantId: p.merchantId, name: p.productName, status: p.status}))
      )
    } else {
      ElMessage.error(response.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('加载商品列表失败', error)
    ElMessage.error('获取商品列表失败')
    // 确保在错误情况下也有合理的显示
    if (productList.value.length === 0) {
      productList.value = []
    }
  } finally {
    loading.value = false
  }
}

// 搜索商品
const searchProducts = () => {
  pagination.currentPage = 1
  loadProducts()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
  resetForm()
}

// 编辑商品
const editProduct = (product: Product) => {
  isEdit.value = true
  dialogVisible.value = true

  // 清空并复制所有字段，确保description等字段正确回显
  resetForm()
  Object.assign(productForm, {
    productId: product.productId,
    productName: product.productName,
    brand: merchantBrand.value,
    category: product.category,
    teaTags: product.teaTags || '',
    originPlace: product.originPlace || '',
    flavorProfile: product.flavorProfile || '',
    price: product.price,
    stockQuantity: product.stockQuantity,
    warningStock: product.warningStock ?? 10,
    imageUrl: product.imageUrl || '',
    description: product.description || '', // 确保描述字段有值
    status: product.status // 保留状态但不在表单中显示
  })
}

// 切换商品状态
const toggleProductStatus = async (product: Product) => {
  if (!userStore.isLoggedIn) {
    return
  }

  const newStatus = product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? '上架' : '下架'

  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 显示Loading状态
    loading.value = true

    try {
      const response = await merchantApi.updateProductStatus(product.productId!, newStatus)
      if (response.code === 200) {
        // 直接重新加载列表而不是局部更新，确保显示与数据库同步
        await loadProducts()
        ElMessage.success(`商品已${action}`)
      }
    } catch (error) {
      console.error('更新商品状态失败', error)
      const message = getActionErrorMessage(error, `商品${action}失败，请刷新后重试`)
      ElMessage.error(message)
      if (shouldRefreshProducts(message)) {
        await loadProducts()
      }
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用户取消操作，不做处理
    if (error !== 'cancel') {
      console.error('状态更新对话框错误', error)
    }
  }
}

// 删除商品
const deleteProduct = async (product: Product) => {
  if (!userStore.isLoggedIn) {
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后商品将从商城隐藏，历史订单信息不受影响。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await merchantApi.deleteProduct(product.productId!)
    if (response.code === 200) {
      ElMessage.success('商品已删除')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败', error)
      const message = getActionErrorMessage(error, '删除商品失败')
      ElMessage.error(message)
      if (shouldRefreshProducts(message)) {
        await loadProducts()
      }
    }
  }
}

// 重置表单
const resetForm = () => {
  if (productFormRef.value) {
    productFormRef.value.resetFields()
  }
  Object.assign(productForm, {
    productId: undefined,
    productName: '',
    brand: merchantBrand.value,
    category: '',
    teaTags: '',
    originPlace: '',
    flavorProfile: '',
    price: 0,
    stockQuantity: 0,
    warningStock: 10,
    imageUrl: '',
    description: '',
    status: 'INACTIVE'
  })
}

// 提交表单
const submitForm = () => {
  if (!productFormRef.value || !userStore.isLoggedIn) return

  productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true

      try {
        syncMerchantBrand()
        if (!productForm.warningStock || productForm.warningStock < 1) {
          productForm.warningStock = Math.max(1, Math.min(10, productForm.stockQuantity || 10))
        }
        // 设置默认状态（如果是新增）或保留当前状态（如果是编辑）
        if (!productForm.status) {
          productForm.status = isEdit.value ? 'INACTIVE' : 'INACTIVE'
        }

        let response
        if (isEdit.value) {
          // 更新商品
          response = await merchantApi.updateProduct(productForm.productId!, productForm)
        } else {
          // 添加商品
          response = await merchantApi.addProduct(productForm)
        }

        if (response.code === 200) {
          ElMessage.success(isEdit.value ? '商品更新成功' : '商品添加成功')
          dialogVisible.value = false
          resetForm()
          loadProducts() // 重新加载列表
        } else {
          ElMessage.error(response.message || (isEdit.value ? '商品更新失败' : '商品添加失败'))
        }
      } catch (error) {
        console.error('提交商品失败', error)
        ElMessage.error(isEdit.value ? '商品更新失败' : '商品添加失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 分页处理
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadProducts()
}

const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
  loadProducts()
}

// 组件挂载
onMounted(() => {
  syncMerchantBrand()
  if (userStore.isLoggedIn) {
    loadProducts()
  }
})
</script>

<style scoped>
.merchant-products {
  padding: 20px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.search-bar {
  margin-bottom: 20px;
}

.search-bar :deep(.el-col) {
  max-width: fit-content;
}

.search-bar :deep(.el-input) {
  width: 200px;
}

.search-bar :deep(.el-select) {
  width: 140px;
}

.search-bar :deep(.el-input__wrapper) {
  height: 32px;
  line-height: 30px;
}

.search-bar :deep(.el-select .el-input__wrapper) {
  height: 32px;
}

.search-bar :deep(.el-input__inner) {
  height: 30px;
  line-height: 30px;
}

.search-btn {
  height: 32px !important;
  padding: 0 16px !important;
  font-size: 13px;
}

.product-list {
  margin-top: 20px;
}

.compact-table :deep(.el-table__header th) {
  padding: 8px 0;
  font-size: 13px;
}

.compact-table :deep(.el-table__body td) {
  padding: 6px 0;
  font-size: 13px;
}

.compact-table :deep(.el-table__cell) {
  line-height: 1.5;
}

.action-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  padding: 4px 0;
}

:deep(.action-stack .el-button) {
  padding: 0 16px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.action-button {
  min-width: 78px;
}

.action-button--danger {
  align-self: flex-end;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.image-upload-box {
  width: 100%;
}

.product-uploader {
  width: 220px;
  display: block;
}

.product-image-preview,
.upload-placeholder {
  width: 220px;
  height: 220px;
  border-radius: 16px;
  object-fit: cover;
  border: 1px dashed #d8c8ad;
  background: linear-gradient(180deg, #fcfaf4, #f4ead9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7e6b56;
  transition: all 0.2s ease;
}

.upload-placeholder {
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
}

.upload-label {
  line-height: 1.4;
}

.upload-placeholder:hover {
  border-color: #8ba67f;
  color: #3f6f52;
}

.upload-icon {
  font-size: 28px;
}

.upload-actions {
  margin-top: 8px;
}

.upload-tip {
  margin: 8px 0 0;
  color: #8a7b69;
  font-size: 12px;
}

:deep(.el-table) {
  font-size: 14px;
}

.tea-tags-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-dialog) {
  margin-top: 5vh;
}
</style>
