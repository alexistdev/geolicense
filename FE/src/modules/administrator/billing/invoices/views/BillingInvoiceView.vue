<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import adminInvoiceService from '@/modules/administrator/billing/invoices/services/invoice.service.ts'
import type { InvoiceItem } from '@/modules/administrator/billing/invoices/services/invoice.service.ts'

const router = useRouter()
const PAGE_SIZE = 10

const invoices = ref<InvoiceItem[]>([])
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const error = ref<string | null>(null)
const searchKeyword = ref('')

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    fetchInvoices()
  }, 300)
}

async function fetchInvoices() {
  loading.value = true
  error.value = null
  try {
    const params = {
      page: currentPage.value,
      size: PAGE_SIZE,
      sortBy: 'createdDate',
      direction: 'desc' as const,
    }
    const res = searchKeyword.value.trim()
      ? await adminInvoiceService.searchInvoices(searchKeyword.value.trim(), params)
      : await adminInvoiceService.getAllInvoices(params)

    if (res.status) {
      invoices.value = res.payload.content
      totalElements.value = res.payload.totalElements
      totalPages.value = res.payload.totalPages
      currentPage.value = res.payload.number
    } else {
      invoices.value = []
      totalElements.value = 0
      totalPages.value = 0
      currentPage.value = 0
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    error.value = err.response?.data?.messages?.[0] ?? 'Failed to load invoices. Please try again.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchInvoices)

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchInvoices()
}

const showingFrom = computed(() =>
  totalElements.value === 0 ? 0 : currentPage.value * PAGE_SIZE + 1,
)
const showingTo = computed(() =>
  Math.min((currentPage.value + 1) * PAGE_SIZE, totalElements.value),
)

const pageNumbers = computed<(number | '...')[]>(() => {
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)

  const pages: (number | '...')[] = [0]
  if (current > 2) pages.push('...')
  for (let i = Math.max(1, current - 1); i <= Math.min(total - 2, current + 1); i++) {
    pages.push(i)
  }
  if (current < total - 3) pages.push('...')
  pages.push(total - 1)
  return pages
})

function invoiceStatus(status: string): { label: string; cls: string } {
  if (status === 'PAID') return { label: 'Paid', cls: 'bg-green-100 text-green-800' }
  if (status === 'CANCELLED') return { label: 'Cancelled', cls: 'bg-error-container text-on-error-container' }
  if (status === 'AWAITING_VERIFICATION') return { label: 'Awaiting Verification', cls: 'bg-blue-100 text-blue-800' }
  return { label: 'Unpaid', cls: 'bg-amber-100 text-amber-800' }
}

function formatAmount(amount: number, currency: string): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

function formatDate(dateStr: string): string {
  return new Intl.DateTimeFormat('en-US', { dateStyle: 'medium' }).format(new Date(dateStr))
}

// --- Toast ---
interface Toast {
  id: number
  message: string
  type: 'success' | 'warning' | 'error'
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

void showToast
</script>

<template>
  <DashboardLayout>
    <main class="p-12 min-h-screen">
      <!-- Header -->
      <section class="grid grid-cols-12 gap-6 mb-12">
        <div class="col-span-12 lg:col-span-8">
          <h2 class="text-4xl font-headline font-extrabold text-on-surface tracking-tight mb-2">
            Invoices
          </h2>
          <p class="text-on-surface-variant font-body text-lg">
            Manage all invoices for GeoLicense
          </p>
        </div>
      </section>

      <!-- Metrics Cards -->
      <section class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Total Invoices
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ totalElements.toLocaleString() }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-primary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-fixed-variant">receipt_long</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Paid
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ invoices.filter((i) => i.status === 'PAID').length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-tertiary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-tertiary-fixed-variant">check_circle</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Pending
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ invoices.filter((i) => i.status === 'UNPAID').length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-secondary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-secondary-fixed-variant">hourglass_empty</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              This Page
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ invoices.length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-primary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-fixed-variant">list_alt</span>
          </div>
        </div>
      </section>

      <!-- Data Table -->
      <div
        class="bg-surface-container-lowest rounded-xl shadow-xl shadow-black/[0.03] overflow-hidden"
      >
        <!-- Table Controls -->
        <div class="p-6 bg-surface-container-low flex flex-wrap items-center justify-between gap-4">
          <div class="relative">
            <span
              class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline"
              >search</span
            >
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="Search by invoice number..."
              class="pl-10 pr-4 py-2.5 bg-surface-container-lowest border-none rounded-lg text-sm text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary/20 w-64"
              @input="onSearchInput"
            />
          </div>
        </div>

        <!-- Error Banner -->
        <div
          v-if="error"
          class="px-6 py-4 bg-error-container text-on-error-container text-sm font-medium flex items-center gap-2"
        >
          <span class="material-symbols-outlined text-base">error</span>
          {{ error }}
        </div>

        <!-- Table -->
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-surface-container-low border-b border-surface-container-high">
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Invoice No.
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Order No.
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Amount
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Issued At
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Status
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-surface-container">
              <!-- Loading skeleton -->
              <template v-if="loading">
                <tr v-for="n in PAGE_SIZE" :key="n" class="animate-pulse">
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-4">
                      <div class="w-10 h-10 bg-surface-container rounded-xl"></div>
                      <div class="h-3 w-36 bg-surface-container rounded"></div>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-48 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-24 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-28 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-6 w-20 bg-surface-container rounded-full"></div>
                  </td>
                </tr>
              </template>

              <!-- Empty state -->
              <tr v-else-if="invoices.length === 0">
                <td colspan="5" class="px-6 py-16 text-center text-on-surface-variant">
                  <span class="material-symbols-outlined text-4xl block mb-2">receipt_long</span>
                  No invoices found.
                </td>
              </tr>

              <!-- Data rows -->
              <tr
                v-else
                v-for="item in invoices"
                :key="item.id"
                class="hover:bg-surface-container-low transition-colors cursor-pointer"
                @click="router.push({ name: 'admin-invoice-detail', params: { id: item.id } })"
              >
                <td class="px-6 py-4">
                  <div class="flex items-center gap-4">
                    <div
                      class="w-10 h-10 rounded-xl bg-primary-fixed flex items-center justify-center shrink-0"
                    >
                      <span class="material-symbols-outlined text-on-primary-fixed-variant text-lg"
                        >receipt</span
                      >
                    </div>
                    <p class="font-mono font-bold text-on-surface text-sm leading-tight">
                      {{ item.invoiceNumber }}
                    </p>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <p class="font-mono text-sm text-on-surface-variant">{{ item.orderNumber }}</p>
                </td>
                <td class="px-6 py-4">
                  <p class="text-sm font-bold text-on-surface">
                    {{ formatAmount(item.totalAmount, item.currency) }}
                  </p>
                </td>
                <td class="px-6 py-4">
                  <p class="text-sm text-on-surface-variant">{{ formatDate(item.issuedAt) }}</p>
                </td>
                <td class="px-6 py-4">
                  <span
                    class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold"
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
        <div
          class="px-6 py-6 flex items-center justify-between border-t border-surface-container"
        >
          <p class="text-sm text-on-surface-variant">
            Showing
            <span class="font-bold text-on-surface">{{ showingFrom }} - {{ showingTo }}</span>
            of
            <span class="font-bold text-on-surface">{{ totalElements.toLocaleString() }}</span>
            invoices
          </p>
          <div class="flex items-center gap-2">
            <button
              :disabled="currentPage === 0"
              aria-label="Previous page"
              class="w-10 h-10 flex items-center justify-center rounded-lg bg-surface-container-high text-on-surface-variant transition-colors"
              :class="
                currentPage === 0
                  ? 'cursor-not-allowed opacity-40'
                  : 'hover:bg-surface-container-highest cursor-pointer'
              "
              @click="goToPage(currentPage - 1)"
            >
              <span class="material-symbols-outlined">chevron_left</span>
            </button>

            <template v-for="(p, i) in pageNumbers" :key="i">
              <span v-if="p === '...'" class="px-2 text-outline">...</span>
              <button
                v-else
                class="w-10 h-10 flex items-center justify-center rounded-lg font-bold transition-colors"
                :class="
                  p === currentPage
                    ? 'bg-primary text-on-primary'
                    : 'hover:bg-surface-container-high text-on-surface-variant'
                "
                @click="goToPage(p)"
              >
                {{ p + 1 }}
              </button>
            </template>

            <button
              :disabled="currentPage >= totalPages - 1"
              aria-label="Next page"
              class="w-10 h-10 flex items-center justify-center rounded-lg bg-surface-container-high text-on-surface transition-colors"
              :class="
                currentPage >= totalPages - 1
                  ? 'cursor-not-allowed opacity-40'
                  : 'hover:bg-surface-container-highest cursor-pointer'
              "
              @click="goToPage(currentPage + 1)"
            >
              <span class="material-symbols-outlined">chevron_right</span>
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- Toast Stack -->
    <Teleport to="body">
      <div class="fixed bottom-6 right-6 z-[60] flex flex-col gap-3 items-end">
        <TransitionGroup name="toast">
          <div
            v-for="toast in toasts"
            :key="toast.id"
            class="flex items-center gap-3 px-4 py-3 rounded-xl shadow-xl text-sm font-semibold min-w-64 max-w-sm"
            :class="{
              'bg-green-100 text-green-800': toast.type === 'success',
              'bg-amber-100 text-amber-800': toast.type === 'warning',
              'bg-error-container text-on-error-container': toast.type === 'error',
            }"
          >
            <span class="material-symbols-outlined text-base shrink-0">
              {{
                toast.type === 'success'
                  ? 'check_circle'
                  : toast.type === 'warning'
                    ? 'edit_note'
                    : 'error'
              }}
            </span>
            <span class="flex-1">{{ toast.message }}</span>
          </div>
        </TransitionGroup>
      </div>
    </Teleport>
  </DashboardLayout>
</template>

<style>
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
