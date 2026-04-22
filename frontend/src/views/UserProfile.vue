<template>
  <div class="user-profile">
    <el-card class="page-container" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
          <el-tag type="success">可编辑</el-tag>
        </div>
      </template>

      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else class="profile-content">
        <el-row :gutter="20">
          <el-col :xs="24" :md="8">
            <el-card shadow="never" class="profile-side-card">
              <div class="user-avatar">
                <el-avatar :size="90" :src="avatarPreview || undefined" />
                <h3>{{ profileForm.username || '未设置昵称' }}</h3>
                <p>{{ profileForm.email || '未设置邮箱' }}</p>
                <el-tag size="small">注册于 {{ profileForm.registerTime || '-' }}</el-tag>
                <div class="avatar-actions">
                  <input
                    ref="avatarInputRef"
                    type="file"
                    accept="image/*"
                    class="avatar-input"
                    @change="onAvatarFileChange"
                  />
                  <el-button size="small" @click="triggerAvatarSelect">上传头像</el-button>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :md="16">
            <el-card shadow="never" class="profile-main-card">
              <h3>基本信息</h3>
              <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="92px">
                <el-form-item label="账号" prop="account">
                  <el-input v-model="profileForm.account" disabled />
                </el-form-item>
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" placeholder="请输入用户名" maxlength="20" show-word-limit />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <el-card shadow="never" class="password-card">
              <h3>修改密码</h3>
              <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="92px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="changingPwd" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <el-card shadow="never" class="address-card">
              <div class="address-header">
                <h3>收货地址管理</h3>
                <el-button type="primary" @click="openAddressDialog()">新增地址</el-button>
              </div>
              <el-empty v-if="!addressList.length" description="暂无收货地址" />
              <div v-else class="address-list">
                <div v-for="item in addressList" :key="item.addressId" class="address-item">
                  <div class="address-top">
                    <div class="address-user">
                      <strong>{{ item.receiverName }}</strong>
                      <span>{{ item.contactPhone }}</span>
                      <el-tag v-if="item.tag" size="small">{{ item.tag }}</el-tag>
                    </div>
                    <div class="address-actions">
                      <el-button size="small" class="address-action-btn" @click="openAddressDialog(item)">
                        编辑
                      </el-button>
                      <span v-if="item.isDefault" class="address-default-slot">
                        <el-tag size="small" type="success" effect="light" class="address-default-tag">
                          默认
                        </el-tag>
                      </span>
                      <el-button
                        v-else
                        size="small"
                        type="primary"
                        class="address-action-btn address-action-btn--primary address-default-slot"
                        @click="setDefault(item.addressId!)"
                      >
                        设为默认
                      </el-button>
                      <el-button
                        size="small"
                        type="danger"
                        plain
                        class="address-action-btn address-action-btn--danger"
                        @click="removeAddress(item.addressId!)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                  <p class="address-detail" :title="formatFullAddress(item)">{{ formatFullAddress(item) }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-dialog v-model="addressDialogVisible" :title="editingAddressId ? '编辑地址' : '新增地址'" width="560px">
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
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi } from '@/api/user'
import { addressApi, type UserAddress } from '@/api/address'
import { useUserStore } from '@/stores/user'
import { formatDate, isValidEmail, isValidPhone } from '@/utils'

const userStore = useUserStore()

const loading = ref(true)
const saving = ref(false)
const changingPwd = ref(false)
const currentUserId = ref<number | null>(null)

const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const avatarInputRef = ref<HTMLInputElement | null>(null)
const avatarPreview = ref('')

const addressFormRef = ref<FormInstance>()
const addressDialogVisible = ref(false)
const editingAddressId = ref<number | null>(null)
const addressSaving = ref(false)
const addressList = ref<UserAddress[]>([])

const profileForm = reactive({
  userId: 0,
  account: '',
  username: '',
  phone: '',
  email: '',
  address: '',
  registerTime: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const addressForm = reactive<UserAddress>({
  receiverName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  tag: '家',
  isDefault: false
})

const profileRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度应为2-20位', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback()
        if (!isValidPhone(value)) return callback(new Error('请输入正确手机号'))
        callback()
      },
      trigger: 'blur'
    }
  ],
  email: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback()
        if (!isValidEmail(value)) return callback(new Error('请输入正确邮箱'))
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度应为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback(new Error('请确认新密码'))
        if (value !== passwordForm.newPassword) return callback(new Error('两次密码不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
}

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

const parseUserIdFromToken = (token?: string | null): number | null => {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = JSON.parse(atob(parts[1]))
    return payload?.userId ?? null
  } catch {
    return null
  }
}

const loadAddresses = async () => {
  try {
    const response = await addressApi.getList()
    addressList.value = response.data || []
  } catch (error: any) {
    console.error('加载地址失败:', error)
    ElMessage.error(error?.response?.data?.message || '加载地址失败')
  }
}

const loadUserProfile = async () => {
  loading.value = true
  try {
    const parsedUserId = parseUserIdFromToken(userStore.token) || userStore.userInfo?.userId || null
    if (!parsedUserId) {
      ElMessage.error('未识别到用户信息，请重新登录')
      return
    }

    currentUserId.value = parsedUserId
    const response = await userApi.getUserById(parsedUserId)
    const user = response.data
    if (!user) {
      ElMessage.error('获取用户信息失败')
      return
    }

    profileForm.userId = user.userId || parsedUserId
    profileForm.account = user.account || ''
    profileForm.username = user.username || ''
    profileForm.phone = user.phone || ''
    profileForm.email = user.email || ''
    profileForm.address = user.address || ''
    profileForm.registerTime = user.registerTime ? formatDate(user.registerTime) : ''
    avatarPreview.value = user.avatarUrl || userStore.userInfo?.avatarUrl || ''
    userStore.updateUserInfo({ avatarUrl: avatarPreview.value })
    await loadAddresses()
  } catch (error: any) {
    console.error('加载用户信息失败:', error)
    ElMessage.error(error?.response?.data?.message || '加载用户信息失败')
  } finally {
    loading.value = false
  }
}

const triggerAvatarSelect = () => {
  avatarInputRef.value?.click()
}

const onAvatarFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  const previousAvatar = avatarPreview.value

  if (!file.type.startsWith('image/')) {
    ElMessage.error('仅支持图片格式')
    target.value = ''
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像大小不能超过 2MB')
    target.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    avatarPreview.value = String(reader.result || '')
  }
  reader.readAsDataURL(file)

  userApi.uploadAvatar(file).then((response) => {
    const avatarUrl = response.data?.avatarUrl || ''
    if (!avatarUrl) {
      ElMessage.error('头像上传失败，请重试')
      return
    }

    avatarPreview.value = avatarUrl
    userStore.updateUserInfo({ avatarUrl })
    ElMessage.success('头像已更新')
    target.value = ''
  }).catch((error: any) => {
    console.error('头像上传失败:', error)
    avatarPreview.value = previousAvatar
    ElMessage.error(error?.response?.data?.message || '头像上传失败')
    target.value = ''
  })
}

const saveProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid || !currentUserId.value) return

  saving.value = true
  try {
    await userApi.updateUser({
      userId: currentUserId.value,
      username: profileForm.username,
      phone: profileForm.phone,
      email: profileForm.email,
      address: profileForm.address,
      account: profileForm.account
    })

    userStore.updateUserInfo({
      username: profileForm.username,
      phone: profileForm.phone,
      email: profileForm.email,
      address: profileForm.address
    })

    ElMessage.success('个人信息已更新')
  } catch (error: any) {
    console.error('更新用户信息失败:', error)
    ElMessage.error(error?.response?.data?.message || '更新失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid || !currentUserId.value) return

  changingPwd.value = true
  try {
    await userApi.changePassword({
      userId: currentUserId.value,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功，请妥善保存')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.clearValidate()
  } catch (error: any) {
    console.error('修改密码失败:', error)
    ElMessage.error(error?.response?.data?.message || '密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

const openAddressDialog = (address?: UserAddress) => {
  if (address) {
    editingAddressId.value = address.addressId || null
    Object.assign(addressForm, {
      receiverName: address.receiverName,
      contactPhone: address.contactPhone,
      province: address.province || '',
      city: address.city || '',
      district: address.district || '',
      detailAddress: address.detailAddress,
      tag: address.tag || '家',
      isDefault: !!address.isDefault
    })
  } else {
    editingAddressId.value = null
    Object.assign(addressForm, {
      receiverName: '',
      contactPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      tag: '家',
      isDefault: addressList.value.length === 0
    })
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  const valid = await addressFormRef.value?.validate().catch(() => false)
  if (!valid) return

  addressSaving.value = true
  try {
    if (editingAddressId.value) {
      await addressApi.updateAddress({
        addressId: editingAddressId.value,
        ...addressForm
      })
      ElMessage.success('地址更新成功')
    } else {
      await addressApi.addAddress({ ...addressForm })
      ElMessage.success('地址新增成功')
    }
    addressDialogVisible.value = false
    await loadAddresses()
  } catch (error: any) {
    console.error('保存地址失败:', error)
    ElMessage.error(error?.response?.data?.message || '保存地址失败')
  } finally {
    addressSaving.value = false
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

const setDefault = async (addressId: number) => {
  try {
    await addressApi.setDefault(addressId)
    ElMessage.success('默认地址已更新')
    await loadAddresses()
  } catch (error: any) {
    console.error('设置默认地址失败:', error)
    ElMessage.error(error?.response?.data?.message || '设置默认地址失败')
  }
}

const formatFullAddress = (item: UserAddress) => {
  return [item.province, item.city, item.district, item.detailAddress].filter(Boolean).join(' ')
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
  background: transparent;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 22px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
  color: #173a2f;
}

.loading-wrap {
  padding: 20px;
}

.profile-side-card,
.profile-main-card,
.password-card,
.address-card {
  border-radius: 18px;
  margin-bottom: 16px;
}

.user-avatar {
  text-align: center;
  padding: 20px;
}

.user-avatar h3 {
  margin: 15px 0 5px 0;
  color: #173a2f;
}

.user-avatar p {
  margin: 0 0 10px;
  color: #6f6759;
}

.avatar-actions {
  margin-top: 14px;
}

.avatar-input {
  display: none;
}

.profile-main-card h3,
.password-card h3,
.address-header h3 {
  margin: 0 0 12px;
  color: #173a2f;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  border: 1px solid #e8dfd0;
  border-radius: 12px;
  padding: 12px;
  background: #fffcf6;
}

.address-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.address-user {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #173a2f;
}

.address-item p {
  margin: 8px 0 0;
  color: #6f6759;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.address-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.address-action-btn {
  min-width: 72px;
  height: 30px;
  border-radius: 999px;
  font-weight: 600;
  padding: 0 12px;
}

.address-action-btn--primary.el-button--primary {
  --el-button-text-color: #ffffff;
  --el-button-hover-text-color: #ffffff;
  --el-button-active-text-color: #ffffff;
  --el-button-bg-color: var(--el-color-primary);
  --el-button-border-color: var(--el-color-primary);
  --el-button-hover-bg-color: var(--el-color-primary-light-3);
  --el-button-hover-border-color: var(--el-color-primary-light-3);
  --el-button-active-bg-color: var(--el-color-primary-dark-2);
  --el-button-active-border-color: var(--el-color-primary-dark-2);
}

.address-action-btn--danger {
  --el-button-hover-bg-color: #fff1f0;
  --el-button-hover-border-color: #ffb0ae;
}

.address-default-slot {
  min-width: 72px;
  display: inline-flex;
  justify-content: center;
}

.address-default-tag {
  min-width: 72px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-weight: 600;
}

.address-detail {
  margin: 8px 0 0;
  color: #6f6759;
  width: min(100%, 460px);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 768px) {
  .user-profile {
    padding: 10px;
  }

  .address-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .address-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
