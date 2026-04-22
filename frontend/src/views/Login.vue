<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <img src="@/assets/logo.svg" alt="沏刻" class="logo-icon" />
            <h1>BrewNow 沏刻茶叶电商平台</h1>
          </div>
          <p class="subtitle">BrewNow 沏刻茶叶电商平台</p>
        </div>

        <!-- 用户类型选择 -->
        <div class="user-type-selector">
          <el-radio-group v-model="userType" size="large" @change="onUserTypeChange">
            <el-radio-button value="consumer">消费者</el-radio-button>
            <el-radio-button value="merchant">商家</el-radio-button>
            <el-radio-button value="admin">管理员</el-radio-button>
          </el-radio-group>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs">
          <!-- 登录表单 -->
          <el-tab-pane label="登录" name="login">
            <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="login-form"
              @keyup.enter="handleLogin"
            >
              <!-- 消费者登录 -->
              <template v-if="userType === 'consumer'">
                <el-form-item prop="account">
                  <el-input
                    v-model="loginForm.account"
                    placeholder="账号 / 用户名 / 手机号 / 邮箱"
                    :prefix-icon="User"
                    clearable
                    size="large"
                  />
                </el-form-item>
              </template>

              <!-- 商家登录 -->
              <template v-if="userType === 'merchant'">
                <el-form-item prop="merchantAccount">
                  <el-input
                    v-model="loginForm.merchantAccount"
                    placeholder="品牌账号 / 商家ID"
                    :prefix-icon="Shop"
                    clearable
                    size="large"
                  />
                </el-form-item>
              </template>

              <!-- 管理员登录 -->
              <template v-if="userType === 'admin'">
                <el-form-item prop="username">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="管理员账号"
                    :prefix-icon="User"
                    clearable
                    size="large"
                  />
                </el-form-item>
              </template>

              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="密码"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item>
                <div class="form-options">
                  <el-checkbox v-model="loginForm.rememberMe">
                    记住密码
                  </el-checkbox>
                  <el-link type="primary" @click="showForgotPassword">
                    忘记密码？
                  </el-link>
                </div>
              </el-form-item>

              <el-form-item>
                <BaseButton
                  type="primary"
                  size="large"
                  @click="handleLogin"
                  :loading="loginLoading"
                  block
                >
                  立即登录
                </BaseButton>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 注册表单 -->
          <el-tab-pane :label="getRegisterLabel" name="register" v-if="userType !== 'admin'">
            <el-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              class="register-form"
              @keyup.enter="handleRegister"
            >
              <!-- 基础用户信息 -->
              <el-form-item prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="用户名（3-20个字符）"
                  :prefix-icon="User"
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item prop="phone">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="手机号"
                  :prefix-icon="Phone"
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item prop="email">
                <el-input
                  v-model="registerForm.email"
                  placeholder="邮箱地址"
                  :prefix-icon="Message"
                  clearable
                  size="large"
                />
              </el-form-item>

              <!-- 商家特有字段 -->
              <template v-if="userType === 'merchant'">
                <el-form-item prop="merchantId">
                  <el-input
                    v-model="registerForm.merchantId"
                    placeholder="商家ID（唯一标识）"
                    :prefix-icon="Shop"
                    clearable
                    size="large"
                  />
                </el-form-item>

                <el-form-item prop="companyName">
                  <el-input
                    v-model="registerForm.companyName"
                    placeholder="企业名称"
                    :prefix-icon="Shop"
                    clearable
                    size="large"
                  />
                </el-form-item>

                <el-form-item prop="contactPerson">
                  <el-input
                    v-model="registerForm.contactPerson"
                    placeholder="联系人姓名"
                    :prefix-icon="User"
                    clearable
                    size="large"
                  />
                </el-form-item>

                <el-form-item prop="businessAddress">
                  <el-input
                    v-model="registerForm.businessAddress"
                    placeholder="经营地址"
                    :prefix-icon="Location"
                    clearable
                    size="large"
                    type="textarea"
                    :rows="2"
                  />
                </el-form-item>
              </template>

              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="密码（6-20位，包含数字和字母）"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                  size="large"
                />
              </el-form-item>

              <el-form-item prop="agreement">
                <el-checkbox v-model="registerForm.agreement">
                  我已阅读并同意
                  <el-link type="primary" @click="showTerms">《用户协议》</el-link>
                  和
                  <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
                </el-checkbox>
              </el-form-item>

              <el-form-item>
                <BaseButton
                  type="primary"
                  size="large"
                  @click="handleRegister"
                  :loading="registerLoading"
                  block
                >
                  {{ userType === 'merchant' ? '提交商家申请' : '立即注册' }}
                </BaseButton>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPasswordVisible"
      title="重置密码"
      width="400px"
      :before-close="closeForgotPassword"
    >
      <el-form
        ref="forgotFormRef"
        :model="forgotForm"
        :rules="forgotRules"
        label-width="80px"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="forgotForm.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <BaseButton @click="closeForgotPassword">取消</BaseButton>
          <BaseButton
            type="primary"
            @click="sendResetEmail"
            :loading="resetEmailLoading"
          >
            发送重置邮件
          </BaseButton>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import {
  User,
  Lock,
  Message,
  Phone,
  Shop,
  Location
} from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { isValidEmail, isValidPhone, isValidPassword } from '@/utils'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const userType = ref<'consumer' | 'merchant' | 'admin'>('consumer')
const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const resetEmailLoading = ref(false)
const forgotPasswordVisible = ref(false)

// 表单引用
const loginFormRef = ref()
const registerFormRef = ref()
const forgotFormRef = ref()

// 登录表单
const loginForm = reactive({
  account: '',
  merchantAccount: '',
  username: '',
  password: '',
  rememberMe: false
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  merchantId: '',
  companyName: '',
  contactPerson: '',
  businessAddress: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 忘记密码表单
const forgotForm = reactive({
  email: ''
})

// 计算属性
const getRegisterLabel = computed(() => {
  return userType.value === 'merchant' ? '商家申请' : '注册'
})

// 登录验证规则
const loginRules = computed(() => {
  const baseRules = {
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ]
  }

  if (userType.value === 'consumer') {
    return {
      ...baseRules,
      account: [
        { required: true, message: '请输入账号/用户名/手机号/邮箱', trigger: 'blur' },
        { min: 3, message: '账号长度至少3个字符', trigger: 'blur' }
      ]
    }
  } else if (userType.value === 'merchant') {
    return {
      ...baseRules,
      merchantAccount: [
        { required: true, message: '请输入商家账号', trigger: 'blur' },
        { min: 3, message: '商家账号长度至少3个字符', trigger: 'blur' }
      ]
    }
  } else { // admin
    return {
      ...baseRules,
      username: [
        { required: true, message: '请输入管理员账号', trigger: 'blur' },
        { min: 3, message: '账号长度至少3个字符', trigger: 'blur' }
      ]
    }
  }
})

// 注册验证规则
const registerRules = computed(() => {
  const baseRules = {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱地址', trigger: 'blur' },
      { validator: (rule: any, value: string, callback: Function) => {
        if (!isValidEmail(value)) {
          callback(new Error('请输入正确的邮箱格式'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { validator: (rule: any, value: string, callback: Function) => {
        if (!isValidPhone(value)) {
          callback(new Error('请输入正确的手机号格式'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { validator: (rule: any, value: string, callback: Function) => {
        if (!isValidPassword(value)) {
          callback(new Error('密码必须包含数字和字母，长度6-20位'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      { validator: (rule: any, value: string, callback: Function) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
    ],
    agreement: [
      { validator: (rule: any, value: boolean, callback: Function) => {
        if (!value) {
          callback(new Error('请阅读并同意用户协议'))
        } else {
          callback()
        }
      }, trigger: 'change' }
    ]
  }

  // 商家注册额外规则
  if (userType.value === 'merchant') {
    return {
      ...baseRules,
      merchantId: [
        { required: true, message: '请输入商家ID', trigger: 'blur' },
        { min: 3, max: 50, message: '商家ID长度在3到50个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_-]+$/, message: '商家ID只能包含字母、数字、下划线和中划线', trigger: 'blur' }
      ],
      companyName: [
        { required: true, message: '请输入企业名称', trigger: 'blur' },
        { min: 2, max: 100, message: '企业名称长度在2到100个字符', trigger: 'blur' }
      ],
      contactPerson: [
        { required: true, message: '请输入联系人姓名', trigger: 'blur' },
        { min: 2, max: 50, message: '联系人姓名长度在2到50个字符', trigger: 'blur' }
      ],
      businessAddress: [
        { required: true, message: '请输入经营地址', trigger: 'blur' },
        { min: 5, max: 200, message: '经营地址长度在5到200个字符', trigger: 'blur' }
      ]
    }
  }

  return baseRules
})

// 忘记密码验证规则
const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { validator: (rule: any, value: string, callback: Function) => {
      if (!isValidEmail(value)) {
        callback(new Error('请输入正确的邮箱格式'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const parseUserIdFromToken = (token: string): number | null => {
  try {
    const tokenParts = token.split('.')
    if (tokenParts.length !== 3) return null
    const payload = JSON.parse(atob(tokenParts[1]))
    return payload?.userId ?? null
  } catch {
    return null
  }
}

// 用户类型切换
const onUserTypeChange = () => {
  // 重置表单
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()

  // 清空表单数据
  Object.assign(loginForm, {
    account: '',
    merchantAccount: '',
    username: '',
    password: '',
    rememberMe: false
  })

  Object.assign(registerForm, {
    username: '',
    email: '',
    phone: '',
    merchantId: '',
    companyName: '',
    contactPerson: '',
    businessAddress: '',
    password: '',
    confirmPassword: '',
    agreement: false
  })

  // 管理员只能登录，不能注册
  if (userType.value === 'admin' && activeTab.value === 'register') {
    activeTab.value = 'login'
  }
}

// 处理登录
const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()

    loginLoading.value = true
    console.log('开始登录流程 - 用户类型:', userType.value)

    let response

    if (userType.value === 'consumer') {
      console.log('消费者登录 - 账号:', loginForm.account)
      response = await userApi.login({
        account: loginForm.account,
        password: loginForm.password
      })
    } else if (userType.value === 'merchant') {
      console.log('商家登录 - 商家账号:', loginForm.merchantAccount)
      response = await userApi.merchantLogin({
        account: loginForm.merchantAccount,
        password: loginForm.password
      })
    } else { // admin
      console.log('管理员登录 - 用户名:', loginForm.username)
      response = await userApi.adminLogin({
        username: loginForm.username,
        password: loginForm.password
      })
    }
    
    console.log('登录API响应:', response)

    if (response.code === 200) {
      console.log('登录API调用成功，开始保存状态')
      
      ElNotification({
        title: '登录成功',
        message: `欢迎回来！`,
        type: 'success'
      })

      // 使用userStore保存登录状态
      let token: string
      let userInfo: any = undefined
      let merchantInfo: any = undefined

      if (userType.value === 'consumer') {
        // 消费者登录返回 LoginResponse 结构
        token = (response.data as any)?.token || response.data
        userInfo = (response.data as any)?.userInfo
      } else if (userType.value === 'merchant') {
        const payload = response.data as any
        token = typeof payload === 'string' ? payload : payload?.token || ''
        merchantInfo = typeof payload === 'string' ? { merchantId: loginForm.merchantAccount } : payload?.merchantInfo
        if (!merchantInfo) {
          merchantInfo = { merchantId: loginForm.merchantAccount }
        }
      } else {
        // 商家和管理员登录直接返回 token 字符串
        token = response.data as string
      }

      if (!userInfo && (userType.value === 'consumer' || userType.value === 'merchant')) {
        const userId = parseUserIdFromToken(token)
        if (userId) {
          try {
            const profileResp = await userApi.getUserById(userId)
            userInfo = profileResp.data
          } catch (profileError) {
            console.warn('登录后补充用户信息失败:', profileError)
          }
        }
      }
      
      console.log('准备保存用户状态 - token:', token?.substring(0, 10) + '...')

      userStore.login({
        token: token,
        userType: userType.value,
        userInfo: userInfo,
        merchantInfo,
        adminInfo: userType.value === 'admin' ? { username: loginForm.username } : undefined
      })
      
      console.log('用户状态保存完成，准备跳转')

      // 跳转到目标页面或根据用户类型的默认页面
      let redirectPath: string

      if (route.query.redirect) {
        // 如果有重定向参数，使用重定向地址
        redirectPath = route.query.redirect as string
        console.log('使用重定向地址:', redirectPath)
      } else {
        // 根据用户类型跳转到对应的默认页面
        switch (userType.value) {
          case 'consumer':
            redirectPath = '/'  // 消费者跳转到首页
            break
          case 'merchant':
            redirectPath = '/merchant'  // 商家跳转到商家后台
            console.log('商家登录，将跳转到: /merchant')
            break
          case 'admin':
            redirectPath = '/admin'  // 管理员跳转到管理后台
            break
          default:
            redirectPath = '/'
        }
      }
      
      console.log('准备跳转到:', redirectPath, '用户类型:', userType.value)

      // 使用try-catch包装路由跳转，捕获可能的错误
      try {
        router.push(redirectPath)
        console.log('跳转指令已发送!')
      } catch (routeError) {
        console.error('路由跳转失败:', routeError)
        // 备选方案：如果路由跳转失败，尝试使用window.location
        setTimeout(() => {
          console.log('尝试使用window.location跳转...')
          window.location.href = redirectPath
        }, 500)
      }
    } else {
      console.error('登录失败 - 错误代码:', response.code, '错误信息:', response.message)
      ElMessage.error(response.message || '登录失败')
    }

  } catch (error: any) {
    console.error('登录异常:', error)
    
    if (error.errors) {
      // 表单验证错误
      console.log('表单验证错误，不显示提示')
      return
    }

    // 根据错误信息提供针对性的提示
    let errorMessage = '登录失败，请稍后重试'
    let errorDetails = ''

    if (error.response?.data?.message) {
      const serverMessage = error.response.data.message
      console.log('服务器错误信息:', serverMessage)
      
      if (serverMessage.includes('账号不存在')) {
        errorMessage = '账号不存在'
        errorDetails = userType.value === 'consumer'
          ? '请检查手机号是否正确，或先注册账号'
          : userType.value === 'merchant'
          ? '请检查商家ID是否正确'
          : '请检查用户名是否正确'
      } else if (serverMessage.includes('密码错误')) {
        errorMessage = '密码错误'
        errorDetails = '请检查密码是否正确，或使用忘记密码功能'
      } else if (serverMessage.includes('待审核')) {
        errorMessage = '商家账号待审核'
        errorDetails = '您的商家申请正在审核中，请耐心等待管理员审核'
      } else if (serverMessage.includes('被拒绝')) {
        errorMessage = '商家账号被拒绝'
        errorDetails = '您的商家申请被拒绝，请联系管理员了解详情'
      } else if (serverMessage.includes('被暂停')) {
        errorMessage = '账号被暂停'
        errorDetails = '您的账号已被暂停，请联系管理员'
      } else {
        errorMessage = serverMessage
      }
    } else if (error.message?.includes('Network Error') || error.code === 'NETWORK_ERROR') {
      errorMessage = '网络连接失败'
      errorDetails = '请检查网络连接，确保后端服务正常运行'
    }

    // 显示错误提示
    ElNotification({
      title: errorMessage,
      message: errorDetails,
      type: 'error',
      duration: 5000
    })

  } finally {
    loginLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()

    registerLoading.value = true

    let response

    if (userType.value === 'consumer') {
      response = await userApi.register({
        account: registerForm.username,
        username: registerForm.username,
        email: registerForm.email,
        phone: registerForm.phone,
        password: registerForm.password
      })
    } else if (userType.value === 'merchant') {
      response = await userApi.registerMerchant({
        username: registerForm.username,
        phone: registerForm.phone,
        email: registerForm.email,
        password: registerForm.password,
        merchantId: registerForm.merchantId,
        companyName: registerForm.companyName,
        contactPerson: registerForm.contactPerson,
        contactPhone: registerForm.phone,
        businessAddress: registerForm.businessAddress
      })
    }

    if (response?.code === 200) {
      ElNotification({
        title: userType.value === 'merchant' ? '申请提交成功' : '注册成功',
        message: userType.value === 'merchant'
          ? '商家申请已提交，请等待管理员审核'
          : '注册成功！请登录您的账户',
        type: 'success'
      })

      // 切换到登录页面
      activeTab.value = 'login'

      // 重置注册表单
      registerFormRef.value?.resetFields()
    } else {
      ElMessage.error(response?.message || '注册失败')
    }

  } catch (error: any) {
    console.error('注册失败:', error)
    if (error.errors) {
      // 表单验证错误
      return
    }
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}

// 显示忘记密码对话框
const showForgotPassword = () => {
  forgotPasswordVisible.value = true
}

// 关闭忘记密码对话框
const closeForgotPassword = () => {
  forgotPasswordVisible.value = false
  forgotFormRef.value?.resetFields()
}

// 发送重置邮件
const sendResetEmail = async () => {
  try {
    await forgotFormRef.value?.validate()

    resetEmailLoading.value = true

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))

    ElNotification({
      title: '邮件已发送',
      message: '重置密码邮件已发送到您的邮箱，请查收',
      type: 'success'
    })

    closeForgotPassword()

  } catch (error: any) {
    console.error('发送重置邮件失败:', error)
    if (error.errors) {
      return
    }
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    resetEmailLoading.value = false
  }
}

// 显示条款
const showTerms = () => {
  ElMessage.info('用户协议页面开发中...')
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessage.info('隐私政策页面开发中...')
}

// 组件挂载时检查登录状态
onMounted(() => {
  console.log('Login.vue onMounted - 检查登录状态')
  console.log('userStore.isLoggedIn:', userStore.isLoggedIn)
  console.log('localStorage token:', localStorage.getItem('token'))

  // 只记录状态，不进行跳转，避免与handleLogin和路由守卫冲突
  if (userStore.isLoggedIn) {
    console.log('用户已登录，但不在此处跳转，由路由守卫统一处理')
  } else {
    console.log('用户未登录，显示登录页面')
  }
})
</script>

<style scoped>
.login-page {
  --tea-green-900: #1f4d3b;
  --tea-green-800: #2d6b52;
  --tea-green-700: #3c8062;
  --tea-green-600: #4d9773;
  --tea-green-100: #e8f4ed;
  --tea-green-050: #f3faf6;

  min-height: calc(100vh - 68px);
  width: 100%;
  background: linear-gradient(135deg, var(--tea-green-800) 0%, var(--tea-green-900) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 80%, rgba(60, 128, 98, 0.28) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(45, 107, 82, 0.22) 0%, transparent 50%);
  pointer-events: none;
}

.login-container {
  width: 100%;
  max-width: 550px;
  position: relative;
  z-index: 1;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow:
    0 25px 50px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: transform 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
}

.login-header {
  text-align: center;
  padding: 40px 30px 30px;
  background: linear-gradient(135deg, var(--tea-green-700), var(--tea-green-900));
  color: white;
  position: relative;
}

.login-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 70% 80%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 15px;
  position: relative;
  z-index: 1;
}

.logo h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo .logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
  transition: transform 0.3s ease;
}

.logo:hover .logo-icon {
  transform: scale(1.08);
}

.subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 15px;
  font-weight: 400;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

.user-type-selector {
  padding: 25px 30px 20px;
  text-align: center;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
}

.user-type-selector .el-radio-group {
  width: 100%;
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-type-selector :deep(.el-radio-button) {
  flex: 1;
  margin: 0;
}

.user-type-selector :deep(.el-radio-button__inner) {
  width: 100%;
  border: none;
  border-radius: 0;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 14px;
  background: #ffffff;
  color: #606266;
  transition: all 0.3s ease;
  position: relative;
}

.user-type-selector :deep(.el-radio-button__inner:hover) {
  background: var(--tea-green-050);
  color: var(--tea-green-800);
}

.user-type-selector :deep(.el-radio-button.is-active .el-radio-button__inner) {
  background: linear-gradient(135deg, var(--tea-green-700), var(--tea-green-900));
  color: white;
  box-shadow: 0 2px 8px rgba(31, 77, 59, 0.32);
}

.user-type-selector :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 12px 0 0 12px;
}

.user-type-selector :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 12px 12px 0;
}

.login-tabs {
  padding: 20px 30px 30px;
  background: white;
}

.login-tabs :deep(.el-tabs__header) {
  margin: 0 0 25px 0;
  border-bottom: 2px solid #f0f2f7;
}

.login-tabs :deep(.el-tabs__content) {
  background: white;
  padding-bottom: 0;
}

.login-tabs :deep(.el-tab-pane) {
  background: white;
}

.login-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.login-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  padding: 12px 20px;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s ease;
}

.login-tabs :deep(.el-tabs__item:hover) {
  color: var(--tea-green-800);
}

.login-tabs :deep(.el-tabs__item.is-active) {
  color: var(--tea-green-800);
  background: transparent;
}

.login-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, var(--tea-green-700), var(--tea-green-900));
  height: 3px;
  border-radius: 2px;
}

.login-form,
.register-form {
  margin-top: 20px;
}

.login-form :deep(.el-form-item),
.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input),
.register-form :deep(.el-input) {
  border-radius: 10px;
}

.login-form :deep(.el-input__wrapper),
.register-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover),
.register-form :deep(.el-input__wrapper:hover) {
  border-color: var(--tea-green-700);
  box-shadow: 0 2px 12px rgba(45, 107, 82, 0.16);
}

.login-form :deep(.el-input.is-focus .el-input__wrapper),
.register-form :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: var(--tea-green-800);
  box-shadow: 0 2px 12px rgba(31, 77, 59, 0.24);
}

.login-form :deep(.el-button),
.register-form :deep(.el-button) {
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.login-form :deep(.el-button--primary),
.register-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, var(--tea-green-700), var(--tea-green-900));
  border: none;
  box-shadow: 0 4px 15px rgba(31, 77, 59, 0.32);
}

.login-form :deep(.el-button--primary:hover),
.register-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, var(--tea-green-600), var(--tea-green-800));
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(31, 77, 59, 0.38);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin: 15px 0;
}

.form-options :deep(.el-checkbox__label) {
  color: #606266;
  font-size: 14px;
}

.form-options :deep(.el-link) {
  font-size: 14px;
  font-weight: 500;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (min-width: 1200px) {
  .login-container {
    max-width: 650px;
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .login-container {
    max-width: 550px;
  }
}

@media (min-width: 481px) and (max-width: 767px) {
  .login-container {
    max-width: 500px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 15px;
  }

  .login-container {
    max-width: 100%;
  }

  .login-card {
    border-radius: 16px;
  }

  .login-header {
    padding: 30px 20px 25px;
  }

  .user-type-selector,
  .login-tabs {
    padding: 20px 20px 25px;
  }

  .logo h1 {
    font-size: 28px;
  }

  .logo .el-icon {
    font-size: 32px;
  }

  .form-options {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .user-type-selector :deep(.el-radio-button__inner) {
    padding: 10px 12px;
    font-size: 13px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-card {
  animation: fadeInUp 0.6s ease-out;
}
</style>
