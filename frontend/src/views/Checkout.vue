<template>
  <div class="checkout-page">
    <div class="checkout-header">
      <h1>订单结算</h1>
      <p>请确认订单信息并完成支付</p>
    </div>

    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <el-empty description="请先登录完成结算">
        <BaseButton type="primary" @click="goToLogin">立即登录</BaseButton>
      </el-empty>
    </div>

    <div v-else-if="!checkoutItems.length" class="empty-checkout">
      <el-empty description="没有要结算的商品">
        <BaseButton type="primary" @click="goToCart">返回购物车</BaseButton>
      </el-empty>
    </div>

    <div v-else class="checkout-content">
      <el-row :gutter="30">
        <el-col :md="14" :sm="24">
          <el-card class="address-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Location /></el-icon>
                <span>收货地址</span>
                <BaseButton type="primary" link @click="openAddressDialog">新增地址</BaseButton>
              </div>
            </template>

            <el-empty v-if="!addressList.length" description="暂无收货地址，请先新增地址">
              <BaseButton type="primary" @click="openAddressDialog">新增地址并使用</BaseButton>
            </el-empty>

            <el-radio-group v-else v-model="selectedAddressId" class="address-radio-group">
              <div v-for="item in addressList" :key="item.addressId" class="address-option" :class="{ active: selectedAddressId === item.addressId }">
                <div class="address-radio-content">
                  <el-radio :value="item.addressId">
                    <div class="address-main">
                      <div class="address-line">
                        <strong>{{ item.receiverName }}</strong>
                        <span>{{ item.contactPhone }}</span>
                        <el-tag v-if="item.tag" size="small">{{ item.tag }}</el-tag>
                      </div>
                      <p class="address-detail" :title="formatFullAddress(item)">{{ formatFullAddress(item) }}</p>
                    </div>
                  </el-radio>
                </div>
                <div class="address-ops">
                  <el-tag v-if="item.isDefault" size="small" type="success">默认</el-tag>
                  <BaseButton text type="primary" v-if="!item.isDefault" @click="setDefaultAddress(item.addressId!)">设为默认</BaseButton>
                  <span v-else class="address-op-placeholder">设为默认</span>
                  <BaseButton text type="danger" @click="removeAddress(item.addressId!)">删除</BaseButton>
                </div>
              </div>
            </el-radio-group>

            <div class="remark-panel">
              <div class="remark-header">
                <span class="remark-title">订单备注</span>
                <span class="remark-tip">选填，可填写口味偏好、送达时间或其他说明</span>
              </div>
              <el-input
                v-model="extraForm.remark"
                type="textarea"
                :rows="3"
                resize="none"
                placeholder="例如：工作日白天送达；礼盒无需手提袋；尽量避开午休时间。"
                maxlength="200"
                show-word-limit
              />
            </div>
          </el-card>

          <el-card class="payment-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><CreditCard /></el-icon>
                <span>支付方式</span>
              </div>
            </template>

            <div class="payment-methods">
              <div
                v-for="method in paymentMethods"
                :key="method.value"
                class="payment-option"
                :class="{ active: selectedPaymentMethod === method.value }"
                @click="selectedPaymentMethod = method.value"
              >
                <div class="payment-content">
                  <div class="payment-icon">
                    <component :is="method.icon" />
                  </div>
                  <div class="payment-info">
                    <h4>{{ method.label }}</h4>
                    <p>{{ method.description }}</p>
                  </div>
                  <div class="payment-check">
                    <el-radio :model-value="selectedPaymentMethod" :value="method.value" />
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :md="10" :sm="24">
          <div class="order-summary">
            <el-card class="summary-card" shadow="always">
              <template #header>
                <div class="card-header">
                  <el-icon><ShoppingBag /></el-icon>
                  <span>订单信息</span>
                </div>
              </template>

              <div class="product-list">
                <div v-for="item in checkoutItems" :key="item.cartItemId" class="product-item">
                  <div class="product-image">
                    <img
                      :src="resolveProductImageUrl(item.product.productName, item.product.imageUrl)"
                      :alt="item.product.productName"
                      @error="handleImageError"
                    />
                  </div>
                  <div class="product-info">
                    <h4>{{ item.product.productName }}</h4>
                    <p>{{ item.product.brand }} · {{ item.product.category }}</p>
                    <div class="product-price">
                      <span class="price">{{ formatPrice(item.product.price) }}</span>
                      <span class="quantity">× {{ item.quantity }}</span>
                    </div>
                  </div>
                  <div class="product-total">
                    {{ formatPrice(item.product.price * item.quantity) }}
                  </div>
                </div>
              </div>

              <el-divider />

              <div class="price-summary">
                <div class="price-row">
                  <span>商品总额：</span>
                  <span>{{ formatPrice(subtotal) }}</span>
                </div>
                <div class="price-row">
                  <span>运费：</span>
                  <span class="shipping">{{ shippingFee > 0 ? formatPrice(shippingFee) : '免运费' }}</span>
                </div>
                <el-divider />
                <div class="price-row total">
                  <span>应付金额：</span>
                  <span class="total-amount">{{ formatPrice(totalAmount) }}</span>
                </div>
              </div>

              <div class="submit-section">
                <BaseButton
                  type="primary"
                  size="large"
                  @click="submitOrder"
                  :loading="submitting"
                  :disabled="!selectedAddress"
                >
                  {{ submitting ? '提交中...' : `提交订单 ${formatPrice(totalAmount)}` }}
                </BaseButton>
                <BaseButton size="large" @click="goBack">
                  {{ isFromBuyNow ? '返回首页' : '返回购物车' }}
                </BaseButton>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="addressDialogVisible" title="新增收货地址" width="560px">
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="92px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" maxlength="20" />
        </el-form-item>
        <el-form-item label="手机号" prop="contactPhone">
          <el-input v-model="addressForm.contactPhone" maxlength="11" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="如：广东省" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="如：深圳市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="如：南山区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="addressForm.tag" placeholder="家/公司/学校" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <BaseButton @click="addressDialogVisible = false">取消</BaseButton>
        <BaseButton type="primary" :loading="addressSaving" @click="saveAddress">保存并使用</BaseButton>
      </template>
    </el-dialog>

    <el-dialog
      v-model="paymentDialogVisible"
      title="支付进行中"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="payment-progress">
        <div class="payment-steps">
          <el-steps :active="currentStep" direction="vertical" space="60px">
            <el-step title="订单已创建" description="订单创建成功，等待支付" />
            <el-step title="支付处理中" description="正在连接支付系统..." />
            <el-step title="支付完成" description="支付成功，订单处理中" />
          </el-steps>
        </div>
        <div class="payment-info" v-if="createdOrder">
          <p><strong>订单号：</strong>{{ createdOrder.orderNumber }}</p>
          <p><strong>支付金额：</strong>{{ formatPrice(createdOrder.totalAmount) }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Location,
  CreditCard,
  ShoppingBag,
  Wallet,
  ChatDotRound,
  Van
} from '@element-plus/icons-vue'
import { formatPrice, isValidPhone } from '@/utils'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { orderApi, type CheckoutItemPayload, type CreateOrderRequest, type Order, PaymentMethod } from '@/api/order'
import type { CartItem } from '@/api/cart'
import type { FormInstance, FormRules } from 'element-plus'
import { resolveProductImageUrl } from '@/utils/productImageResolver'
import { addressApi, type UserAddress } from '@/api/address'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const addressFormRef = ref<FormInstance>()
const checkoutItems = ref<CartItem[]>([])
const checkoutSourceType = ref<'cart' | 'buyNow'>('cart')
const submitting = ref(false)
const paymentDialogVisible = ref(false)
const currentStep = ref(0)
const createdOrder = ref<Order | null>(null)

const addressList = ref<UserAddress[]>([])
const selectedAddressId = ref<number | null>(null)
const addressDialogVisible = ref(false)
const addressSaving = ref(false)

const addressForm = ref<UserAddress>({
  receiverName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  tag: '家',
  isDefault: false
})

const extraForm = ref({ remark: '' })

const addressRules: FormRules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: (_rule, value, cb) => (isValidPhone(value) ? cb() : cb(new Error('手机号格式不正确'))), trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const selectedPaymentMethod = ref(PaymentMethod.ALIPAY)
const paymentMethods = [
  { value: PaymentMethod.ALIPAY, label: '支付宝', description: '推荐使用支付宝安全快捷支付', icon: markRaw(Wallet) },
  { value: PaymentMethod.WECHAT, label: '微信支付', description: '使用微信钱包余额或银行卡支付', icon: markRaw(ChatDotRound) },
  { value: PaymentMethod.CREDIT_CARD, label: '信用卡支付', description: '支持主流银行信用卡支付', icon: markRaw(CreditCard) },
  { value: PaymentMethod.CASH, label: '货到付款', description: '送货上门后现金或刷卡支付', icon: markRaw(Van) }
]

const subtotal = computed(() => checkoutItems.value.reduce((total, item) => total + (item.product.price * item.quantity), 0))
const shippingFee = computed(() => (subtotal.value >= 99 ? 0 : 10))
const totalAmount = computed(() => subtotal.value + shippingFee.value)
const isFromBuyNow = computed(() => checkoutSourceType.value === 'buyNow')
const selectedAddress = computed(() => addressList.value.find(item => item.addressId === selectedAddressId.value) || null)

const formatFullAddress = (item: UserAddress) => [item.province, item.city, item.district, item.detailAddress].filter(Boolean).join(' ')

const loadCheckoutItems = () => {
  try {
    const checkoutType = sessionStorage.getItem('checkoutType')
    checkoutSourceType.value = checkoutType === 'buyNow' ? 'buyNow' : 'cart'

    if (checkoutType === 'buyNow') {
      const buyNowItem = sessionStorage.getItem('buyNowItem')
      if (buyNowItem) {
        const item = JSON.parse(buyNowItem)
        checkoutItems.value = [{
          cartItemId: 0,
          cartId: 0,
          productId: item.productId,
          quantity: item.quantity,
          addTime: new Date().toISOString(),
          product: {
            productId: item.productId,
            productName: item.productName,
            price: item.price,
            imageUrl: item.imageUrl || '',
            category: item.category,
            brand: item.brand,
            stockQuantity: 999,
            status: 'ACTIVE'
          }
        }]
      } else {
        ElMessage.warning('商品信息丢失，请重新选择')
        router.push('/')
      }
    } else {
      const savedItems = sessionStorage.getItem('checkoutItems')
      if (savedItems) {
        checkoutItems.value = JSON.parse(savedItems)
      } else {
        ElMessage.warning('没有要结算的商品，请先添加商品到购物车')
        router.push('/cart')
      }
    }
  } catch (error) {
    console.error('加载结算商品失败:', error)
    ElMessage.error('加载商品信息失败')
    router.push('/cart')
  }
}

const loadAddresses = async () => {
  try {
    const response = await addressApi.getList()
    addressList.value = response.data || []
    if (addressList.value.length) {
      const defaultAddress = addressList.value.find(item => item.isDefault)
      selectedAddressId.value = defaultAddress?.addressId || addressList.value[0].addressId || null
    } else {
      selectedAddressId.value = null
    }
  } catch (error: any) {
    console.error('加载地址失败:', error)
    ElMessage.error(error?.response?.data?.message || '加载地址失败')
  }
}

const openAddressDialog = () => {
  addressForm.value = {
    receiverName: '',
    contactPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    tag: '家',
    isDefault: addressList.value.length === 0
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  const valid = await addressFormRef.value?.validate().catch(() => false)
  if (!valid) return
  addressSaving.value = true
  try {
    const response = await addressApi.quickAddAddress(addressForm.value)
    ElMessage.success('地址已保存')
    addressDialogVisible.value = false
    await loadAddresses()
    if (response.data?.addressId) {
      selectedAddressId.value = response.data.addressId
    }
  } catch (error: any) {
    console.error('新增地址失败:', error)
    ElMessage.error(error?.response?.data?.message || '新增地址失败')
  } finally {
    addressSaving.value = false
  }
}

const setDefaultAddress = async (addressId: number) => {
  try {
    await addressApi.setDefault(addressId)
    ElMessage.success('默认地址已更新')
    await loadAddresses()
    selectedAddressId.value = addressId
  } catch (error: any) {
    console.error('设置默认地址失败:', error)
    ElMessage.error(error?.response?.data?.message || '设置默认地址失败')
  }
}

const removeAddress = async (addressId: number) => {
  try {
    await ElMessageBox.confirm('确定删除该地址吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await addressApi.deleteAddress(addressId)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error(error?.response?.data?.message || '删除地址失败')
    }
  }
}

const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请先选择或新增收货地址')
    return
  }

  try {
    await ElMessageBox.confirm(`确认提交订单？应付金额 ${formatPrice(totalAmount.value)}`, '确认订单', {
      confirmButtonText: '确认提交',
      cancelButtonText: '再想想',
      type: 'info'
    })

    submitting.value = true

    const fullAddress = formatFullAddress(selectedAddress.value)
    const orderItems: CheckoutItemPayload[] = checkoutItems.value.map(item => ({
      cartItemId: checkoutSourceType.value === 'cart' && item.cartItemId > 0 ? item.cartItemId : undefined,
      productId: item.productId || item.product.productId,
      quantity: item.quantity
    }))
    const orderData: CreateOrderRequest = {
      shippingAddress: fullAddress,
      contactPhone: selectedAddress.value.contactPhone,
      paymentMethod: selectedPaymentMethod.value,
      remark: extraForm.value.remark || undefined,
      items: orderItems
    }

    const response = await orderApi.createOrderFromCart(orderData)

    if (response.data) {
      createdOrder.value = response.data
      if (checkoutSourceType.value === 'buyNow') {
        sessionStorage.removeItem('buyNowItem')
        sessionStorage.removeItem('checkoutType')
      } else {
        sessionStorage.removeItem('checkoutItems')
        await cartStore.loadCartItems(true)
      }
      await startPaymentProcess()
    }
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('提交订单失败:', error)
    if (error.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error(error.message || '提交订单失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

const startPaymentProcess = async () => {
  if (!createdOrder.value) return

  if (selectedPaymentMethod.value === PaymentMethod.CASH) {
    ElMessage.success('订单提交成功！您选择了货到付款，我们会尽快为您安排发货')
    router.push('/orders')
    return
  }

  paymentDialogVisible.value = true
  currentStep.value = 0

  try {
    await sleep(1000)
    currentStep.value = 1
    await sleep(2000)
    await orderApi.payOrder(createdOrder.value.orderId)
    currentStep.value = 2
    await sleep(1000)
    ElMessage.success('支付成功！订单已提交，我们会尽快为您发货')
    paymentDialogVisible.value = false
    router.push('/orders')
  } catch (error: any) {
    console.error('支付失败:', error)
    paymentDialogVisible.value = false
    if (error.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('支付失败，订单已创建，您可以稍后在订单中心完成支付')
      router.push('/orders')
    }
  }
}

const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

const goToCart = () => router.push('/cart')
const goBack = () => router.push(isFromBuyNow.value ? '/' : '/cart')
const goToLogin = () => router.push('/login')

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  if (img.src.includes('data:image')) return
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTRweCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaaguaXoOWbvueJhzwvdGV4dD4KPC9zdmc+'
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    loadCheckoutItems()
    await loadAddresses()
  }
})
</script>

<style scoped>
.checkout-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.checkout-header {
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 2px solid #f1f3f4;
}

.checkout-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.checkout-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.not-logged-in,
.empty-checkout {
  text-align: center;
  padding: 80px 0;
}

.checkout-content {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
}

.card-header .el-button {
  margin-left: auto;
}

.address-card,
.payment-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.address-radio-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-option {
  border: 1px solid #dfe8e2;
  border-radius: 16px;
  padding: 12px 22px 20px;
  display: flex;
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.address-option.active {
  border-color: #2a5a44;
  background: linear-gradient(180deg, #f7fbf8 0%, #f2f8f4 100%);
  box-shadow: 0 10px 24px rgba(42, 90, 68, 0.08);
}

.address-radio-content {
  flex: 1;
  min-width: 0;
}

.address-radio-content :deep(.el-radio) {
  width: 100%;
  margin-right: 0;
  align-items: flex-start;
  height: auto;
}

.address-radio-content :deep(.el-radio__label) {
  display: block;
  width: 100%;
}

.address-main {
  margin-left: 8px;
  flex: 1;
  min-width: 0;
  padding-bottom: 2px;
}

.address-line {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 8px;
  margin-bottom: 6px;
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
}

.address-line strong,
.address-line span {
  flex-shrink: 0;
}

.address-line .el-tag {
  flex-shrink: 0;
}

.address-detail {
  margin: 0;
  padding-bottom: 2px;
  color: #5d645f;
  line-height: 1.65;
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.address-ops {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-shrink: 0;
  min-width: 168px;
}

.address-op-placeholder {
  visibility: hidden;
  display: inline-block;
  min-width: 72px;
}

.remark-panel {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #eef2ee;
}

.remark-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}

.remark-title {
  font-size: 15px;
  font-weight: 600;
  color: #31463d;
}

.remark-tip {
  font-size: 12px;
  color: #8a918d;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  border: 2px solid #e8ecef;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-option:hover {
  border-color: #2a5a44;
  box-shadow: 0 2px 8px rgba(42, 90, 68, 0.12);
}

.payment-option.active {
  border-color: #2a5a44;
  background: #f4faf6;
}

.payment-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.payment-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f3f4;
  border-radius: 8px;
  color: #666;
}

.payment-icon :deep(svg) {
  width: 24px;
  height: 24px;
}

.payment-info {
  flex: 1;
}

.payment-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.payment-info p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.payment-check {
  flex-shrink: 0;
}

.order-summary {
  position: sticky;
  top: 20px;
}

.summary-card {
  border-radius: 12px;
  border: 1px solid #e8ecef;
}

.product-list {
  margin-bottom: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f3f4;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-info h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-info p {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #999;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.product-price .price {
  color: #e74c3c;
  font-weight: 600;
}

.product-price .quantity {
  color: #666;
}

.product-total {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: #e74c3c;
}

.price-summary {
  margin: 16px 0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.price-row span:first-child {
  color: #666;
}

.price-row span:last-child {
  color: #333;
  font-weight: 500;
}

.price-row.total {
  font-size: 16px;
  font-weight: 600;
  margin-top: 8px;
}

.price-row.total .total-amount {
  color: #e74c3c;
  font-size: 18px;
}

.shipping {
  color: #27ae60 !important;
}

.submit-section {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submit-section .el-button {
  height: 52px;
  width: 80%;
  align-self: center;
  border-radius: 20px;
  margin: 0 !important;
  font-size: 17px;
  font-weight: 600;
}

.payment-progress {
  text-align: center;
}

.payment-steps {
  margin-bottom: 20px;
}

.payment-info {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  text-align: left;
}

.payment-info p {
  margin: 4px 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .checkout-page {
    padding: 10px;
  }

  .checkout-content .el-col {
    margin-bottom: 20px;
  }

  .order-summary {
    position: static;
    order: -1;
  }

  .payment-content {
    flex-wrap: wrap;
    gap: 12px;
  }

  .payment-info {
    flex-basis: 100%;
  }

  .checkout-header h1 {
    font-size: 22px;
  }

  .submit-section .el-button {
    width: 100%;
  }

  .address-option {
    flex-direction: column;
  }

  .address-radio-content,
  .address-ops {
    width: 100%;
  }

  .address-ops {
    min-width: 0;
    justify-content: flex-start;
  }

  .address-line,
  .address-detail {
    width: 100%;
  }
}
</style>
