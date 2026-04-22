const merchantPresetMap: Record<string, { brandName: string; companyName: string }> = {
  BREW001: { brandName: '沏刻', companyName: '沏刻茶业有限公司' },
  BREW002: { brandName: '云岭', companyName: '云岭茶坊有限公司' },
  BREW003: { brandName: '山岚', companyName: '山岚茶业有限公司' }
}

const COMPANY_SUFFIXES = [
  '茶业有限责任公司',
  '茶叶有限责任公司',
  '茶坊有限责任公司',
  '茶业有限公司',
  '茶叶有限公司',
  '茶坊有限公司',
  '有限责任公司',
  '有限公司'
]

export const normalizeMerchantId = (merchantId?: string | null) => {
  return (merchantId || '').trim().toUpperCase()
}

export const getMerchantPreset = (merchantId?: string | null) => {
  const normalizedMerchantId = normalizeMerchantId(merchantId)
  return merchantPresetMap[normalizedMerchantId]
}

export const getMerchantCompanyName = (merchantInfo?: { merchantId?: string; companyName?: string } | null, fallbackAccount?: string | null) => {
  const normalizedMerchantId = normalizeMerchantId(merchantInfo?.merchantId || fallbackAccount)
  const companyName = (merchantInfo?.companyName || '').trim()
  if (companyName) {
    return companyName
  }
  return getMerchantPreset(normalizedMerchantId)?.companyName || ''
}

export const getMerchantBrandName = (merchantInfo?: { merchantId?: string; companyName?: string } | null, fallbackAccount?: string | null) => {
  const normalizedMerchantId = normalizeMerchantId(merchantInfo?.merchantId || fallbackAccount)
  const companyName = getMerchantCompanyName(merchantInfo, fallbackAccount)
  if (companyName) {
    for (const suffix of COMPANY_SUFFIXES) {
      if (companyName.endsWith(suffix)) {
        const brandName = companyName.slice(0, companyName.length - suffix.length).trim()
        if (brandName) {
          return brandName
        }
      }
    }
    return companyName
  }
  return getMerchantPreset(normalizedMerchantId)?.brandName || normalizedMerchantId || '茶叶商家'
}

export const getMerchantStatusLabel = (status?: string | null) => {
  const normalizedStatus = (status || '').trim().toUpperCase()
  const labelMap: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审核',
    REJECTED: '已拒绝',
    SUSPENDED: '已暂停'
  }
  return labelMap[normalizedStatus] || '未知状态'
}
