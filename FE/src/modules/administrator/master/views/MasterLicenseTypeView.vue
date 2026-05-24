<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import MasterLicenseTypeService from '@/modules/administrator/master/services/masterlicensetype.service'
import type { LicenseTypePayload } from '../models/licensetype.response'

const PAGE_SIZE = 10

const licenseTypes = ref<LicenseTypePayload[]>([])
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
    fetchLicenseTypes()
  }, 300)
}

async function fetchLicenseTypes() {
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
      ? await MasterLicenseTypeService.getAllByFilter({ filter: filterName.value.trim(), ...params })
      : await MasterLicenseTypeService.getAll(params)
    const page = res.payload
    licenseTypes.value = page.content
    totalElements.value = page.totalElements
    totalPages.value = page.totalPages
  } catch {
    error.value = 'Failed to load license types. Please try again.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchLicenseTypes)

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchLicenseTypes()
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

function trialBadgeClass(isTrial: boolean) {
  return isTrial
    ? 'bg-tertiary-fixed text-on-tertiary-fixed-variant'
    : 'bg-secondary-fixed text-on-secondary-fixed-variant'
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

// --- Add Modal ---
const showModal = ref(false)
const modalLoading = ref(false)
const modalError = ref<string | null>(null)

interface LicenseTypeForm {
  name: string
  description: string
  isTrial: boolean
}

const form = ref<LicenseTypeForm>({
  name: '',
  description: '',
  isTrial: false,
})

function openModal() {
  form.value = { name: '', description: '', isTrial: false }
  modalError.value = null
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function submitLicenseType() {
  modalError.value = null
  if (!form.value.name.trim()) {
    modalError.value = 'Name is required.'
    return
  }
  modalLoading.value = true
  try {
    await MasterLicenseTypeService.addLicenseType({
      name: form.value.name.trim(),
      description: form.value.description.trim(),
      isTrial: form.value.isTrial,
    })
    closeModal()
    showToast('License type successfully added.')
    await fetchLicenseTypes()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    closeModal()
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to add license type. Please try again.',
      'error',
    )
  } finally {
    modalLoading.value = false
  }
}

// --- Delete Modal ---
const showDeleteModal = ref(false)
const deleteLoading = ref(false)
const deleteTarget = ref<LicenseTypePayload | null>(null)

function openDeleteModal(licenseType: LicenseTypePayload) {
  deleteTarget.value = licenseType
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
    await MasterLicenseTypeService.deleteLicenseType(deleteTarget.value.id)
    closeDeleteModal()
    showToast('License type successfully deleted.', 'error')
    await fetchLicenseTypes()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    closeDeleteModal()
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to delete license type. Please try again.',
      'error',
    )
  } finally {
    deleteLoading.value = false
  }
}

// --- Edit Modal ---
const showEditModal = ref(false)
const editModalLoading = ref(false)
const editModalError = ref<string | null>(null)

interface EditLicenseTypeForm {
  id: string
  name: string
  description: string
  isTrial: boolean
}

const editForm = ref<EditLicenseTypeForm>({
  id: '',
  name: '',
  description: '',
  isTrial: false,
})

function openEditModal(licenseType: LicenseTypePayload) {
  editForm.value = {
    id: licenseType.id,
    name: licenseType.name,
    description: licenseType.description,
    isTrial: licenseType.isTrial,
  }
  editModalError.value = null
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
}

async function submitEditLicenseType() {
  editModalError.value = null
  if (!editForm.value.name.trim()) {
    editModalError.value = 'Name is required.'
    return
  }
  editModalLoading.value = true
  try {
    await MasterLicenseTypeService.updateLicenseType({
      id: editForm.value.id,
      name: editForm.value.name.trim(),
      description: editForm.value.description.trim(),
      isTrial: editForm.value.isTrial,
    })
    closeEditModal()
    showToast('License type successfully updated.', 'warning')
    await fetchLicenseTypes()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { messages?: string[] } } }
    closeEditModal()
    showToast(
      err.response?.data?.messages?.[0] ?? 'Failed to update license type. Please try again.',
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
            License Types
          </h2>
          <p class="text-on-surface-variant font-body text-lg">
            Manage License Types for GeoLicense
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
            Add License Type
          </button>
        </div>
      </section>

      <!-- Metrics Cards -->
      <section class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
        <div class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between">
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Total Types
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ totalElements.toLocaleString() }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-primary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-fixed-variant">badge</span>
          </div>
        </div>
        <div class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between">
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Trial
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ licenseTypes.filter((l) => l.isTrial).length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-tertiary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-tertiary-fixed-variant">experiment</span>
          </div>
        </div>
        <div class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between">
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Full
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ licenseTypes.filter((l) => !l.isTrial).length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-secondary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-secondary-fixed-variant">verified</span>
          </div>
        </div>
        <div class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between">
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              This Page
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ licenseTypes.length }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-primary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-fixed-variant">list_alt</span>
          </div>
        </div>
      </section>

      <!-- Data Table -->
      <div class="bg-surface-container-lowest rounded-xl shadow-xl shadow-black/[0.03] overflow-hidden">
        <!-- Table Controls -->
        <div class="p-6 bg-surface-container-low flex flex-wrap items-center justify-between gap-4">
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">search</span>
            <input
              v-model="filterName"
              type="text"
              placeholder="Search by name..."
              class="pl-10 pr-4 py-2.5 bg-surface-container-lowest border-none rounded-lg text-sm text-on-surface placeholder:text-outline focus:ring-2 focus:ring-primary/20 w-64"
              @input="onFilterInput"
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
                <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest">
                  NAME
                </th>
                <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest">
                  DESCRIPTION
                </th>
                <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest">
                  TYPE LICENSE
                </th>
                <th class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest text-right">
                  ACTIONS
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
                      <div class="h-3 w-32 bg-surface-container rounded"></div>
                    </div>
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
              <tr v-else-if="licenseTypes.length === 0">
                <td colspan="4" class="px-6 py-16 text-center text-on-surface-variant">
                  <span class="material-symbols-outlined text-4xl block mb-2">badge</span>
                  No license types found.
                </td>
              </tr>

              <!-- Data rows -->
              <tr
                v-else
                v-for="licenseType in licenseTypes"
                :key="licenseType.id"
                class="hover:bg-surface-container-low transition-colors"
              >
                <td class="px-6 py-4">
                  <div class="flex items-center gap-4">
                    <div class="w-10 h-10 rounded-xl bg-primary-fixed flex items-center justify-center shrink-0">
                      <span class="material-symbols-outlined text-on-primary-fixed-variant text-lg">badge</span>
                    </div>
                    <p class="font-headline font-bold text-on-surface leading-tight">{{ licenseType.name }}</p>
                  </div>
                </td>
                <td class="px-6 py-4 max-w-xs">
                  <p class="text-sm text-on-surface-variant truncate">{{ licenseType.description || '—' }}</p>
                </td>
                <td class="px-6 py-4">
                  <span
                    class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold"
                    :class="trialBadgeClass(licenseType.isTrial)"
                  >
                    {{ licenseType.isTrial ? 'Trial' : 'Full' }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <button
                    class="p-2 text-outline hover:text-primary hover:bg-primary-fixed transition-all rounded-lg"
                    aria-label="Edit license type"
                    @click="openEditModal(licenseType)"
                  >
                    <span class="material-symbols-outlined">edit</span>
                  </button>
                  <button
                    class="p-2 text-outline hover:text-error hover:bg-error-container transition-all rounded-lg"
                    aria-label="Delete license type"
                    @click="openDeleteModal(licenseType)"
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
            license types
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
              {{ toast.type === 'success' ? 'check_circle' : toast.type === 'warning' ? 'edit_note' : 'delete_forever' }}
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
            <div class="flex items-center justify-between px-6 py-5 border-b border-surface-container">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-error-container flex items-center justify-center">
                  <span class="material-symbols-outlined text-on-error-container text-lg">delete_forever</span>
                </div>
                <h3 class="text-lg font-headline font-bold text-on-surface">Delete License Type</h3>
              </div>
              <button
                class="p-1.5 rounded-lg text-outline hover:bg-surface-container hover:text-on-surface transition-colors"
                aria-label="Close modal"
                @click="closeDeleteModal"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
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

    <!-- Edit Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showEditModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeEditModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative w-full max-w-md bg-surface-container-lowest rounded-2xl shadow-2xl overflow-hidden">
            <div class="flex items-center justify-between px-6 py-5 border-b border-surface-container">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-primary-fixed flex items-center justify-center">
                  <span class="material-symbols-outlined text-on-primary-fixed-variant text-lg">edit</span>
                </div>
                <h3 class="text-lg font-headline font-bold text-on-surface">Edit License Type</h3>
              </div>
              <button
                class="p-1.5 rounded-lg text-outline hover:bg-surface-container hover:text-on-surface transition-colors"
                aria-label="Close modal"
                @click="closeEditModal"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
            <form class="px-6 py-6 space-y-5" @submit.prevent="submitEditLicenseType">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Name</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">badge</span>
                  <input
                    v-model="editForm.name"
                    type="text"
                    placeholder="e.g. Enterprise Annual"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm"
                    :disabled="editModalLoading"
                  />
                </div>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Description</label>
                <textarea
                  v-model="editForm.description"
                  rows="3"
                  placeholder="Short description..."
                  class="w-full px-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm resize-none"
                  :disabled="editModalLoading"
                ></textarea>
              </div>

              <div class="flex items-center justify-between py-1">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Trial License</label>
                <button
                  type="button"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary/30"
                  :class="editForm.isTrial ? 'bg-primary' : 'bg-surface-container-high'"
                  :disabled="editModalLoading"
                  @click="editForm.isTrial = !editForm.isTrial"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform"
                    :class="editForm.isTrial ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>

              <p v-if="editModalError" class="text-sm text-error font-medium">{{ editModalError }}</p>

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

    <!-- Add Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div
          v-if="showModal"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click.self="closeModal"
        >
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
          <div class="relative w-full max-w-md bg-surface-container-lowest rounded-2xl shadow-2xl overflow-hidden">
            <div class="flex items-center justify-between px-6 py-5 border-b border-surface-container">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-xl bg-secondary-fixed flex items-center justify-center">
                  <span class="material-symbols-outlined text-on-secondary-fixed-variant text-lg">add_box</span>
                </div>
                <h3 class="text-lg font-headline font-bold text-on-surface">Add License Type</h3>
              </div>
              <button
                class="p-1.5 rounded-lg text-outline hover:bg-surface-container hover:text-on-surface transition-colors"
                aria-label="Close modal"
                @click="closeModal"
              >
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>
            <form class="px-6 py-6 space-y-5" @submit.prevent="submitLicenseType">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Name</label>
                <div class="relative">
                  <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">badge</span>
                  <input
                    v-model="form.name"
                    type="text"
                    placeholder="e.g. Enterprise Annual"
                    class="w-full pl-10 pr-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm"
                    :disabled="modalLoading"
                  />
                </div>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Description</label>
                <textarea
                  v-model="form.description"
                  rows="3"
                  placeholder="Short description..."
                  class="w-full px-4 py-3 bg-surface-container rounded-xl text-on-surface placeholder:text-outline border-none focus:ring-2 focus:ring-primary/30 text-sm resize-none"
                  :disabled="modalLoading"
                ></textarea>
              </div>

              <div class="flex items-center justify-between py-1">
                <label class="text-xs font-bold text-on-surface-variant uppercase tracking-widest">Trial License</label>
                <button
                  type="button"
                  class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-primary/30"
                  :class="form.isTrial ? 'bg-primary' : 'bg-surface-container-high'"
                  :disabled="modalLoading"
                  @click="form.isTrial = !form.isTrial"
                >
                  <span
                    class="inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform"
                    :class="form.isTrial ? 'translate-x-6' : 'translate-x-1'"
                  ></span>
                </button>
              </div>

              <p v-if="modalError" class="text-sm text-error font-medium">{{ modalError }}</p>

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
                  {{ modalLoading ? 'Adding...' : 'Add License Type' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </DashboardLayout>
</template>

<style>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
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
