import type { RecommendationItem } from '@/api/recommend'
import type { Product } from '@/api/product'

const GENERIC_REASON_PATTERNS = [
  /基于你偏好的茶类、风味、产地与当季饮茶偏好补充推荐/gi,
  /基于你偏好的茶类、风味、产地与当季饮茶偏好/gi,
  /结合你的近期行为偏好(?:进行)?推荐/gi,
  /基于全站热销茶品推荐/gi,
  /与当前茶品存在相似浏览[\/、]?购买轨迹/gi,
  /与当前茶品存在相似浏览[\/]购买轨迹/gi,
  /并结合当季饮茶偏好排序/gi,
  /当季饮茶偏好排序/gi,
  /协同过滤/gi,
  /时间衰减/gi,
  /季节增强/gi,
  /推荐得分[:：]?\s*\d+(?:\.\d+)?/gi,
  /补充推荐/gi,
  /策略推荐/gi,
  /排序结果/gi,
  /猜你喜欢/gi
]

const normalizeText = (text: string): string => {
  return text.replace(/\s+/g, ' ').trim()
}

const stripGenericPhrases = (reason: string): string => {
  let value = reason
  GENERIC_REASON_PATTERNS.forEach((pattern) => {
    value = value.replace(pattern, '')
  })

  return normalizeText(value.replace(/^[，。；、,;\s]+|[，。；、,;\s]+$/g, ''))
}

const isGenericReason = (reason: string): boolean => {
  const normalized = normalizeText(reason)
  if (!normalized) return true
  if (normalized.length < 8) return true
  return GENERIC_REASON_PATTERNS.some((pattern) => pattern.test(normalized))
}

const splitTokens = (raw: string): string[] => {
  return raw
    .split(/[，,、/\s]+/)
    .map((token) => token.trim())
    .filter(Boolean)
}

const pickFlavorEvidence = (product: Product): string[] => {
  const raw = `${product.flavorProfile || ''} ${product.teaTags || ''}`.trim()
  if (!raw) return []
  return splitTokens(raw).slice(0, 3)
}

const pickSpecEvidence = (product: Product): string => {
  const raw = (product.compatibleDevices || '').trim()
  if (!raw) return ''
  const cleaned = raw.replace(/\s+/g, ' ')
  return cleaned.length > 16 ? `${cleaned.slice(0, 16)}...` : cleaned
}

const pickPriceEvidence = (product: Product): string => {
  if (!Number.isFinite(product.price)) return ''
  if (product.price < 60) return '价格区间偏日常口粮'
  if (product.price <= 180) return '价格区间在主流品质段'
  return '价格区间偏精品尝鲜'
}

const sanitizeReason = (reason: string): string => {
  const stripped = stripGenericPhrases(reason)
  return stripped
    .replace(/^(推荐理由|推荐依据|理由)[:：]\s*/gi, '')
    .replace(/[；;，,]{2,}/g, '；')
    .replace(/^[，。；、,;\s]+|[，。；、,;\s]+$/g, '')
}

const containsAnyEvidenceKeyword = (text: string, product: Product): boolean => {
  const normalized = normalizeText(text)
  if (!normalized) return false

  const keywords: string[] = []
  if (product.category) keywords.push(product.category)
  if (product.originPlace) keywords.push(product.originPlace)
  if (product.brand) keywords.push(product.brand)
  keywords.push(...pickFlavorEvidence(product))

  return keywords.some((keyword) => keyword && normalized.includes(keyword))
}

const shouldUseRawReason = (reason: string, product: Product): boolean => {
  if (isGenericReason(reason)) return false
  return containsAnyEvidenceKeyword(reason, product)
}

const pickFallbackText = (fallback: string): string => {
  const cleanFallback = sanitizeReason(fallback)
  if (!cleanFallback) return ''
  if (isGenericReason(cleanFallback)) return ''
  return cleanFallback
}

const buildEvidenceReason = (product: Product): string => {
  const clauses: string[] = []

  if (product.category) {
    clauses.push(`与当前茶品同属${product.category}`)
  }

  if (product.originPlace) {
    clauses.push(`同产地${product.originPlace}`)
  }

  const flavorTokens = pickFlavorEvidence(product)
  if (flavorTokens.length) {
    clauses.push(`风味更偏${flavorTokens.join('、')}`)
  }

  if (product.brand) {
    clauses.push(`同品牌线更容易延续相近口感`)
  }

  const specEvidence = pickSpecEvidence(product)
  if (specEvidence) {
    clauses.push(`规格信息适合日常饮用`)
  }

  const priceEvidence = pickPriceEvidence(product)
  if (priceEvidence) {
    clauses.push(priceEvidence)
  }

  if (!clauses.length) {
    return '这款茶信息完整，适合作为同类对比选择。'
  }

  const opening = clauses.slice(0, 2).join('、')
  const middle = clauses.slice(2, 4)
  const tail = priceEvidence && !clauses.slice(0, 4).includes(priceEvidence) ? `，${priceEvidence}` : ''

  if (middle.length) {
    return `${opening}，${middle.join('，')}，适合作为搭配选择${tail}。`
  }

  return `${opening}，适合作为搭配选择${tail}。`
}

// Keep API stable for existing page calls.
export const getReliableRecommendationReason = (
  item: Partial<RecommendationItem> | null | undefined,
  fallback = ''
): string => {
  const product = item?.product as Product | undefined

  if (!product) {
    return pickFallbackText(fallback) || '商品信息较完整，适合作为同类对比选择。'
  }

  const rawReason = normalizeText(String(item?.reason || ''))
  const cleanReason = sanitizeReason(rawReason)

  if (cleanReason && shouldUseRawReason(cleanReason, product)) {
    return cleanReason
  }

  const fallbackText = pickFallbackText(fallback)
  if (fallbackText) return fallbackText

  return buildEvidenceReason(product)
}
