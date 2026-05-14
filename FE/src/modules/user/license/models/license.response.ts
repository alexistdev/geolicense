
export interface LicenseTypeItem {
  id: string
  name: string
  description: string
  durationDays: number
  maxSeats: number
  isTrial: boolean
}

export interface ProductItem {
  id: string
  name: string
  version: string
  description: string
  sku: string
  isActive: boolean
}

export interface LicenseItem {
  id: string
  userId: string
  licenseType: LicenseTypeItem
  product: ProductItem
  licenseKey: string
  issuedAt: string
  expiresAt: string
}

export interface LicensePagePayload {
  content: LicenseItem[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface LicenseResponse {
  messages: string[]
  payload: LicensePagePayload
  status: boolean
}
