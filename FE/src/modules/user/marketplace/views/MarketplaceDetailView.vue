<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import marketplaceService from '@/modules/user/marketplace/services/marketplace.service.ts'
import type { ProductDetail, ProductPlan } from '@/modules/user/marketplace/models/marketplace.response.ts'

const route = useRoute()
const router = useRouter()
const product = ref<ProductDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const productId = route.params['productId'] as string

function formatPrice(plan: ProductPlan): string {
  if (plan.price === 0) return `${plan.currency} 0`
  return `${plan.currency} ${plan.price.toLocaleString('id-ID')}`
}

function formatBillingCycle(cycle: string): string {
  const map: Record<string, string> = {
    ONE_TIME: 'One Time',
    MONTHLY: 'Monthly',
    YEARLY: 'Yearly',
    DAILY: 'Daily',
  }
  return map[cycle.toUpperCase()] ?? cycle
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await marketplaceService.getDetail(productId)
    product.value = res.payload
  } catch {
    error.value = 'Failed to load product details. Please try again.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <DashboardLayout>
    <div class="max-w-6xl mx-auto space-y-12">

      <!-- Loading skeleton -->
      <template v-if="loading">
        <div class="animate-pulse space-y-8">
          <div class="h-8 w-48 rounded bg-surface-container"></div>
          <div class="h-40 rounded-xl bg-surface-container"></div>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div v-for="n in 3" :key="n" class="h-72 rounded-xl bg-surface-container"></div>
          </div>
        </div>
      </template>

      <!-- Error state -->
      <template v-else-if="error">
        <div class="flex flex-col items-center justify-center py-32 gap-4">
          <span class="material-symbols-outlined text-5xl text-error opacity-60">error</span>
          <p class="font-body text-body-md text-on-surface-variant">{{ error }}</p>
          <button
            class="px-5 py-2 rounded-md bg-surface-container text-on-surface text-sm font-semibold hover:bg-surface-container-high transition-colors"
            @click="router.push({ name: 'user-marketplace' })"
          >
            Back to Catalog
          </button>
        </div>
      </template>

      <!-- Detail content -->
      <template v-else-if="product">

        <!-- Back link -->
        <button
          class="text-primary hover:text-primary-fixed transition-colors flex items-center gap-1 text-sm font-medium"
          @click="router.push({ name: 'user-marketplace' })"
        >
          <span class="material-symbols-outlined text-base">arrow_back</span> Back to Catalog
        </button>

        <!-- Product hero -->
        <div class="bg-surface rounded-xl p-8 relative overflow-hidden">
          <div class="absolute inset-0 bg-gradient-to-br from-primary/5 to-transparent pointer-events-none"></div>
          <div class="relative z-10 flex flex-col md:flex-row md:items-center gap-6">
            <div
              class="w-20 h-20 rounded-xl bg-surface-container-highest flex items-center justify-center text-primary border border-outline-variant/15 shadow-[0_8px_32px_-8px_rgba(6,14,32,0.8)] shrink-0"
            >
              <span
                class="material-symbols-outlined text-4xl"
                style="font-variation-settings: 'FILL' 1"
              >workspace_premium</span>
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2 flex-wrap">
                <h1 class="font-display text-3xl lg:text-4xl font-extrabold text-on-surface tracking-tight">
                  {{ product.name }}
                </h1>
                <span class="font-label text-label-sm uppercase tracking-widest text-primary bg-primary/10 px-2 py-1 rounded">
                  v{{ product.version }}
                </span>
              </div>
              <p class="font-body text-body-lg text-on-surface-variant max-w-2xl">
                {{ product.description }}
              </p>
            </div>
          </div>
        </div>

        <!-- Plans section -->
        <div class="space-y-6">
          <h2 class="font-display text-2xl font-bold text-on-surface tracking-tight uppercase">
            Available License Plans
          </h2>

          <!-- Empty plans -->
          <div
            v-if="product.plans.length === 0"
            class="text-center py-16 text-on-surface-variant font-body text-body-lg"
          >
            No plans available for this product.
          </div>

          <!-- Plans grid -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="plan in product.plans"
              :key="plan.planId"
              class="bg-surface rounded-xl flex flex-col relative overflow-hidden border transition-all duration-300 group"
              :class="plan.trial
                ? 'border-secondary/20 hover:border-secondary/40'
                : 'border-outline-variant/15 hover:border-primary/30'"
            >
              <!-- Plan header -->
              <div
                class="px-6 py-4 border-b"
                :class="plan.trial ? 'border-secondary/15 bg-secondary/5' : 'border-outline-variant/10 bg-surface-container/50'"
              >
                <div class="flex items-center justify-between gap-2">
                  <h3 class="font-display text-base font-bold text-on-surface uppercase tracking-wide">
                    {{ plan.planName }}
                  </h3>
                  <span
                    v-if="plan.trial"
                    class="font-label text-label-sm uppercase tracking-widest text-tertiary bg-tertiary/10 px-2 py-0.5 rounded"
                  >Trial</span>
                </div>
              </div>

              <!-- Plan body -->
              <div class="px-6 py-6 flex flex-col flex-1 gap-5">
                <!-- Price -->
                <div>
                  <span class="font-display text-3xl font-extrabold text-on-surface">
                    {{ formatPrice(plan) }}
                  </span>
                </div>

                <!-- Features -->
                <ul class="space-y-3 flex-1">
                  <li class="flex items-center gap-3 text-on-surface-variant font-body text-body-sm">
                    <span class="material-symbols-outlined text-primary text-base shrink-0">schedule</span>
                    {{ plan.durationDays }} Day{{ plan.durationDays !== 1 ? 's' : '' }}
                  </li>
                  <li class="flex items-center gap-3 text-on-surface-variant font-body text-body-sm">
                    <span class="material-symbols-outlined text-primary text-base shrink-0">devices</span>
                    {{ plan.maxSeats }} Device{{ plan.maxSeats !== 1 ? 's' : '' }}
                  </li>
                  <li class="flex items-center gap-3 text-on-surface-variant font-body text-body-sm">
                    <span class="material-symbols-outlined text-primary text-base shrink-0">payments</span>
                    Billing: {{ formatBillingCycle(plan.billingCycle) }}
                  </li>
                </ul>

                <!-- CTA -->
                <button
                  v-if="plan.trial"
                  class="w-full py-3 px-4 rounded-lg bg-secondary/10 text-secondary border border-secondary/20 font-bold hover:bg-secondary/20 transition-colors flex items-center justify-center gap-2"
                >
                  Start Trial
                  <span class="material-symbols-outlined text-sm">arrow_forward</span>
                </button>
                <button
                  v-else
                  class="w-full py-3 px-4 rounded-lg bg-primary text-on-primary font-bold hover:bg-primary/90 transition-colors flex items-center justify-center gap-2 shadow-sm"
                >
                  Buy Now
                  <span class="material-symbols-outlined text-sm">shopping_cart</span>
                </button>
              </div>
            </div>
          </div>
        </div>

      </template>

    </div>
  </DashboardLayout>
</template>
