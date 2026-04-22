<script setup lang="ts">
import { computed } from 'vue'
import { resolveProductImageUrl } from '@/utils/productImageResolver'
import { formatPrice } from '@/utils'
import type { Product } from '@/api/product'

const props = withDefaults(defineProps<{
  product: Product
  description?: string
  metaTag?: string
  metaText?: string
  showMeta?: boolean
  showActions?: boolean
  showPrice?: boolean
  showAddToCart?: boolean
  showBuyNow?: boolean
  clickable?: boolean
}>(), {
  description: '',
  metaTag: '',
  metaText: '',
  showMeta: true,
  showActions: true,
  showPrice: true,
  showAddToCart: true,
  showBuyNow: true,
  clickable: true
})

const emit = defineEmits<{
  select: [productId: number]
  addCart: [product: Product]
  buyNow: [product: Product]
}>()

const imageUrl = computed(() => resolveProductImageUrl(props.product.productName, props.product.imageUrl))
const hasMeta = computed(() => props.showMeta && (props.metaTag || props.metaText || false))
const handleSelect = () => {
  if (!props.clickable || !props.product.productId) return
  emit('select', props.product.productId)
}
</script>

<template>
  <el-card class="product-card clickable-card" shadow="hover" @click="handleSelect">
    <div class="product-image">
      <img :src="imageUrl" :alt="product.productName" />
    </div>
    <div class="product-info">
      <div class="product-content">
        <div v-if="hasMeta" class="product-meta">
          <div class="product-meta-main">
            <el-tag v-if="metaTag" size="small" type="success" effect="plain">
              {{ metaTag }}
            </el-tag>
            <span v-if="metaText" class="product-meta-text">{{ metaText }}</span>
          </div>
          <div class="product-meta-action" @click.stop>
            <slot name="meta-action" />
          </div>
        </div>
        <h4 class="product-name">{{ product.productName }}</h4>
        <p v-if="description" class="product-description">{{ description }}</p>
      </div>
      <div class="product-bottom">
        <div v-if="showPrice" class="product-price">
          <span class="current-price">{{ formatPrice(product.price) }}</span>
        </div>
        <div v-if="showActions" class="product-actions">
          <el-button v-if="showAddToCart" type="primary" size="small" @click.stop="emit('addCart', product)">
            加入购物车
          </el-button>
          <el-button v-if="showBuyNow" type="success" size="small" @click.stop="emit('buyNow', product)">
            立即购买
          </el-button>
        </div>
        <slot name="footer" />
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.product-card {
  width: 100%;
  height: 100%;
  min-height: 460px;
  border-radius: 22px;
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff, #faf5eb);
  border: 1px solid rgba(186, 151, 96, 0.2);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.product-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
}

.clickable-card {
  cursor: pointer;
}

.clickable-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 18px rgba(46, 70, 52, 0.1);
  border-color: rgba(46, 98, 79, 0.35);
}

.product-image {
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #f7eddd, #e4d2ba);
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease, filter 0.35s ease;
}

.clickable-card:hover .product-image img {
  transform: scale(1.02);
  filter: brightness(0.95);
}

.product-info {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 240px;
  padding: 16px;
}

.product-content {
  flex: 1;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.product-meta-main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: #8b7d67;
  font-size: 12px;
}

.product-meta-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta-action {
  flex-shrink: 0;
}

.product-name {
  margin: 0 0 12px;
  color: #173a2f;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  min-height: 56px;
  text-align: center;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-description {
  margin: 0 0 12px;
  color: #7a7468;
  font-size: 13px;
  line-height: 1.55;
  min-height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-bottom {
  margin-top: auto;
}

.product-price {
  margin-bottom: 12px;
  display: flex;
  justify-content: center;
}

.current-price {
  font-size: 22px;
  font-weight: 700;
  color: #b97b25;
}

.product-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.product-actions .el-button {
  height: 44px;
  border-radius: 20px;
  margin: 0 !important;
  font-weight: 600;
}

@media (max-width: 768px) {
  .product-info {
    min-height: 236px;
  }
}
</style>
