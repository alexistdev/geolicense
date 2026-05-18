<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import invoiceService from '@/modules/user/invoice/services/invoice.service.ts'
import type { InvoiceItem } from '@/modules/user/invoice/models/invoice.response.ts'

const invoices = ref<InvoiceItem[]>([])
const loading = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const PAGE_SIZE = 10

interface Toast {
  id: number
  message: string
  type: 'success' | 'error'
}
const toasts = ref<Toast[]>([])
let toastSeq = 0

function showToast(message: string, type: Toast['type'] = 'success') {
  const id = ++toastSeq
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 4000)
}

const showingFrom = computed(() => totalElements.value === 0 ? 0 : currentPage.value * PAGE_SIZE + 1)
const showingTo = computed(() => Math.min((currentPage.value + 1) * PAGE_SIZE, totalElements.value))

async function fetchInvoices(page = 0) {
  loading.value = true
  try {
    const res = await invoiceService.getMyInvoices({
      page,
      size: PAGE_SIZE,
      sortBy: 'id',
      direction: 'desc',
    })
    if (res.status) {
      invoices.value = res.payload.content
      totalElements.value = res.payload.totalElements
      totalPages.value = res.payload.totalPages
      currentPage.value = res.payload.number
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to load invoices. Please try again.',
      'error',
    )
  } finally {
    loading.value = false
  }
}

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value) return
  fetchInvoices(page)
}

function invoiceStatus(status: number): { label: string; cls: string } {
  if (status === 1) return { label: 'Paid', cls: 'bg-green-500/10 text-green-400' }
  if (status === 2) return { label: 'Cancelled', cls: 'bg-error/10 text-error' }
  return { label: 'Pending', cls: 'bg-yellow-500/10 text-yellow-400' }
}

function formatAmount(amount: number, currency: string): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

const visiblePages = computed<number[]>(() => {
  const total = totalPages.value
  if (total <= 5) return Array.from({ length: total }, (_, i) => i)
  const start = Math.max(0, currentPage.value - 1)
  const end = Math.min(total - 1, start + 2)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

onMounted(() => fetchInvoices(0))
</script>

<template>
  <DashboardLayout>
    <div class="p-8 space-y-8">
      <!-- Page Header -->
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <span class="text-[0.6875rem] font-bold uppercase tracking-[0.2em] text-primary">Billing</span>
          <h2 class="text-4xl font-black tracking-tight text-white mt-1">My Invoices</h2>
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-surface p-6 rounded-xl border border-white/5 shadow-2xl">
          <p class="text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant/60">Total Invoices</p>
          <div class="flex items-baseline gap-2 mt-1">
            <span class="text-3xl font-black text-white">{{ loading ? '—' : totalElements.toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="bg-surface rounded-2xl shadow-2xl overflow-hidden border border-white/5">
        <div class="p-6 flex items-center justify-between bg-surface-container-low/50 border-b border-white/5">
          <h3 class="font-bold text-on-surface flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">receipt_long</span>
            Invoice list
          </h3>
          <span class="text-xs font-medium text-on-surface-variant">
            <template v-if="!loading && totalElements > 0">
              Showing {{ showingFrom }}–{{ showingTo }} of {{ totalElements }}
            </template>
            <template v-else-if="loading">Loading…</template>
            <template v-else>No invoices found</template>
          </span>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-surface-container-lowest/30">
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Invoice No.</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Order ID</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Amount</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Status</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/[0.03]">
              <!-- Loading skeleton -->
              <template v-if="loading">
                <tr v-for="n in PAGE_SIZE" :key="n" class="animate-pulse">
                  <td class="px-6 py-5"><div class="h-3 w-36 rounded bg-surface-variant"></div></td>
                  <td class="px-6 py-5"><div class="h-3 w-48 rounded bg-surface-variant"></div></td>
                  <td class="px-6 py-5"><div class="h-3 w-24 rounded bg-surface-variant"></div></td>
                  <td class="px-6 py-5"><div class="h-5 w-16 rounded-full bg-surface-variant"></div></td>
                </tr>
              </template>

              <!-- Empty state -->
              <tr v-else-if="invoices.length === 0">
                <td colspan="4" class="px-6 py-16 text-center text-on-surface-variant text-sm">
                  <span class="material-symbols-outlined text-4xl block mb-2 opacity-40">receipt_long</span>
                  No invoices found.
                </td>
              </tr>

              <!-- Data rows -->
              <tr v-else v-for="item in invoices" :key="item.id" class="hover:bg-surface-container-low transition-colors">
                <td class="px-6 py-5">
                  <span class="font-mono text-[0.75rem] text-on-surface font-semibold">{{ item.invoiceNumber }}</span>
                </td>
                <td class="px-6 py-5">
                  <span class="font-mono text-[0.7rem] text-on-surface-variant">{{ item.orderId }}</span>
                </td>
                <td class="px-6 py-5">
                  <span class="text-sm font-bold text-white">{{ formatAmount(item.amount, item.currency) }}</span>
                </td>
                <td class="px-6 py-5">
                  <span
                    class="px-3 py-1 rounded-full text-[0.65rem] font-black uppercase tracking-wider"
                    :class="invoiceStatus(item.status).cls"
                  >
                    {{ invoiceStatus(item.status).label }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="px-6 py-4 flex items-center justify-between border-t border-white/5 bg-surface-container-low/20">
          <button
            class="text-xs font-bold flex items-center gap-1 transition-colors"
            :class="currentPage === 0 ? 'text-on-surface-variant/30 cursor-not-allowed' : 'text-on-surface-variant hover:text-white'"
            :disabled="currentPage === 0"
            @click="goToPage(currentPage - 1)"
          >
            <span class="material-symbols-outlined text-[1rem]">chevron_left</span>
            Previous
          </button>
          <div class="flex gap-2">
            <span
              v-for="p in visiblePages"
              :key="p"
              class="w-8 h-8 flex items-center justify-center rounded text-xs font-bold cursor-pointer transition-colors"
              :class="p === currentPage ? 'bg-primary text-on-primary' : 'hover:bg-surface-container text-on-surface-variant'"
              @click="goToPage(p)"
            >
              {{ p + 1 }}
            </span>
          </div>
          <button
            class="text-xs font-bold flex items-center gap-1 transition-colors"
            :class="currentPage >= totalPages - 1 ? 'text-on-surface-variant/30 cursor-not-allowed' : 'text-on-surface-variant hover:text-white'"
            :disabled="currentPage >= totalPages - 1"
            @click="goToPage(currentPage + 1)"
          >
            Next
            <span class="material-symbols-outlined text-[1rem]">chevron_right</span>
          </button>
        </div>
      </div>
    </div>
  </DashboardLayout>

  <!-- Toast Stack -->
  <Teleport to="body">
    <div class="fixed bottom-6 right-6 z-[60] flex flex-col gap-3 items-end">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="flex items-center gap-3 px-4 py-3 rounded-xl shadow-xl text-sm font-semibold min-w-64 max-w-sm"
          :class="
            toast.type === 'success'
              ? 'bg-primary-fixed text-on-primary-fixed-variant'
              : 'bg-error-container text-on-error-container'
          "
        >
          <span class="material-symbols-outlined text-base shrink-0">
            {{ toast.type === 'success' ? 'check_circle' : 'error' }}
          </span>
          <span class="flex-1">{{ toast.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
.toast-move {
  transition: transform 0.3s ease;
}
</style>
