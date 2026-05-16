<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import licenseService from '@/modules/user/license/services/license.service.ts'
import type { LicenseItem } from '@/modules/user/license/models/license.response.ts'
import { useLicenseStore } from '@/modules/user/license/stores/license.store.ts'

const router = useRouter()
const licenseStore = useLicenseStore()
const licenses = ref<LicenseItem[]>([])
const loading = ref(false)
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const PAGE_SIZE = 10
const copiedId = ref<string | null>(null)

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

async function copyLicenseKey(item: LicenseItem) {
  await navigator.clipboard.writeText(item.licenseKey)
  copiedId.value = item.id
  setTimeout(() => { copiedId.value = null }, 2000)
}

const userId = computed<string>(() => {
  const raw = localStorage.getItem('userId')
  return raw ? JSON.parse(raw) : ''
})

const showingFrom = computed(() => totalElements.value === 0 ? 0 : currentPage.value * PAGE_SIZE + 1)
const showingTo = computed(() => Math.min((currentPage.value + 1) * PAGE_SIZE, totalElements.value))

async function fetchLicenses(page = 0) {
  if (!userId.value) return
  loading.value = true
  try {
    const res = await licenseService.getAll(userId.value, {
      page,
      size: PAGE_SIZE,
      sortBy: 'id',
      direction: 'asc',
    })
    if (res.status) {
      licenses.value = res.payload.content
      totalElements.value = res.payload.totalElements
      totalPages.value = res.payload.totalPages
      currentPage.value = res.payload.number
      licenseStore.setItems(res.payload.content)
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to load licenses. Please try again.',
      'error',
    )
  } finally {
    loading.value = false
  }
}

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value) return
  fetchLicenses(page)
}

function isExpired(expiresAt: string): boolean {
  return new Date(expiresAt) < new Date()
}

function licenseStatus(item: LicenseItem): { label: string; cls: string } {
  if (isExpired(item.expiresAt)) return { label: 'Expired', cls: 'bg-error/10 text-error' }
  if (item.licensePlan.billingCycle?.toUpperCase() === 'TRIAL') return { label: 'Trial', cls: 'bg-secondary-container/30 text-secondary' }
  return { label: 'Active', cls: 'bg-primary/10 text-primary' }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const visiblePages = computed<number[]>(() => {
  const total = totalPages.value
  if (total <= 5) return Array.from({ length: total }, (_, i) => i)
  const start = Math.max(0, currentPage.value - 1)
  const end = Math.min(total - 1, start + 2)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

onMounted(() => fetchLicenses(0))
</script>

<template>
  <DashboardLayout>
    <div class="p-8 space-y-8">
      <!-- Page Header -->
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <span class="text-[0.6875rem] font-bold uppercase tracking-[0.2em] text-primary">My Licenses</span>
          <h2 class="text-4xl font-black tracking-tight text-white mt-1">Enterprise Licenses</h2>
        </div>
        <div class="flex gap-3">
          <button class="px-6 py-2 bg-surface-container hover:bg-surface-container-high text-on-surface text-sm font-semibold rounded transition-all flex items-center gap-2">
            <span class="material-symbols-outlined text-sm">filter_list</span>
            Filter View
          </button>
          <button class="px-6 py-2 bg-primary text-on-primary text-sm font-bold rounded shadow-xl shadow-primary/20 flex items-center gap-2 transition-transform active:scale-95">
            <span class="material-symbols-outlined text-sm">add</span>
            New License
          </button>
        </div>
      </div>

      <!-- Bento Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-surface p-6 rounded-xl border border-white/5 shadow-2xl">
          <p class="text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant/60">Total Licenses</p>
          <div class="flex items-baseline gap-2 mt-1">
            <span class="text-3xl font-black text-white">{{ loading ? '—' : totalElements.toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="bg-surface rounded-2xl shadow-2xl overflow-hidden border border-white/5">
        <div class="p-6 flex items-center justify-between bg-surface-container-low/50 border-b border-white/5">
          <h3 class="font-bold text-on-surface flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">database</span>
            License lists
          </h3>
          <div class="flex gap-4">
            <span class="text-xs font-medium text-on-surface-variant">
              <template v-if="!loading && totalElements > 0">
                Showing {{ showingFrom }}–{{ showingTo }} of {{ totalElements }}
              </template>
              <template v-else-if="loading">Loading…</template>
              <template v-else>No licenses found</template>
            </span>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-surface-container-lowest/30">
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Plan</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Pricing</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">License Key</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Status</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant text-right">Expiration</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant text-center">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/[0.03]">
              <!-- Loading skeleton -->
              <template v-if="loading">
                <tr v-for="n in PAGE_SIZE" :key="n" class="animate-pulse">
                  <td class="px-6 py-5">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded-lg bg-surface-variant"></div>
                      <div class="space-y-1.5">
                        <div class="h-3 w-32 rounded bg-surface-variant"></div>
                        <div class="h-2 w-16 rounded bg-surface-variant/60"></div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-5"><div class="h-3 w-24 rounded bg-surface-variant"></div></td>
                  <td class="px-6 py-5"><div class="h-5 w-28 rounded bg-surface-variant"></div></td>
                  <td class="px-6 py-5"><div class="h-5 w-16 rounded-full bg-surface-variant"></div></td>
                  <td class="px-6 py-5 text-right"><div class="h-3 w-20 rounded bg-surface-variant ml-auto"></div></td>
                  <td class="px-6 py-5"><div class="h-7 w-7 rounded bg-surface-variant mx-auto"></div></td>
                </tr>
              </template>

              <!-- Empty state -->
              <tr v-else-if="licenses.length === 0">
                <td colspan="6" class="px-6 py-16 text-center text-on-surface-variant text-sm">
                  <span class="material-symbols-outlined text-4xl block mb-2 opacity-40">key_off</span>
                  No licenses found.
                </td>
              </tr>

              <!-- Data rows -->
              <tr v-else v-for="item in licenses" :key="item.id" class="hover:bg-surface-container-low transition-colors group">
                <td class="px-6 py-5">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-surface-variant flex items-center justify-center text-primary font-bold uppercase">
                      {{ item.licensePlan.name.charAt(0) }}
                    </div>
                    <div>
                      <p class="text-sm font-bold text-white">{{ item.licensePlan.name }}</p>
                      <p class="text-[0.65rem] text-on-surface-variant uppercase tracking-tighter">{{ item.licensePlan.billingCycle }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-5">
                  <span class="text-sm text-on-surface">{{ item.licensePlan.currency }} {{ item.licensePlan.price.toLocaleString() }}</span>
                  <p class="text-[0.65rem] text-on-surface-variant mt-0.5">{{ item.licensePlan.maxSeats }} seat · {{ item.licensePlan.durationDays }}d</p>
                </td>
                <td class="px-6 py-5 font-mono text-[0.7rem] text-on-surface-variant">
                  <div class="flex items-center gap-2">
                    <span class="px-2 py-1 bg-surface-container rounded border border-white/5">{{ item.licenseKey }}</span>
                    <button
                      class="text-on-surface-variant/40 hover:text-primary transition-colors"
                      :title="copiedId === item.id ? 'Copied!' : 'Copy to clipboard'"
                      @click="copyLicenseKey(item)"
                    >
                      <span class="material-symbols-outlined text-[1rem]">
                        {{ copiedId === item.id ? 'check' : 'content_copy' }}
                      </span>
                    </button>
                  </div>
                </td>
                <td class="px-6 py-5">
                  <span
                    class="px-3 py-1 rounded-full text-[0.65rem] font-black uppercase tracking-wider"
                    :class="licenseStatus(item).cls"
                  >
                    {{ licenseStatus(item).label }}
                  </span>
                </td>
                <td class="px-6 py-5 text-right">
                  <span
                    class="text-sm font-medium"
                    :class="isExpired(item.expiresAt) ? 'text-error font-bold italic' : 'text-on-surface'"
                  >
                    {{ isExpired(item.expiresAt) ? 'EXPIRED' : formatDate(item.expiresAt) }}
                  </span>
                </td>
                <td class="px-6 py-5 text-center">
                  <button
                    class="text-on-surface-variant/50 hover:text-primary transition-colors"
                    title="View details"
                    @click="router.push({ name: 'user-license-detail', params: { id: item.id } })"
                  >
                    <span class="material-symbols-outlined text-[1.25rem]">open_in_new</span>
                  </button>
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

      <!-- Contextual Insight Footer -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 py-8 border-t border-white/5">
        <div class="space-y-4">
          <h4 class="text-sm font-black uppercase tracking-widest text-on-surface-variant">System Intelligence</h4>
          <div class="p-5 bg-surface-container rounded-xl flex gap-4">
            <div class="mt-1">
              <span class="material-symbols-outlined text-primary" style="font-variation-settings: 'FILL' 1;">auto_awesome</span>
            </div>
            <div>
              <p class="text-sm font-bold text-white">Notification</p>
              <p class="text-xs text-on-surface-variant mt-1 leading-relaxed">Geobill adalah layanan manajemen license untuk produk-produk digital, dikembangkan oleh AlexistDev.</p>
            </div>
          </div>
        </div>
        <div class="space-y-4">
          <h4 class="text-sm font-black uppercase tracking-widest text-on-surface-variant">Security Alerts</h4>
          <div class="p-5 bg-surface-container rounded-xl flex gap-4 border-l-4 border-tertiary">
            <div class="mt-1">
              <span class="material-symbols-outlined text-tertiary" style="font-variation-settings: 'FILL' 1;">warning</span>
            </div>
            <div>
              <p class="text-sm font-bold text-white">Unrecognized Key Access</p>
              <p class="text-xs text-on-surface-variant mt-1 leading-relaxed">Multiple failed authentication attempts detected from unfamiliar IP addresses.</p>
            </div>
          </div>
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
