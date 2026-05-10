import axios from 'axios'
import Cookies from 'js-cookie'

const apiClient = axios.create({
  baseURL: 'http://localhost:8082/api/v1',
  withCredentials: true,
})

apiClient.interceptors.request.use((config) => {
  const sid = Cookies.get('SID')
  if (sid) {
    config.headers['Cookie'] = `SID=${sid}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default apiClient
