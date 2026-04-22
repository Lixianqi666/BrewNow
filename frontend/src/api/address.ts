import request from './request'

export interface UserAddress {
  addressId?: number
  userId?: number
  receiverName: string
  contactPhone: string
  province?: string
  city?: string
  district?: string
  detailAddress: string
  tag?: string
  isDefault?: boolean
  createTime?: string
  updateTime?: string
}

export const addressApi = {
  getList() {
    return request.get<UserAddress[]>('/address/list')
  },
  getDefault() {
    return request.get<UserAddress | null>('/address/default')
  },
  addAddress(data: UserAddress) {
    return request.post<UserAddress>('/address/add', data)
  },
  quickAddAddress(data: UserAddress) {
    return request.post<UserAddress>('/address/quick-add', data)
  },
  updateAddress(data: UserAddress) {
    return request.put('/address/update', data)
  },
  deleteAddress(addressId: number) {
    return request.delete(`/address/${addressId}`)
  },
  setDefault(addressId: number) {
    return request.put(`/address/default/${addressId}`)
  }
}

