<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import invoiceService, { type SubmitPaymentPayload } from '@/modules/user/invoice/services/invoice.service.ts'
import type { InvoiceDetail } from '@/modules/user/invoice/models/invoice.response.ts'

const BANK_ACCOUNTS = [
  { bank: 'BCA', accountNumber: '1234567890', accountHolder: 'PT Geo License Indonesia' },
  { bank: 'Mandiri', accountNumber: '9876543210', accountHolder: 'PT Geo License Indonesia' },
  { bank: 'BNI', accountNumber: '4567891230', accountHolder: 'PT Geo License Indonesia' },
  { bank: 'BRI', accountNumber: '3210987654', accountHolder: 'PT Geo License Indonesia' },
]

const BANK_OPTIONS = ['BCA', 'Mandiri', 'BNI', 'BRI', 'CIMB Niaga', 'Permata', 'Danamon', 'BTN', 'Other']

const route = useRoute()
const router = useRouter()

const invoice = ref<InvoiceDetail | null>(null)
const loading = ref(false)
const submitting = ref(false)
const error = ref<string | null>(null)
const successMsg = ref<string | null>(null)

const form = ref<SubmitPaymentPayload>({ provider: '', providerReference: '' })
const formError = ref<{ provider?: string; providerReference?: string }>({})

const invoiceId = computed(() => route.params['id'] as string)
const selectedBank = computed(() => BANK_ACCOUNTS.find((b) => b.bank === form.value.provider) ?? BANK_ACCOUNTS[0])

async function fetchDetail() {
  if (!invoiceId.value) return
  loading.value = true
  error.value = null
  try {
    const res = await invoiceService.getInvoiceDetail(invoiceId.value)
    if (res.status) {
      invoice.value = res.payload
      if (invoice.value.status !== 'UNPAID') {
        router.replace({ name: 'user-invoice-detail', params: { id: invoiceId.value } })
      }
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

function validate(): boolean {
  formError.value = {}
  if (!form.value.provider) formError.value.provider = 'Please select your bank.'
  if (!form.value.providerReference.trim()) formError.value.providerReference = 'Transaction reference is required.'
  return Object.keys(formError.value).length === 0
}

async function submit() {
  if (!validate()) return
  submitting.value = true
  error.value = null
  try {
    const res = await invoiceService.submitPayment(invoiceId.value, {
      provider: form.value.provider,
      providerReference: form.value.providerReference.trim(),
    })
    if (res.status) {
      successMsg.value = res.messages[0] ?? 'Payment submitted successfully.'
      setTimeout(() => {
        router.push({ name: 'user-invoice-detail', params: { id: invoiceId.value } })
      }, 2000)
    } else {
      error.value = res.messages[0] ?? 'Failed to submit payment.'
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    error.value = err.response?.data?.messages?.[0] ?? 'Failed to submit payment.'
  } finally {
    submitting.value = false
  }
}

function formatAmount(amount: number, currency: string): string {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency }).format(amount)
}

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text)
}

onMounted(() => fetchDetail())
</script>

<template>
  <DashboardLayout>
    <div class="p-8 max-w-3xl mx-auto">

      <!-- Loading -->
      <template v-if="loading">
        <div class="animate-pulse space-y-6">
          <div class="h-5 w-32 rounded bg-surface-container"></div>
          <div class="h-64 rounded-xl bg-surface-container"></div>
          <div class="h-48 rounded-xl bg-surface-container"></div>
        </div>
      </template>

      <!-- Error -->
      <template v-else-if="error && !invoice">
        <div class="flex flex-col items-center justify-center py-32 gap-4">
          <span class="material-symbols-outlined text-5xl text-error opacity-60">error</span>
          <p class="text-on-surface-variant text-sm">{{ error }}</p>
          <button
            class="px-5 py-2 rounded-md bg-surface-container text-on-surface text-sm font-semibold hover:bg-surface-container-high transition-colors"
            @click="router.push({ name: 'user-invoice' })"
          >
            Back to Invoices
          </button>
        </div>
      </template>

      <template v-else-if="invoice">

        <!-- Back -->
        <button
          class="text-primary hover:text-primary-fixed transition-colors flex items-center gap-1 text-sm font-medium mb-8"
          @click="router.push({ name: 'user-invoice-detail', params: { id: invoiceId } })"
        >
          <span class="material-symbols-outlined text-base">arrow_back</span> Back to Invoice
        </button>

        <!-- Header -->
        <div class="mb-8">
          <span class="text-[0.6875rem] font-bold uppercase tracking-[0.2em] text-primary">Billing</span>
          <h1 class="text-4xl font-black tracking-tight text-white mt-1">Payment Instructions</h1>
          <p class="text-on-surface-variant text-sm mt-1 font-mono">{{ invoice.invoiceNumber }}</p>
        </div>

        <!-- Success banner -->
        <div
          v-if="successMsg"
          class="mb-6 flex items-center gap-3 bg-green-500/10 border border-green-500/20 rounded-xl px-5 py-4"
        >
          <span class="material-symbols-outlined text-green-400 text-xl">check_circle</span>
          <p class="text-green-400 text-sm font-semibold">{{ successMsg }} Redirecting…</p>
        </div>

        <!-- Amount to transfer -->
        <div class="bg-surface-container rounded-xl p-6 mb-6 relative overflow-hidden">
          <div class="absolute -right-8 -bottom-8 w-40 h-40 bg-primary/5 rounded-full blur-3xl"></div>
          <p class="text-[0.625rem] text-on-surface-variant font-bold uppercase tracking-widest mb-2">Total Transfer Amount</p>
          <p class="text-5xl font-black text-white tracking-tight">{{ formatAmount(invoice.totalAmount, invoice.currency) }}</p>
          <p class="text-xs text-on-surface-variant mt-3 font-mono">
            Subtotal {{ formatAmount(invoice.amount, invoice.currency) }}
            <span class="text-yellow-400"> + unique code {{ invoice.uniqueCode }}</span>
          </p>
          <p class="text-[0.625rem] text-yellow-400/80 mt-1 font-medium">Transfer the <span class="font-black">exact</span> amount including the unique code so we can verify your payment automatically.</p>
        </div>

        <!-- Bank accounts -->
        <div class="bg-surface-container rounded-xl p-6 mb-6">
          <h2 class="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-5 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-lg">account_balance</span> Bank Transfer Accounts
          </h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div
              v-for="acc in BANK_ACCOUNTS"
              :key="acc.bank"
              class="bg-surface-container-high rounded-lg p-4 border border-outline-variant/10"
            >
              <p class="text-[0.625rem] font-black uppercase tracking-widest text-primary mb-2">{{ acc.bank }}</p>
              <div class="flex items-center justify-between gap-2">
                <p class="text-lg font-black text-white font-mono tracking-wider">{{ acc.accountNumber }}</p>
                <button
                  class="text-on-surface-variant hover:text-primary transition-colors"
                  title="Copy account number"
                  @click="copyToClipboard(acc.accountNumber)"
                >
                  <span class="material-symbols-outlined text-base">content_copy</span>
                </button>
              </div>
              <p class="text-xs text-on-surface-variant mt-1">{{ acc.accountHolder }}</p>
            </div>
          </div>
        </div>

        <!-- Steps -->
        <div class="bg-surface-container rounded-xl p-6 mb-6">
          <h2 class="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-5 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-lg">format_list_numbered</span> How to Pay
          </h2>
          <ol class="space-y-3 text-sm text-on-surface-variant">
            <li class="flex gap-3">
              <span class="flex-shrink-0 w-5 h-5 rounded-full bg-primary/10 text-primary text-[0.625rem] font-black flex items-center justify-center mt-0.5">1</span>
              Transfer <span class="font-black text-white mx-1">{{ formatAmount(invoice.totalAmount, invoice.currency) }}</span> to one of the bank accounts above.
            </li>
            <li class="flex gap-3">
              <span class="flex-shrink-0 w-5 h-5 rounded-full bg-primary/10 text-primary text-[0.625rem] font-black flex items-center justify-center mt-0.5">2</span>
              Make sure the amount is exact — the unique code <span class="font-mono text-yellow-400 mx-1">{{ invoice.uniqueCode }}</span> is part of the total.
            </li>
            <li class="flex gap-3">
              <span class="flex-shrink-0 w-5 h-5 rounded-full bg-primary/10 text-primary text-[0.625rem] font-black flex items-center justify-center mt-0.5">3</span>
              After transferring, fill in the form below with your bank name and the transaction reference number from your receipt.
            </li>
          </ol>
        </div>

        <!-- Confirmation form -->
        <div class="bg-surface-container rounded-xl p-6 border border-outline-variant/10">
          <h2 class="text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-lg">receipt_long</span> Confirm Your Transfer
          </h2>

          <div class="space-y-5">
            <!-- Provider -->
            <div>
              <label class="block text-[0.625rem] font-bold uppercase tracking-widest text-on-surface-variant mb-2">
                Bank You Transferred From <span class="text-error">*</span>
              </label>
              <select
                v-model="form.provider"
                class="w-full bg-surface-container-high border rounded-lg px-4 py-3 text-sm text-on-surface appearance-none focus:outline-none focus:ring-2 focus:ring-primary/40 transition-shadow"
                :class="formError.provider ? 'border-error' : 'border-outline-variant/20'"
              >
                <option value="" disabled>Select your bank</option>
                <option v-for="bank in BANK_OPTIONS" :key="bank" :value="bank">{{ bank }}</option>
              </select>
              <p v-if="formError.provider" class="text-error text-xs mt-1.5">{{ formError.provider }}</p>
            </div>

            <!-- Provider reference -->
            <div>
              <label class="block text-[0.625rem] font-bold uppercase tracking-widest text-on-surface-variant mb-2">
                Transaction Reference Number <span class="text-error">*</span>
              </label>
              <input
                v-model="form.providerReference"
                type="text"
                placeholder="e.g. TRX20260531XXXXXXXX"
                class="w-full bg-surface-container-high border rounded-lg px-4 py-3 text-sm text-on-surface placeholder:text-on-surface-variant/40 font-mono focus:outline-none focus:ring-2 focus:ring-primary/40 transition-shadow"
                :class="formError.providerReference ? 'border-error' : 'border-outline-variant/20'"
              />
              <p v-if="formError.providerReference" class="text-error text-xs mt-1.5">{{ formError.providerReference }}</p>
              <p class="text-on-surface-variant text-xs mt-1.5">The reference or transaction ID from your bank receipt or m-banking app.</p>
            </div>

            <!-- Error -->
            <div
              v-if="error"
              class="flex items-center gap-2 bg-error/10 border border-error/20 rounded-lg px-4 py-3"
            >
              <span class="material-symbols-outlined text-error text-base">error</span>
              <p class="text-error text-sm">{{ error }}</p>
            </div>

            <!-- Submit -->
            <button
              :disabled="submitting || !!successMsg"
              class="w-full flex items-center justify-center gap-2 px-6 py-3.5 rounded-lg bg-primary text-on-primary text-sm font-black uppercase tracking-widest transition-opacity disabled:opacity-50"
              @click="submit"
            >
              <span v-if="submitting" class="material-symbols-outlined text-base animate-spin">progress_activity</span>
              <span v-else class="material-symbols-outlined text-base">send</span>
              {{ submitting ? 'Submitting…' : 'Confirm Payment' }}
            </button>
          </div>
        </div>

      </template>

    </div>
  </DashboardLayout>
</template>