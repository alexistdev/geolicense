<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import adminInvoiceService from '@/modules/administrator/billing/invoices/services/invoice.service.ts'
import type { InvoiceDetail } from '@/modules/user/invoice/models/invoice.response.ts'

const route = useRoute()
const router = useRouter()

const invoice = ref<InvoiceDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const invoiceId = computed(() => route.params['id'] as string)

async function fetchDetail() {
  if (!invoiceId.value) return
  loading.value = true
  error.value = null
  try {
    const res = await adminInvoiceService.getInvoiceDetail(invoiceId.value)
    if (res.status) {
      invoice.value = res.payload
    } else {
      error.value = res.messages[0] ?? 'Failed to load invoice.'
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    error.value = err.response?.data?.messages?.[0] ?? 'Failed to load invoice.'
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr: string): string {
  return new Intl.DateTimeFormat('en-US', { dateStyle: 'medium' }).format(new Date(dateStr))
}

function formatAmount(amount: number, currency: string): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount)
}

function invoiceStatus(status: number): { label: string; cls: string } {
  if (status === 1) return { label: 'Paid', cls: 'bg-green-500/10 text-green-400 border border-green-500/20' }
  if (status === 2) return { label: 'Cancelled', cls: 'bg-error/10 text-error border border-error/20' }
  return { label: 'Pending', cls: 'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20' }
}

onMounted(() => fetchDetail())
</script>

<template>
  <DashboardLayout>
    <div class="p-8">

      <!-- Loading skeleton -->
      <template v-if="loading">
        <div class="animate-pulse space-y-6">
          <div class="h-5 w-32 rounded bg-surface-container"></div>
          <div class="h-10 w-80 rounded bg-surface-container"></div>
          <div class="grid grid-cols-12 gap-6">
            <div class="col-span-12 lg:col-span-4 h-48 rounded-xl bg-surface-container"></div>
            <div class="col-span-12 lg:col-span-8 h-48 rounded-xl bg-surface-container"></div>
            <div class="col-span-12 h-64 rounded-xl bg-surface-container"></div>
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
            @click="router.push({ name: 'admin-invoice' })"
          >
            Back to Invoices
          </button>
        </div>
      </template>

      <!-- Detail content -->
      <template v-else-if="invoice">

        <!-- Header -->
        <div class="mb-10">
          <button
            class="text-primary hover:text-primary-fixed transition-colors flex items-center gap-1 text-sm font-medium mb-6"
            @click="router.push({ name: 'admin-invoice' })"
          >
            <span class="material-symbols-outlined text-base">arrow_back</span> Back to Invoices
          </button>
          <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
            <div>
              <span class="text-[0.6875rem] font-bold uppercase tracking-[0.2em] text-primary">Billing</span>
              <h1 class="text-4xl font-black tracking-tight text-white mt-1">Invoice Detail</h1>
              <p class="text-on-surface-variant text-sm mt-1 font-mono">{{ invoice.invoiceNumber }}</p>
            </div>
            <span
              class="self-start md:self-auto px-4 py-1.5 rounded-full text-[0.6875rem] font-black uppercase tracking-widest"
              :class="invoiceStatus(invoice.status).cls"
            >
              {{ invoiceStatus(invoice.status).label }}
            </span>
          </div>
        </div>

        <div class="grid grid-cols-12 gap-6">

          <!-- Amount card -->
          <div class="col-span-12 lg:col-span-4 bg-surface-container p-8 rounded-xl relative overflow-hidden flex flex-col justify-between min-h-[200px]">
            <div class="relative z-10">
              <h3 class="text-on-surface-variant font-medium uppercase tracking-[0.1em] text-xs mb-6 flex items-center gap-2">
                <span class="material-symbols-outlined text-primary text-lg">payments</span> Amount Due
              </h3>
              <div class="mb-2">
                <span class="text-5xl font-black text-white tracking-tight">
                  {{ formatAmount(invoice.totalAmount, invoice.currency) }}
                </span>
              </div>
              <p class="text-on-surface-variant text-xs font-semibold uppercase tracking-widest">{{ invoice.currency }}</p>
            </div>
            <div class="absolute -right-10 -bottom-10 w-48 h-48 bg-primary/5 rounded-full blur-3xl"></div>
          </div>

          <!-- Invoice metadata -->
          <div class="col-span-12 lg:col-span-8 bg-surface-container p-8 rounded-xl border border-outline-variant/10">
            <h3 class="text-on-surface-variant font-medium uppercase tracking-[0.1em] text-xs mb-8 flex items-center gap-2">
              <span class="material-symbols-outlined text-primary text-lg">receipt_long</span> Invoice Metadata
            </h3>
            <div class="grid grid-cols-2 gap-y-8">
              <div>
                <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">Invoice Number</p>
                <p class="text-sm font-bold text-on-surface font-mono">{{ invoice.invoiceNumber }}</p>
              </div>
              <div>
                <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">Order Number</p>
                <p class="text-sm font-bold text-on-surface font-mono">{{ invoice.orderNumber }}</p>
              </div>
              <div>
                <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">Issued Date</p>
                <p class="text-sm font-bold text-on-surface">{{ formatDate(invoice.issuedAt) }}</p>
              </div>
              <div>
                <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">Status</p>
                <span
                  class="px-3 py-1 rounded-full text-[0.65rem] font-black uppercase tracking-wider"
                  :class="invoiceStatus(invoice.status).cls"
                >
                  {{ invoiceStatus(invoice.status).label }}
                </span>
              </div>
              <div>
                <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-1">Unique Code</p>
                <p class="text-sm font-bold text-on-surface font-mono">{{ invoice.uniqueCode }}</p>
              </div>
            </div>
          </div>

          <!-- Order items table -->
          <div class="col-span-12 bg-surface rounded-2xl shadow-2xl overflow-hidden border border-white/5">
            <div class="p-6 flex items-center gap-2 bg-surface-container-low/50 border-b border-white/5">
              <span class="material-symbols-outlined text-primary">inventory_2</span>
              <h3 class="font-bold text-on-surface">Order Items</h3>
              <span class="ml-auto text-xs text-on-surface-variant font-medium">{{ invoice.items.length }} item{{ invoice.items.length !== 1 ? 's' : '' }}</span>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full text-left border-collapse">
                <thead>
                  <tr class="bg-surface-container-lowest/30">
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Product</th>
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Plan</th>
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Billing</th>
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Duration</th>
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Qty</th>
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Unit Price</th>
                    <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Total</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-white/[0.03]">
                  <tr v-if="invoice.items.length === 0">
                    <td colspan="7" class="px-6 py-16 text-center text-on-surface-variant text-sm">
                      <span class="material-symbols-outlined text-4xl block mb-2 opacity-40">inventory_2</span>
                      No items found.
                    </td>
                  </tr>
                  <tr
                    v-else
                    v-for="(item, idx) in invoice.items"
                    :key="idx"
                    class="hover:bg-surface-container-low transition-colors"
                  >
                    <td class="px-6 py-5">
                      <p class="text-sm font-bold text-on-surface">{{ item.productName }}</p>
                      <p class="text-xs text-on-surface-variant mt-0.5">v{{ item.productVersion }} · {{ item.licenseTypeName }}</p>
                    </td>
                    <td class="px-6 py-5">
                      <div class="flex items-center gap-2">
                        <span class="text-sm font-semibold text-on-surface">{{ item.planName }}</span>
                        <span
                          v-if="item.isTrial"
                          class="px-2 py-0.5 rounded-full text-[0.6rem] font-black uppercase tracking-wider bg-secondary-container text-on-secondary-container"
                        >Trial</span>
                      </div>
                    </td>
                    <td class="px-6 py-5">
                      <span class="text-sm text-on-surface-variant capitalize">{{ item.billingCycle.toLowerCase() }}</span>
                    </td>
                    <td class="px-6 py-5">
                      <span class="text-sm text-on-surface-variant">{{ item.durationDays }}d</span>
                    </td>
                    <td class="px-6 py-5">
                      <span class="text-sm font-semibold text-on-surface">{{ item.quantity }}</span>
                    </td>
                    <td class="px-6 py-5">
                      <span class="text-sm text-on-surface-variant">{{ formatAmount(item.unitPrice, invoice.currency) }}</span>
                    </td>
                    <td class="px-6 py-5">
                      <span class="text-sm font-bold text-white">{{ formatAmount(item.totalPrice, invoice.currency) }}</span>
                    </td>
                  </tr>
                </tbody>
                <tfoot class="border-t border-white/10">
                  <tr class="bg-surface-container-low/10">
                    <td colspan="6" class="px-6 py-3 text-right text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Subtotal</td>
                    <td class="px-6 py-3">
                      <span class="text-sm text-on-surface">{{ formatAmount(invoice.amount, invoice.currency) }}</span>
                    </td>
                  </tr>
                  <tr class="bg-surface-container-low/10">
                    <td colspan="6" class="px-6 py-3 text-right text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Discount</td>
                    <td class="px-6 py-3">
                      <span class="text-sm text-green-400">− {{ formatAmount(invoice.discount, invoice.currency) }}</span>
                    </td>
                  </tr>
                  <tr class="bg-surface-container-low/10">
                    <td colspan="6" class="px-6 py-3 text-right text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Tax</td>
                    <td class="px-6 py-3">
                      <span class="text-sm text-on-surface-variant">+ {{ formatAmount(invoice.tax, invoice.currency) }}</span>
                    </td>
                  </tr>
                  <tr class="bg-surface-container-low/10">
                    <td colspan="6" class="px-6 py-3 text-right text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Unique Code</td>
                    <td class="px-6 py-3">
                      <span class="text-sm font-mono text-on-surface-variant">+ {{ invoice.uniqueCode }}</span>
                    </td>
                  </tr>
                  <tr class="bg-surface-container-low/30 border-t border-white/10">
                    <td colspan="6" class="px-6 py-4 text-right text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Total</td>
                    <td class="px-6 py-4">
                      <span class="text-base font-black text-primary">{{ formatAmount(invoice.totalAmount, invoice.currency) }}</span>
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>

        </div>
      </template>

    </div>
  </DashboardLayout>
</template>
