<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import marketplaceService from '@/modules/user/marketplace/services/marketplace.service.ts'
import orderService from '@/modules/user/order/services/order.service.ts'
import type { ProductDetail, ProductPlan } from '@/modules/user/marketplace/models/marketplace.response.ts'
import type { CreateOrderResponse } from '@/modules/user/order/models/order.model.ts'

const route = useRoute()
const router = useRouter()
const product = ref<ProductDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const buyingPlanId = ref<string | null>(null)
const orderResult = ref<CreateOrderResponse | null>(null)
const orderError = ref<string | null>(null)

const confirmPlan = ref<ProductPlan | null>(null)

function openConfirm(plan: ProductPlan) {
  orderResult.value = null
  orderError.value = null
  confirmPlan.value = plan
}

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

async function confirmOrder() {
  if (!confirmPlan.value) return
  const planId = confirmPlan.value.planId
  confirmPlan.value = null
  buyingPlanId.value = planId
  try {
    const res = await orderService.createOrder({ licensePlanId: planId, quantity: 1 })
    orderResult.value = res.payload
  } catch {
    orderError.value = 'Failed to place order. Please try again.'
  } finally {
    buyingPlanId.value = null
  }
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

          <!-- Order feedback -->
          <div
            v-if="orderResult"
            class="flex items-start gap-3 rounded-xl bg-primary/10 border border-primary/20 px-5 py-4 text-on-surface"
          >
            <span class="material-symbols-outlined text-primary text-xl shrink-0 mt-0.5" style="font-variation-settings: 'FILL' 1">check_circle</span>
            <div class="flex-1">
              <p class="font-semibold text-sm">Order placed successfully!</p>
              <p class="text-on-surface-variant text-sm mt-0.5">
                Order <span class="font-mono font-semibold">{{ orderResult.orderNumber }}</span> —
                {{ orderResult.currency }} {{ orderResult.totalAmount.toLocaleString('id-ID') }}
              </p>
            </div>
            <button
              class="text-on-surface-variant hover:text-on-surface transition-colors"
              @click="orderResult = null"
            >
              <span class="material-symbols-outlined text-base">close</span>
            </button>
          </div>

          <div
            v-if="orderError"
            class="flex items-center gap-3 rounded-xl bg-error/10 border border-error/20 px-5 py-4 text-error text-sm font-medium"
          >
            <span class="material-symbols-outlined text-base shrink-0">error</span>
            {{ orderError }}
            <button class="ml-auto text-error/60 hover:text-error transition-colors" @click="orderError = null">
              <span class="material-symbols-outlined text-base">close</span>
            </button>
          </div>

          <!-- Plans grid -->
          <div v-if="product.plans.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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
                  :disabled="buyingPlanId === plan.planId"
                  class="w-full py-3 px-4 rounded-lg bg-secondary/10 text-secondary border border-secondary/20 font-bold hover:bg-secondary/20 transition-colors flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
                  @click="openConfirm(plan)"
                >
                  <span v-if="buyingPlanId === plan.planId" class="material-symbols-outlined text-sm animate-spin">progress_activity</span>
                  <template v-else>
                    Start Trial
                    <span class="material-symbols-outlined text-sm">arrow_forward</span>
                  </template>
                </button>
                <button
                  v-else
                  :disabled="buyingPlanId === plan.planId"
                  class="w-full py-3 px-4 rounded-lg bg-primary text-on-primary font-bold hover:bg-primary/90 transition-colors flex items-center justify-center gap-2 shadow-sm disabled:opacity-60 disabled:cursor-not-allowed"
                  @click="openConfirm(plan)"
                >
                  <span v-if="buyingPlanId === plan.planId" class="material-symbols-outlined text-sm animate-spin">progress_activity</span>
                  <template v-else>
                    Buy Now
                    <span class="material-symbols-outlined text-sm">shopping_cart</span>
                  </template>
                </button>
              </div>
            </div>
          </div>
        </div>

      </template>

    </div>

    <!-- Order confirmation modal -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="confirmPlan"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="confirmPlan = null"
        >
          <!-- Backdrop -->
          <div class="absolute inset-0 bg-scrim/60 backdrop-blur-sm"></div>

          <!-- Dialog -->
          <Transition
            enter-active-class="transition duration-200 ease-out"
            enter-from-class="opacity-0 scale-95"
            enter-to-class="opacity-100 scale-100"
            leave-active-class="transition duration-150 ease-in"
            leave-from-class="opacity-100 scale-100"
            leave-to-class="opacity-0 scale-95"
          >
            <div
              v-if="confirmPlan"
              class="relative z-10 w-full max-w-md bg-surface-container-low rounded-2xl shadow-2xl overflow-hidden"
            >
              <!-- Modal header -->
              <div class="px-6 pt-6 pb-4 border-b border-outline-variant/15">
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="font-label text-label-sm uppercase tracking-widest text-primary mb-1">
                      {{ confirmPlan.trial ? 'Start Free Trial' : 'Confirm Purchase' }}
                    </p>
                    <h3 class="font-display text-xl font-bold text-on-surface">
                      {{ confirmPlan.planName }}
                    </h3>
                  </div>
                  <button
                    class="text-on-surface-variant hover:text-on-surface transition-colors mt-0.5 shrink-0"
                    @click="confirmPlan = null"
                  >
                    <span class="material-symbols-outlined">close</span>
                  </button>
                </div>
              </div>

              <!-- Modal body -->
              <div class="px-6 py-5 space-y-5">
                <!-- Product name -->
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg bg-surface-container-highest flex items-center justify-center text-primary shrink-0">
                    <span class="material-symbols-outlined text-xl" style="font-variation-settings: 'FILL' 1">workspace_premium</span>
                  </div>
                  <div>
                    <p class="font-body text-body-sm text-on-surface-variant">Product</p>
                    <p class="font-semibold text-on-surface text-sm">{{ product?.name }}</p>
                  </div>
                </div>

                <!-- Order summary rows -->
                <div class="rounded-xl bg-surface-container divide-y divide-outline-variant/10">
                  <div class="flex items-center justify-between px-4 py-3">
                    <span class="font-body text-body-sm text-on-surface-variant flex items-center gap-2">
                      <span class="material-symbols-outlined text-base text-primary">payments</span>
                      Billing
                    </span>
                    <span class="font-semibold text-sm text-on-surface">{{ formatBillingCycle(confirmPlan.billingCycle) }}</span>
                  </div>
                  <div class="flex items-center justify-between px-4 py-3">
                    <span class="font-body text-body-sm text-on-surface-variant flex items-center gap-2">
                      <span class="material-symbols-outlined text-base text-primary">schedule</span>
                      Duration
                    </span>
                    <span class="font-semibold text-sm text-on-surface">
                      {{ confirmPlan.durationDays }} Day{{ confirmPlan.durationDays !== 1 ? 's' : '' }}
                    </span>
                  </div>
                  <div class="flex items-center justify-between px-4 py-3">
                    <span class="font-body text-body-sm text-on-surface-variant flex items-center gap-2">
                      <span class="material-symbols-outlined text-base text-primary">devices</span>
                      Max Devices
                    </span>
                    <span class="font-semibold text-sm text-on-surface">
                      {{ confirmPlan.maxSeats }} Device{{ confirmPlan.maxSeats !== 1 ? 's' : '' }}
                    </span>
                  </div>
                </div>

                <!-- Price total -->
                <div class="flex items-center justify-between rounded-xl bg-primary/8 border border-primary/15 px-4 py-4">
                  <span class="font-body text-body-sm text-on-surface-variant">Total</span>
                  <span class="font-display text-2xl font-extrabold text-on-surface">
                    {{ formatPrice(confirmPlan) }}
                  </span>
                </div>
              </div>

              <!-- Modal footer -->
              <div class="px-6 pb-6 flex gap-3">
                <button
                  class="flex-1 py-3 px-4 rounded-lg bg-surface-container text-on-surface-variant font-semibold hover:bg-surface-container-high transition-colors text-sm"
                  @click="confirmPlan = null"
                >
                  Cancel
                </button>
                <button
                  v-if="confirmPlan.trial"
                  class="flex-1 py-3 px-4 rounded-lg bg-secondary text-on-secondary font-bold hover:bg-secondary/90 transition-colors flex items-center justify-center gap-2 text-sm shadow-sm"
                  @click="confirmOrder"
                >
                  Start Trial
                  <span class="material-symbols-outlined text-sm">arrow_forward</span>
                </button>
                <button
                  v-else
                  class="flex-1 py-3 px-4 rounded-lg bg-primary text-on-primary font-bold hover:bg-primary/90 transition-colors flex items-center justify-center gap-2 text-sm shadow-sm"
                  @click="confirmOrder"
                >
                  Confirm & Pay
                  <span class="material-symbols-outlined text-sm">shopping_cart</span>
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

  </DashboardLayout>
</template>
