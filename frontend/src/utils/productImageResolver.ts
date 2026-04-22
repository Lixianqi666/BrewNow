const PRODUCT_IMAGE_BASE_URL = 'http://127.0.0.1:9000/brew-now/products'

const buildProductImageUrl = (fileName: string): string => `${PRODUCT_IMAGE_BASE_URL}/${fileName}`

const DEFAULT_PRODUCT_IMAGE = buildProductImageUrl('tea_035.jpg')

const PRODUCT_IMAGE_POOL = Array.from({ length: 35 }, (_, index) =>
  buildProductImageUrl(`tea_${String(index + 1).padStart(3, '0')}.jpg`)
)

const PRODUCT_IMAGE_BY_NAME: Record<string, string> = {
  '明前西湖龙井 100g': buildProductImageUrl('tea_001.jpg'),
  '祁门红茶 特级 150g': buildProductImageUrl('tea_002.jpg'),
  '安溪铁观音 兰花香 125g': buildProductImageUrl('tea_003.jpg'),
  '福鼎白毫银针 一级 100g': buildProductImageUrl('tea_004.jpg'),
  '武夷山正山小种 120g': buildProductImageUrl('tea_005.jpg'),
  '福鼎寿眉 2019年陈化 200g': buildProductImageUrl('tea_006.jpg'),
  '云南古树普洱熟茶饼 357g': buildProductImageUrl('tea_007.jpg'),
  '茉莉花茶 绿雪芽 100g': buildProductImageUrl('tea_008.jpg'),
  '凤凰单丛 鸭屎香 100g': buildProductImageUrl('tea_009.jpg'),
  '岩韵花香乌龙 150g': buildProductImageUrl('tea_010.jpg'),
  '白牡丹 2022春茶 125g': buildProductImageUrl('tea_011.jpg'),
  '安吉白茶 明前 100g': buildProductImageUrl('tea_012.jpg'),
  '六安瓜片 春茶 120g': buildProductImageUrl('tea_013.jpg'),
  '玫瑰花茶组合 120g': buildProductImageUrl('tea_014.jpg'),
  '桂花乌龙 125g': buildProductImageUrl('tea_015.jpg'),
  '陈皮普洱 150g': buildProductImageUrl('tea_016.jpg'),
  '大红袍 岩茶特级 100g': buildProductImageUrl('tea_017.jpg'),
  '金骏眉 红茶 100g': buildProductImageUrl('tea_018.jpg'),
  '滇红松针 125g': buildProductImageUrl('tea_019.jpg'),
  '碧螺春 特级 100g': buildProductImageUrl('tea_020.jpg'),
  '太平猴魁 礼盒 80g': buildProductImageUrl('tea_021.jpg'),
  '信阳毛尖 春茶 100g': buildProductImageUrl('tea_022.jpg'),
  '正山小种 烟熏款 100g': buildProductImageUrl('tea_023.jpg'),
  '坦洋工夫 红茶 150g': buildProductImageUrl('tea_024.jpg'),
  '水仙岩茶 100g': buildProductImageUrl('tea_025.jpg'),
  '单丛蜜兰香 100g': buildProductImageUrl('tea_026.jpg'),
  '福鼎白牡丹 一级 100g': buildProductImageUrl('tea_027.jpg'),
  '老寿眉 饼茶 300g': buildProductImageUrl('tea_028.jpg'),
  '胎菊花茶 80g': buildProductImageUrl('tea_029.jpg'),
  '桂圆红枣花茶 120g': buildProductImageUrl('tea_030.jpg'),
  '普洱生茶 饼 357g': buildProductImageUrl('tea_031.jpg'),
  '普洱熟茶 金砖 250g': buildProductImageUrl('tea_032.jpg'),
  '东方美人 乌龙 100g': buildProductImageUrl('tea_033.jpg'),
  '阿萨姆奶茶红茶基底 200g': buildProductImageUrl('tea_034.jpg'),
  '雨前龙井 200g 家庭装': buildProductImageUrl('tea_035.jpg')
}

const keywordFallbacks: Array<{ keyword: string; imageUrl: string }> = [
  { keyword: '龙井', imageUrl: buildProductImageUrl('tea_001.jpg') },
  { keyword: '碧螺春', imageUrl: buildProductImageUrl('tea_020.jpg') },
  { keyword: '绿茶', imageUrl: buildProductImageUrl('tea_012.jpg') },
  { keyword: '祁门', imageUrl: buildProductImageUrl('tea_002.jpg') },
  { keyword: '阿萨姆', imageUrl: buildProductImageUrl('tea_034.jpg') },
  { keyword: '红茶', imageUrl: buildProductImageUrl('tea_018.jpg') },
  { keyword: '乌龙', imageUrl: buildProductImageUrl('tea_003.jpg') },
  { keyword: '白茶', imageUrl: buildProductImageUrl('tea_004.jpg') },
  { keyword: '花茶', imageUrl: buildProductImageUrl('tea_008.jpg') },
  { keyword: '普洱', imageUrl: buildProductImageUrl('tea_007.jpg') }
]

const pickFallbackFromPool = (seed?: string): string => {
  if (!seed) {
    return DEFAULT_PRODUCT_IMAGE
  }

  let hash = 0
  for (let index = 0; index < seed.length; index += 1) {
    hash = (hash * 31 + seed.charCodeAt(index)) >>> 0
  }

  return PRODUCT_IMAGE_POOL[hash % PRODUCT_IMAGE_POOL.length] || DEFAULT_PRODUCT_IMAGE
}

export const resolveProductImageUrl = (productName?: string, sourceImageUrl?: string): string => {
  const normalizedName = productName?.trim()
  const normalizedSource = sourceImageUrl?.trim()
  const isRandomPlaceholder = normalizedSource?.includes('picsum.photos')
  const isGeneratedCatalogPlaceholder = normalizedSource?.includes('/catalog/product-')
    || normalizedSource?.includes('/products/product-')
    || normalizedSource?.toLowerCase().endsWith('.svg')
  const isManagedSource = normalizedSource?.includes('/brew-now/')
    || normalizedSource?.includes(':9000/')
    || normalizedSource?.includes('/api/uploads/')

  if (normalizedSource && isManagedSource && !isGeneratedCatalogPlaceholder) {
    return normalizedSource
  }

  if (normalizedName && PRODUCT_IMAGE_BY_NAME[normalizedName]) {
    return PRODUCT_IMAGE_BY_NAME[normalizedName]
  }

  if (normalizedName) {
    const matchedKeywordFallback = keywordFallbacks.find((item) => normalizedName.includes(item.keyword))
    if (matchedKeywordFallback) {
      return matchedKeywordFallback.imageUrl
    }
  }

  if (normalizedSource && !isRandomPlaceholder && !isGeneratedCatalogPlaceholder) {
    return normalizedSource
  }

  return pickFallbackFromPool(normalizedName || normalizedSource)
}

export const getDefaultProductImage = (): string => DEFAULT_PRODUCT_IMAGE
