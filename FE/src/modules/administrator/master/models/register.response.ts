export interface RegisteredUser {
  id: string
  fullName: string
  email: string
  role: string
  createdDate: string | null
  modifiedDate: string | null
  suspended: boolean
}

export interface RegisterPayload {
  user: RegisteredUser
  token: string
}

export interface RegisterResponse {
  messages: string[]
  payload: RegisterPayload
  status: boolean
}
