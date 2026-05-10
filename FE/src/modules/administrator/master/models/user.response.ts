export interface UserResponse {
  id: string
  username: string
  fullName: string
  email: string
  role: string
  isSuspend: boolean
  lastLogin: string
  createdDate: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
