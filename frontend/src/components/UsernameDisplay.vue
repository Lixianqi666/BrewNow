<template>
  <span>{{ displayName }}</span>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '@/api'

const props = defineProps<{
  userId: number
}>()

const displayName = ref('加载中...')

onMounted(async () => {
  await loadUsername()
})

const loadUsername = async () => {
  if (!props.userId) {
    displayName.value = '未关联'
    return
  }
  
  try {
    const response = await userApi.getUserById(props.userId)
    if (response.code === 200 && response.data) {
      displayName.value = response.data.username
    } else {
      displayName.value = '未知用户'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    displayName.value = '未知用户'
  }
}
</script> 