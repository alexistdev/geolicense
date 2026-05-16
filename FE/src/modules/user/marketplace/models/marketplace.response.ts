export interface ProductItem {
  productId: string
  productName: string
  description: string
  version: string
  startingPrice: number
  currency: string
  totalPlans: number
  hasTrial: boolean
}

export interface ProductPlan {
  planId: string
  planName: string
  licenseType: string
  price: number
  currency: string
  billingCycle: string
  durationDays: number
  maxSeats: number
  trial: boolean
}

export interface ProductDetail {
  productId: string
  name: string
  version: string
  description: string
  plans: ProductPlan[]
}
