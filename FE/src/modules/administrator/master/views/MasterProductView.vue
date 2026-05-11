<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import MasterProductService from '@/modules/administrator/master/services/masterproduct.service'
import type { ProductPayload } from '../models/product.response'

const PAGE_SIZE = 10

const products = ref<ProductPayload[]>([])
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const error = ref<string | null>(null)
const filterName = ref('')

let filterTimer: ReturnType<typeof setTimeout> | null = null
function onFilterInput() {
  if (filterTimer) clearTimeout(filterTimer)
  filterTimer = setTimeout(() => {
    currentPage.value = 0
    fetchProducts()
  }, 300)
}

async function fetchProducts() {
  loading.value = true
  error.value = null
  try {
    const params = {
      page: currentPage.value,
      size: PAGE_SIZE,
      sortBy: 'createdDate',
      direction: 'desc' as const,
    }
    const res = filterName.value.trim()
      ? await MasterProductService.getAllByFilter({ filter: filterName.value.trim(), ...params })
      : await MasterProductService.getAll(params)
    const page = res.payload
    products.value = page.content
    totalElements.value = page.totalElements
    totalPages.value = page.totalPages
  } catch {
    error.value = 'Failed to load products. Please try again.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchProducts)

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchProducts()
}

const showingFrom = computed(() =>
  totalElements.value === 0 ? 0 : currentPage.value * PAGE_SIZE + 1,
)
const showingTo = computed(() => Math.min((currentPage.value + 1) * PAGE_SIZE, totalElements.value))

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

function statusBadgeClass(active: boolean) {
  return active
    ? 'bg-primary-fixed text-on-primary-fixed-variant'
    : 'bg-error-container text-on-error-container'
}

function statusDotClass(active: boolean) {
  return active ? 'bg-primary animate-pulse' : 'bg-error'
}

// --- Toast ---
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

// --- Add Product Modal ---
const showModal = ref(false)
const modalLoading = ref(false)
const modalError = ref<string | null>(null)

interface ProductForm {
  name: string
  version: string
  description: string
  sku: string
  isActive: boolean
}

const form = ref<ProductForm>({
  name: '',
  version: '',
  description: '',
  sku: '',
  isActive: true,
})

function openModal() {
  form.value = { name: '', version: '', description: '', sku: '', isActive: true }
  modalError.value = null
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function submitProduct() {
  modalError.value = null
  if (!form.value.name.trim() || !form.value.version.trim() || !form.value.sku.trim()) {
    modalError.value = 'Name, version, and SKU are required.'
    return
  }
  modalLoading.value = true
  try {
    await MasterProductService.addProduct({
      name: form.value.name.trim(),
      version: form.value.version.trim(),
      description: form.value.description.trim(),
      sku: form.value.sku.trim(),
      isActive: form.value.isActive,
    })
    closeModal()
    showToast('Product successfully added.')
    await fetchProducts()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    closeModal()
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to add product. Please try again.',
      'error',
    )
  } finally {
    modalLoading.value = false
  }
}

// --- Delete Product Modal ---
const showDeleteModal = ref(false)
const deleteLoading = ref(false)
const deleteTarget = ref<ProductPayload | null>(null)

function openDeleteModal(product: ProductPayload) {
  deleteTarget.value = product
  showDeleteModal.value = true
}

function closeDeleteModal() {
  showDeleteModal.value = false
  deleteTarget.value = null
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleteLoading.value = true
  try {
    await MasterProductService.deleteProduct(deleteTarget.value.id)
    closeDeleteModal()
    showToast('Product successfully deleted.')
    await fetchProducts()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    closeDeleteModal()
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to delete product. Please try again.',
      'error',
    )
  } finally {
    deleteLoading.value = false
  }
}

// --- Edit Product Modal ---
const showEditModal = ref(false)
const editModalLoading = ref(false)
const editModalError = ref<string | null>(null)

interface EditProductForm {
  id: string
  name: string
  version: string
  description: string
  sku: string
  isActive: boolean
}

const editForm = ref<EditProductForm>({
  id: '',
  name: '',
  version: '',
  description: '',
  sku: '',
  isActive: true,
})

function openEditModal(product: ProductPayload) {
  editForm.value = {
    id: product.id,
    name: product.name,
    version: product.version,
    description: product.description,
    sku: product.sku,
    isActive: product.active,
  }
  editModalError.value = null
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
}

async function submitEditProduct() {
  editModalError.value = null
  if (!editForm.value.name.trim() || !editForm.value.version.trim() || !editForm.value.sku.trim()) {
    editModalError.value = 'Name, version, and SKU are required.'
    return
  }
  editModalLoading.value = true
  try {
    await MasterProductService.updateProduct({
      id: editForm.value.id,
      name: editForm.value.name.trim(),
      version: editForm.value.version.trim(),
      description: editForm.value.description.trim(),
      sku: editForm.value.sku.trim(),
      isActive: editForm.value.isActive,
    })
    closeEditModal()
    showToast('Product successfully updated.')
    await fetchProducts()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    closeEditModal()
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to update product. Please try again.',
      'error',
    )
  } finally {
    editModalLoading.value = false
  }
}
</script>

<template>
  <DashboardLayout>
    <main class="p-12 min-h-screen">
      <!-- Header -->
      <section class="grid grid-cols-12 gap-6 mb-12">
        <div class="col-span-12 lg:col-span-8">
          <h2 class="text-4xl font-headline font-extrabold text-on-surface tracking-tight mb-2">
            Product Catalog
          </h2>
          <p class="text-on-surface-variant font-body text-lg">
            Manage Products for GeoLicense
          </p>
        </div>
        <div class="col-span-12 lg:col-span-4 flex justify-end items-end gap-4">
          <button
            class="px-6 py-3 bg-surface-container-high text-on-surface font-semibold rounded-xl hover:bg-surface-container-highest transition-colors flex items-center gap-2"
          >
            <span class="material-symbols-outlined text-xl">ios_share</span>
            Export CSV
          </button>
          <button
            class="px-8 py-3 bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold rounded-xl shadow-lg shadow-primary/20 active:scale-95 transition-transform flex items-center gap-2"
            @click="openModal"
          >
            <span class="material-symbols-outlined text-xl">add_box</span>
            Add New Product
          </button>
        </div>
      </section>

      <!-- Metrics Cards -->
      <section class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Total Products
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ totalElements.toLocaleString() }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-primary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-fixed-variant">inventory_2</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Active
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ products.filter((p) => p.active).length }}
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
              Inactive
            </p>
            <h3 class="text-3xl font-headline font-bold text-error">
              {{ products.filter((p) => !p.active).length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-error-container rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-error-container">cancel</span>
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
              {{ products.length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-secondary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-secondary-fixed-variant">list_alt</span>
          </div>
        </div>
      </section>

      <!-- Main Content Area: Data Table -->
      <div
        class="bg-surface-container-lowest rounded-xl shadow-xl shadow-black/[0.03] overflow-hidden"
      >
        <!-- Table Controls -->
        <div class="p-6 bg-surface-container-low flex flex-wrap items-center justify-between gap-4">
          <div class="flex items-center gap-4">
            <div class="relative">
              <span
                class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline"
                >search</span
              >
              <input
                v-model="filterName"
                type="text"
                placeholder="Search by name..."
                class="pl-10 pr-4 py-2.5 bg-surface-container-lowest border-none rounded-lg text-sm text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary/20 w-64"
                @input="onFilterInput"
              />
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-xs font-bold text-outline uppercase tracking-widest"
              >Bulk Actions:</span
            >
            <button
              class="px-4 py-2 bg-surface-container-high text-error font-bold text-sm rounded-lg hover:bg-error-container transition active:scale-95"
            >
              Deactivate Selected
            </button>
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

        <!-- The Table -->
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-surface-container-low border-b border-surface-container-high">
                <th class="px-6 py-4 w-12">
                  <input
                    class="rounded border-outline-variant text-primary focus:ring-primary h-4 w-4"
                    type="checkbox"
                    aria-label="Select all products"
                  />
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  NAME
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  SKU
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  VERSION
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  DESCRIPTION
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  STATUS
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest text-right"
                >
                  ACTIONS
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-surface-container">
              <!-- Loading skeleton -->
              <template v-if="loading">
                <tr v-for="n in PAGE_SIZE" :key="n" class="animate-pulse">
                  <td class="px-6 py-4">
                    <div class="h-4 w-4 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-4">
                      <div class="w-10 h-10 bg-surface-container rounded-xl"></div>
                      <div class="space-y-2">
                        <div class="h-3 w-32 bg-surface-container rounded"></div>
                        <div class="h-2 w-24 bg-surface-container rounded"></div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-24 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-16 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-40 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-6 w-20 bg-surface-container rounded-full"></div>
                  </td>
                  <td class="px-6 py-4 text-right">
                    <div class="h-8 w-20 bg-surface-container rounded-lg ml-auto"></div>
                  </td>
                </tr>
              </template>

              <!-- Empty state -->
              <tr v-else-if="products.length === 0">
                <td colspan="7" class="px-6 py-16 text-center text-on-surface-variant">
                  <span class="material-symbols-outlined text-4xl block mb-2">inventory_2</span>
                  No products found.
                </td>
              </tr>

              <!-- Data rows -->
              <tr
                v-else
                v-for="product in products"
                :key="product.id"
                class="hover:bg-surface-container-low transition-colors"
              >
                <td class="px-6 py-4">
                  <input
                    class="rounded border-outline-variant text-primary focus:ring-primary h-4 w-4"
                    type="checkbox"
                    :aria-label="`Select ${product.name}`"
                  />
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center gap-4">
                    <div
                      class="w-10 h-10 rounded-xl bg-secondary-fixed flex items-center justify-center shrink-0"
                    >
                      <span class="material-symbols-outlined text-on-secondary-fixed-variant text-lg">deployed_code</span>
                    </div>
                    <p class="font-headline font-bold text-on-surface leading-tight">{{ product.name }}</p>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span class="text-sm font-mono font-medium text-on-surface-variant">{{ product.sku }}</span>
                </td>
                <td class="px-6 py-4">
                  <span
                    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-tertiary-fixed text-on-tertiary-fixed-variant"
                  >
                    v{{ product.version }}
                  </span>
                </td>
                <td class="px-6 py-4 max-w-xs">
                  <p class="text-sm text-on-surface-variant truncate">{{ product.description || '—' }}</p>
                </td>
                <td class="px-6 py-4">
                  <span
                    class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold"
                    :class="statusBadgeClass(product.active)"
                  >
                    <span
                      class="w-1.5 h-1.5 rounded-full"
                      :class="statusDotClass(product.active)"
                    ></span>
                    {{ product.active ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <button
                    class="p-2 text-outline hover:text-primary hover:bg-primary-fixed transition-all rounded-lg"
                    aria-label="Edit product"
                    @click="openEditModal(product)"
                  >
                    <span class="material-symbols-outlined">edit</span>
                  </button>
                  <button
                    class="p-2 text-outline hover:text-error hover:bg-error-container transition-all rounded-lg"
                    aria-label="Delete product"
                    @click="openDeleteModal(product)"
                  >
                    <span class="material-symbols-outlined">delete</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="px-6 py-6 flex items-center justify-between border-t border-surface-container">
          <p class="text-sm text-on-surface-variant">
            Showing
            <span class="font-bold text-on-surface">{{ showingFrom }} - {{ showingTo }}</span>
            of
            <span class="font-bold text-on-surface">{{ totalElements.toLocaleString() }}</span>
            products
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

    <!-- Delete Confirmation Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showDeleteModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeDeleteModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>

          <div class="relative w-full max-w-sm bg-surface-container-lowest rounded-2xl shadow-2xl overflow-hidden">
            <!-- Header -->
            <div class="flex items-center justify-between px-6 py-5 border-b border-surface-container">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-error-container flex items-center justify-center">
                  <span class="material-symbols-outlined text-on-error-container text-lg">delete_forever</span>
                </div>
                <h3 class="text-lg font-headline font-bold text-on-surface">Delete Product</h3>
              </div>
              <button
                class="p-1.5 rounded-lg text-outline hover:bg-surface-container hover:text-on-surface transition-colors"
                aria-label="Close modal"
                @click="closeDeleteModal"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>

            <!-- Body -->
            <div class="px-6 py-6 space-y-5">
              <p class="text-sm text-on-surface-variant">
                Are you sure you want to delete
                <span class="font-bold text-on-surface">{{ deleteTarget?.name }}</span>?
                This action cannot be undone.
              </p>

              <div class="flex gap-3">
                <button
                  type="button"
                  class="flex-1 py-3 rounded-xl bg-surface-container text-on-surface font-semibold text-sm hover:bg-surface-container-high transition-colors"
                  :disabled="deleteLoading"
                  @click="closeDeleteModal"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  class="flex-1 py-3 rounded-xl bg-error text-on-error font-bold text-sm shadow-lg shadow-error/20 active:scale-95 transition-transform flex items-center justify-center gap-2 disabled:opacity-60 disabled:pointer-events-none"
                  :disabled="deleteLoading"
                  @click="confirmDelete"
                >
                  <span v-if="deleteLoading" class="material-symbols-outlined text-base animate-spin">progress_activity</span>
                  <span v-else class="material-symbols-outlined text-base">delete_forever</span>
                  {{ deleteLoading ? 'Deleting...' : 'Delete' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Edit Product Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showEditModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeEditModal"
        >
          <!-- Backdrop -->
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>

          <!-- Dialog -->
          <div class="relative w-full max-w-md bg-surface-container-lowest rounded-2xl shadow-2xl overflow-hidden">
            <!-- Header -->
            <div class="flex items-center justify-between px-6 py-5 border-b border-surface-container">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-primary-fixed flex items-center justify-center">
                  <span class="material-symbols-outlined text-on-primary-fixed-variant text-lg">edit</span>
                </div>
                <h3 class="text-lg font-headline font-bold text-on-surface">Edit Product</h3>
              </div>
              <button
                class="p-1.5 rounded-lg text-outline hover:bg-surface-container hover:text-on-surface transition-colors"
                aria-label="Close modal"
                @click="closeEditModal"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>

            <!-- Body -->
            <form class="px-6 py-6 space-y-5" @submit.prevent="submitEditProduct">
              <!-- Name -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Product Name</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">deployed_code</span>
                  <input
                    v-model="editForm.name"
                    type="text"
                    placeholder="e.g. GeoLicense Pro"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm"
                    :disabled="editModalLoading"
                  />
                </div>
              </div>

              <!-- SKU -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">SKU</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">qr_code</span>
                  <input
                    v-model="editForm.sku"
                    type="text"
                    placeholder="e.g. GLP-001"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm font-mono"
                    :disabled="editModalLoading"
                  />
                </div>
              </div>

              <!-- Version -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Version</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">tag</span>
                  <input
                    v-model="editForm.version"
                    type="text"
                    placeholder="e.g. 1.0.0"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm"
                    :disabled="editModalLoading"
                  />
                </div>
              </div>

              <!-- Description -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Description</label>
                <textarea
                  v-model="editForm.description"
                  rows="3"
                  placeholder="Short product description..."
                  class="w-full px-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm resize-none"
                  :disabled="editModalLoading"
                ></textarea>
              </div>

              <!-- Active toggle -->
              <div class="flex items-center justify-between py-1">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Active</label>
                <button
                  type="button"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary/30"
                  :class="editForm.isActive ? 'bg-primary' : 'bg-surface-container-high'"
                  :disabled="editModalLoading"
                  @click="editForm.isActive = !editForm.isActive"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform"
                    :class="editForm.isActive ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>

              <!-- Validation error -->
              <p v-if="editModalError" class="text-sm text-error font-medium">{{ editModalError }}</p>

              <!-- Actions -->
              <div class="flex gap-3 pt-2">
                <button
                  type="button"
                  class="flex-1 py-3 rounded-xl bg-surface-container text-on-surface font-semibold text-sm hover:bg-surface-container-high transition-colors"
                  :disabled="editModalLoading"
                  @click="closeEditModal"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  class="flex-1 py-3 rounded-xl bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold text-sm shadow-lg shadow-primary/20 active:scale-95 transition-transform flex items-center justify-center gap-2 disabled:opacity-60 disabled:pointer-events-none"
                  :disabled="editModalLoading"
                >
                  <span v-if="editModalLoading" class="material-symbols-outlined text-base animate-spin">progress_activity</span>
                  <span v-else class="material-symbols-outlined text-base">save</span>
                  {{ editModalLoading ? 'Saving...' : 'Save Changes' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Add Product Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeModal"
        >
          <!-- Backdrop -->
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>

          <!-- Dialog -->
          <div class="relative w-full max-w-md bg-surface-container-lowest rounded-2xl shadow-2xl overflow-hidden">
            <!-- Header -->
            <div class="flex items-center justify-between px-6 py-5 border-b border-surface-container">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-secondary-fixed flex items-center justify-center">
                  <span class="material-symbols-outlined text-on-secondary-fixed-variant text-lg">add_box</span>
                </div>
                <h3 class="text-lg font-headline font-bold text-on-surface">Add New Product</h3>
              </div>
              <button
                class="p-1.5 rounded-lg text-outline hover:bg-surface-container hover:text-on-surface transition-colors"
                aria-label="Close modal"
                @click="closeModal"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>

            <!-- Body -->
            <form class="px-6 py-6 space-y-5" @submit.prevent="submitProduct">
              <!-- Name -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Product Name</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">deployed_code</span>
                  <input
                    v-model="form.name"
                    type="text"
                    placeholder="e.g. GeoLicense Pro"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm"
                    :disabled="modalLoading"
                  />
                </div>
              </div>

              <!-- SKU -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">SKU</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">qr_code</span>
                  <input
                    v-model="form.sku"
                    type="text"
                    placeholder="e.g. GLP-001"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm font-mono"
                    :disabled="modalLoading"
                  />
                </div>
              </div>

              <!-- Version -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Version</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">tag</span>
                  <input
                    v-model="form.version"
                    type="text"
                    placeholder="e.g. 1.0.0"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm"
                    :disabled="modalLoading"
                  />
                </div>
              </div>

              <!-- Description -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Description</label>
                <textarea
                  v-model="form.description"
                  rows="3"
                  placeholder="Short product description..."
                  class="w-full px-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm resize-none"
                  :disabled="modalLoading"
                ></textarea>
              </div>

              <!-- Active toggle -->
              <div class="flex items-center justify-between py-1">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Active</label>
                <button
                  type="button"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary/30"
                  :class="form.isActive ? 'bg-primary' : 'bg-surface-container-high'"
                  :disabled="modalLoading"
                  @click="form.isActive = !form.isActive"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform"
                    :class="form.isActive ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>

              <!-- Validation error -->
              <p v-if="modalError" class="text-sm text-error font-medium">{{ modalError }}</p>

              <!-- Actions -->
              <div class="flex gap-3 pt-2">
                <button
                  type="button"
                  class="flex-1 py-3 rounded-xl bg-surface-container text-on-surface font-semibold text-sm hover:bg-surface-container-high transition-colors"
                  :disabled="modalLoading"
                  @click="closeModal"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  class="flex-1 py-3 rounded-xl bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold text-sm shadow-lg shadow-primary/20 active:scale-95 transition-transform flex items-center justify-center gap-2 disabled:opacity-60 disabled:pointer-events-none"
                  :disabled="modalLoading"
                >
                  <span v-if="modalLoading" class="material-symbols-outlined text-base animate-spin">progress_activity</span>
                  <span v-else class="material-symbols-outlined text-base">add_box</span>
                  {{ modalLoading ? 'Adding...' : 'Add Product' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </DashboardLayout>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-active .relative,
.modal-leave-active .relative {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .relative {
  transform: scale(0.95) translateY(8px);
  opacity: 0;
}

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
