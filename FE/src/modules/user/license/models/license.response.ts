
export interface LicensePlanItem {
  id: string
  productId: string
  name: string
  billingCycle: string
  durationDays: number
  maxSeats: number
  price: number
  currency: string
  isActive: boolean
}

export interface LicenseItem {
  id: string
  userId: string
  licensePlan: LicensePlanItem
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

export interface LicenseDetailResponse {
  messages: string[]
  payload: LicenseItem
  status: boolean
}