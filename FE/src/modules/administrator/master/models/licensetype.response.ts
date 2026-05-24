export interface LicenseTypePayload {
  id: string
  name: string
  description: string
  isTrial: boolean
}

export interface LicenseTypeResponse {
  messages: string[]
  payload: LicenseTypePayload
  status: boolean
}
