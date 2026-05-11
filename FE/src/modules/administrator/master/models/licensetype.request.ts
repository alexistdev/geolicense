export interface LicensetypeRequest {
  id?: string
  name: string
  description: string
  durationDays: number
  maxSeats: number
  isTrial: boolean
}
