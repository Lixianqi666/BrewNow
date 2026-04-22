<script setup lang="ts">
import { computed, useAttrs } from 'vue'

type ButtonVariant = 'primary' | 'default' | 'danger' | 'warning'

defineOptions({
  inheritAttrs: false
})

const props = withDefaults(defineProps<{
  variant?: ButtonVariant
}>(), {
  variant: undefined
})

const attrs = useAttrs()

const resolvedVariant = computed<ButtonVariant>(() => {
  if (props.variant) return props.variant

  const rawType = String(attrs.type || '')
  if (rawType === 'danger') return 'danger'
  if (rawType === 'warning') return 'warning'
  if (rawType === 'success' || rawType === 'primary') return 'primary'
  return 'default'
})

const forwardedAttrs = computed(() => attrs)

const mergedClass = computed(() => [
  'base-button',
  `base-button--${resolvedVariant.value}`,
  attrs.class
])
</script>

<template>
  <el-button v-bind="forwardedAttrs" :class="mergedClass" style="height: 32px; padding: 0 16px; line-height: 30px;">
    <slot />
  </el-button>
</template>
