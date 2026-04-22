/**
 * API连接测试工具
 * 用于测试前后端连接状态和API响应
 */

import { systemApi } from '@/api/system'
import { userApi } from '@/api/user'

// 测试结果接口
export interface TestResult {
  name: string
  success: boolean
  message: string
  duration: number
  data?: any
}

// API测试类
export class ApiTester {
  private results: TestResult[] = []

  // 测试系统健康状态
  async testSystemHealth(): Promise<TestResult> {
    const startTime = Date.now()
    const testName = '系统健康检查'
    
    try {
      const response = await systemApi.health()
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: true,
        message: '系统状态正常',
        duration,
        data: response.data
      }
      
      this.results.push(result)
      return result
    } catch (error: any) {
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: false,
        message: error.message || '系统健康检查失败',
        duration
      }
      
      this.results.push(result)
      return result
    }
  }

  // 测试系统信息获取
  async testSystemInfo(): Promise<TestResult> {
    const startTime = Date.now()
    const testName = '系统信息获取'
    
    try {
      const response = await systemApi.info()
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: true,
        message: '系统信息获取成功',
        duration,
        data: response.data
      }
      
      this.results.push(result)
      return result
    } catch (error: any) {
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: false,
        message: error.message || '系统信息获取失败',
        duration
      }
      
      this.results.push(result)
      return result
    }
  }

  // 测试用户数量统计
  async testUserCount(): Promise<TestResult> {
    const startTime = Date.now()
    const testName = '用户数量统计'
    
    try {
      const response = await userApi.getUserCount()
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: true,
        message: `用户总数: ${response.data}`,
        duration,
        data: response.data
      }
      
      this.results.push(result)
      return result
    } catch (error: any) {
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: false,
        message: error.message || '用户数量统计失败',
        duration
      }
      
      this.results.push(result)
      return result
    }
  }

  // 测试账号检查功能
  async testAccountCheck(account: string = 'test_account'): Promise<TestResult> {
    const startTime = Date.now()
    const testName = '账号检查功能'
    
    try {
      const response = await userApi.checkAccount(account)
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: true,
        message: `账号 ${account} ${response.data ? '已存在' : '不存在'}`,
        duration,
        data: response.data
      }
      
      this.results.push(result)
      return result
    } catch (error: any) {
      const duration = Date.now() - startTime
      
      const result: TestResult = {
        name: testName,
        success: false,
        message: error.message || '账号检查失败',
        duration
      }
      
      this.results.push(result)
      return result
    }
  }

  // 运行所有测试
  async runAllTests(): Promise<TestResult[]> {
    console.log('开始API连接测试...')
    this.results = []
    
    const tests = [
      () => this.testSystemHealth(),
      () => this.testSystemInfo(),
      () => this.testUserCount(),
      () => this.testAccountCheck()
    ]
    
    for (const test of tests) {
      try {
        await test()
      } catch (error) {
        console.error('测试执行出错:', error)
      }
    }
    
    this.printResults()
    return this.results
  }

  // 打印测试结果
  printResults(): void {
    console.log('\n=== API连接测试结果 ===')
    console.log(`总测试数: ${this.results.length}`)
    console.log(`成功: ${this.results.filter(r => r.success).length}`)
    console.log(`失败: ${this.results.filter(r => !r.success).length}`)
    console.log('\n详细结果:')
    
    this.results.forEach((result, index) => {
      const status = result.success ? '✅' : '❌'
      console.log(`${index + 1}. ${status} ${result.name}`)
      console.log(`   消息: ${result.message}`)
      console.log(`   耗时: ${result.duration}ms`)
      if (result.data) {
        console.log(`   数据:`, result.data)
      }
      console.log('')
    })
  }

  // 获取测试结果
  getResults(): TestResult[] {
    return this.results
  }

  // 清除测试结果
  clearResults(): void {
    this.results = []
  }

  // 获取成功率
  getSuccessRate(): number {
    if (this.results.length === 0) return 0
    const successCount = this.results.filter(r => r.success).length
    return (successCount / this.results.length) * 100
  }
}

// 导出默认实例
export const apiTester = new ApiTester()

// 快速测试函数
export const quickTest = async (): Promise<void> => {
  await apiTester.runAllTests()
  
  const successRate = apiTester.getSuccessRate()
  if (successRate === 100) {
    console.log('🎉 所有API测试通过！前后端连接正常。')
  } else if (successRate >= 50) {
    console.log('⚠️ 部分API测试失败，请检查后端服务状态。')
  } else {
    console.log('🚨 大部分API测试失败，请确认后端服务是否启动。')
  }
} 