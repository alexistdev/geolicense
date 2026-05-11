export interface LicenseTypePayload {
  id: string
  name: string
  description: string
  durationDays: number
  maxSeats: number
  isTrial: boolean
}

export interface LicenseTypeResponse {
  messages: string[]
  payload: LicenseTypePayload
  status: boolean
}
