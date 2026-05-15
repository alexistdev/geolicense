<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import licenseService from '@/modules/user/license/services/license.service.ts'
import type { LicenseItem } from '@/modules/user/license/models/license.response.ts'
import { useLicenseStore } from '@/modules/user/license/stores/license.store.ts'

const route = useRoute()
const router = useRouter()
const licenseStore = useLicenseStore()
const license = ref<LicenseItem | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const copied = ref(false)

const userId = computed<string>(() => {
  const raw = localStorage.getItem('userId')
  return raw ? JSON.parse(raw) : ''
})

const licenseId = computed(() => route.params['id'] as string)

async function fetchDetail() {
  if (!userId.value || !licenseId.value) return

  const cached = licenseStore.findById(licenseId.value)
  if (cached) {
    license.value = cached
    return
  }

  loading.value = true
  error.value = null
  try {
    const res = await licenseService.getDetail(userId.value, licenseId.value)
    if (res.status) {
      license.value = res.payload
      licenseStore.setItem(res.payload)
    } else {
      error.value = res.messages[0] ?? 'Failed to load license.'
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    error.value = err.response?.data?.messages?.[0] ?? 'Failed to load license.'
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

const daysRemaining = computed(() => {
  if (!license.value) return 0
  const diff = new Date(license.value.expiresAt).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
})

const progressPercent = computed(() => {
  if (!license.value) return 0
  const total = license.value.licenseType.durationDays
  if (total <= 0) return 100
  const elapsed = total - daysRemaining.value
  return Math.min(100, Math.round((elapsed / total) * 100))
})

const isExpired = computed(() => daysRemaining.value === 0)

const statusLabel = computed(() => {
  if (!license.value) return ''
  if (isExpired.value) return 'EXPIRED'
  if (license.value.licenseType.isTrial) return 'TRIAL'
  return 'ACTIVE'
})

const statusCls = computed(() => {
  if (isExpired.value) return 'bg-error/10 text-error border border-error/20'
  if (license.value?.licenseType.isTrial) return 'bg-secondary-container text-on-secondary-container'
  return 'bg-primary/10 text-primary border border-primary/20'
})

const instanceLimit = computed(() => {
  if (!license.value) return '—'
  return license.value.licenseType.maxSeats === 0 ? 'Unlimited' : String(license.value.licenseType.maxSeats)
})

async function copyKey() {
  if (!license.value) return
  await navigator.clipboard.writeText(license.value.licenseKey)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

onMounted(() => fetchDetail())
</script>

<template>
  <DashboardLayout>

    <!-- Loading skeleton -->
    <template v-if="loading">
      <div class="animate-pulse space-y-6">
        <div class="h-10 w-64 rounded bg-surface-container"></div>
        <div class="grid grid-cols-12 gap-6">
          <div class="col-span-12 lg:col-span-5 h-80 rounded-xl bg-surface-container"></div>
          <div class="col-span-12 lg:col-span-7 h-80 rounded-xl bg-surface-container"></div>
        </div>
      </div>
    </template>

    <!-- Error state -->
    <template v-else-if="error">
      <div class="flex flex-col items-center justify-center py-32 gap-4">
        <span class="material-symbols-outlined text-5xl text-error opacity-60">error</span>
        <p class="text-on-surface-variant text-sm">{{ error }}</p>
        <button
          class="px-5 py-2 rounded-md bg-surface-container text-on-surface text-sm font-semibold hover:bg-surface-container-high transition-colors"
          @click="router.push({ name: 'user-license' })"
        >
          Back to Licenses
        </button>
      </div>
    </template>

    <!-- Detail content -->
    <template v-else-if="license">
      <!-- Header Section -->
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
        <div>
          <div class="flex items-center gap-3 mb-4">
            <button
              class="text-primary hover:text-primary-fixed transition-colors flex items-center gap-1 text-sm font-medium"
              @click="router.push({ name: 'user-license' })"
            >
              <span class="material-symbols-outlined text-base">arrow_back</span> Back to Licenses
            </button>
          </div>
          <h1 class="text-4xl font-extrabold text-on-surface tracking-tight mb-2">
            License Details: {{ license.licenseKey }}
          </h1>
          <div class="flex items-center gap-3">
            <span
              class="px-3 py-1 rounded-full text-[0.6875rem] font-bold tracking-widest"
              :class="statusCls"
            >{{ statusLabel }}</span>
            <span class="bg-secondary-container text-on-secondary-container px-3 py-1 rounded-full text-[0.6875rem] font-bold tracking-widest">
              {{ license.licenseType.name.toUpperCase() }}
            </span>
          </div>
        </div>
        <div class="flex gap-4">
          <button
            class="px-6 py-2.5 rounded-md border border-outline-variant/30 text-on-surface-variant hover:bg-surface-container transition-all font-semibold text-sm flex items-center gap-2"
          >
            <span class="material-symbols-outlined text-lg">download</span> Download Certificate
          </button>
          <button
            class="px-6 py-2.5 rounded-md bg-gradient-to-br from-primary to-primary-container text-on-primary hover:shadow-lg transition-all font-bold text-sm"
          >
            Renew License
          </button>
        </div>
      </div>

      <!-- Dashboard Grid -->
      <div class="grid grid-cols-12 gap-6">
        <!-- Time Remaining (High Impact Card) -->
        <div
          class="col-span-12 lg:col-span-5 bg-surface-container p-8 rounded-xl relative overflow-hidden flex flex-col justify-between min-h-[320px]"
        >
          <div class="relative z-10">
            <h3
              class="text-on-surface-variant font-medium uppercase tracking-[0.1em] text-xs mb-8 flex items-center gap-2"
            >
              <span class="material-symbols-outlined text-primary text-lg">schedule</span> Time
              Remaining
            </h3>
            <div class="mb-4">
              <span class="text-7xl font-black text-on-surface tracking-tighter">{{ daysRemaining }}</span>
              <span class="text-xl font-light text-on-surface-variant ml-2">Days Remaining</span>
            </div>
            <p class="text-on-surface-variant text-sm max-w-xs">
              <template v-if="isExpired">This license has expired. Please renew to continue access.</template>
              <template v-else-if="daysRemaining <= 30">Your license is expiring soon. Renewal is recommended.</template>
              <template v-else>Your license is in good standing. Renewal recommended 30 days prior to expiration.</template>
            </p>
          </div>
          <div class="mt-8 relative z-10">
            <div
              class="flex justify-between items-center text-xs font-bold tracking-widest text-on-surface-variant mb-2"
            >
              <span>PROGRESSION</span>
              <span class="text-primary">{{ progressPercent }}%</span>
            </div>
            <div class="w-full bg-surface-container-lowest h-3 rounded-full overflow-hidden">
              <div
                class="bg-gradient-to-r from-primary to-primary-container h-full rounded-full shadow-[0_0_15px_rgba(173,198,255,0.3)] transition-all"
                :style="{ width: progressPercent + '%' }"
              ></div>
            </div>
          </div>
          <div
            class="absolute -right-16 -top-16 w-64 h-64 bg-primary/5 rounded-full blur-3xl"
          ></div>
        </div>

        <!-- Metadata Card -->
        <div
          class="col-span-12 lg:col-span-7 bg-surface-container p-8 rounded-xl border border-outline-variant/10"
        >
          <h3
            class="text-on-surface-variant font-medium uppercase tracking-[0.1em] text-xs mb-8 flex items-center gap-2"
          >
            <span class="material-symbols-outlined text-primary text-lg">badge</span> License
            Metadata
          </h3>
          <div class="grid grid-cols-2 gap-y-10">
            <div>
              <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">
                Product Name
              </p>
              <p class="text-lg font-bold text-on-surface">{{ license.product.name }}</p>
              <p class="text-xs text-on-surface-variant">v{{ license.product.version }}</p>
            </div>
            <div>
              <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">
                License Type
              </p>
              <p class="text-lg font-bold text-on-surface">{{ license.licenseType.name }}</p>
            </div>
            <div>
              <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">
                Issued Date
              </p>
              <p class="text-lg font-bold text-on-surface">{{ formatDate(license.issuedAt) }}</p>
            </div>
            <div>
              <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">
                Expiration Date
              </p>
              <p class="text-lg font-bold text-on-surface" :class="isExpired ? 'text-error' : ''">
                {{ formatDate(license.expiresAt) }}
              </p>
            </div>
            <div>
              <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">
                Instance Limit
              </p>
              <div class="flex items-center gap-2">
                <p class="text-lg font-bold text-on-surface">{{ instanceLimit }}</p>
                <span v-if="license.licenseType.maxSeats === 0" class="material-symbols-outlined text-primary text-sm">all_inclusive</span>
              </div>
            </div>
            <div>
              <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">
                Description
              </p>
              <p class="text-sm font-medium text-on-surface">{{ license.licenseType.description || '—' }}</p>
            </div>
          </div>
        </div>

        <!-- License Key Section -->
        <div class="col-span-12 bg-surface-container p-8 rounded-xl relative">
          <h3
            class="text-on-surface-variant font-medium uppercase tracking-[0.1em] text-xs mb-8 flex items-center gap-2"
          >
            <span class="material-symbols-outlined text-primary text-lg">lock</span> Security
            Infrastructure
          </h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <div class="flex justify-between items-center mb-3">
                <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest">
                  License Key
                </p>
                <button
                  class="text-[0.625rem] text-primary hover:underline font-bold uppercase tracking-widest flex items-center gap-1"
                  @click="copyKey"
                >
                  <span class="material-symbols-outlined text-xs">{{ copied ? 'check' : 'content_copy' }}</span>
                  {{ copied ? 'Copied!' : 'Copy' }}
                </button>
              </div>
              <div
                class="bg-surface-container-lowest p-5 rounded-lg border border-outline-variant/10 max-h-48 overflow-y-auto font-mono text-xs leading-relaxed text-blue-200/70 break-all"
              >
                {{ license.licenseKey }}
              </div>
            </div>
          </div>
          <div class="mt-8 pt-8 border-t border-outline-variant/5 flex justify-end">
            <button
              class="bg-tertiary-container text-on-tertiary-container px-6 py-2.5 rounded-md font-bold text-sm flex items-center gap-2 hover:bg-red-500 hover:text-white transition-all active:scale-95 duration-200"
            >
              <span class="material-symbols-outlined text-lg">report</span> Revoke License
            </button>
          </div>
        </div>
      </div>
    </template>

  </DashboardLayout>
</template>
