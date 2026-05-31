import type {
  InvoiceItem,
  InvoiceResponse,
  InvoiceDetailResponse,
} from '@/modules/user/invoice/models/invoice.response.ts'
import apiClient from '@/modules/shared/api/api.client.ts'

const BASE_PATH = '/invoices'

export interface InvoicePageParams {
  id?: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

export interface InvoicePageParamsWithFilter {
  filter: string
  page: number
  size: number
  sortBy: string
  direction: 'asc' | 'desc'
}

class AdminInvoiceService {
  async getAllInvoices(params: Omit<InvoicePageParams, 'id'>): Promise<InvoiceResponse> {
    const response = await apiClient.get<InvoiceResponse>(BASE_PATH, { params })
    return response.data
  }

  async searchInvoices(
    keyword: string,
    params: Omit<InvoicePageParams, 'id'>,
  ): Promise<InvoiceResponse> {
    const response = await apiClient.get<InvoiceResponse>(`${BASE_PATH}/search`, {
      params: { keyword, ...params },
    })
    return response.data
  }

  async getInvoiceDetail(invoiceId: string): Promise<InvoiceDetailResponse> {
    const response = await apiClient.get<InvoiceDetailResponse>(`${BASE_PATH}/${invoiceId}`)
    return response.data
  }
}

export type { InvoiceItem }
export default new AdminInvoiceService()