<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import MasterUserService from '@/modules/administrator/master/services/masteruser.service'
import type { UserResponse } from '../models/user.response'

const PAGE_SIZE = 10

const users = ref<UserResponse[]>([])
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const error = ref<string | null>(null)

async function fetchUsers() {
  loading.value = true
  error.value = null
  try {
    const res = await MasterUserService.getAll({
      page: currentPage.value,
      size: PAGE_SIZE,
      sortBy: 'createdDate',
      direction: 'desc',
    })
    const page = res.payload
    users.value = page.content
    totalElements.value = page.totalElements
    totalPages.value = page.totalPages
  } catch {
    error.value = 'Failed to load users. Please try again.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchUsers)

function goToPage(page: number) {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchUsers()
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

function statusBadgeClass(isSuspend: boolean) {
  return isSuspend
    ? 'bg-error-container text-on-error-container'
    : 'bg-primary-fixed text-on-primary-fixed-variant'
}

function statusDotClass(isSuspend: boolean) {
  return isSuspend ? 'bg-error' : 'bg-primary animate-pulse'
}

function getInitials(fullName: string) {
  return fullName
    ?.split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) ?? '?'
}
</script>

<template>
  <DashboardLayout>
    <main class="p-12 min-h-screen">
      <!-- Header -->
      <section class="grid grid-cols-12 gap-6 mb-12">
        <div class="col-span-12 lg:col-span-8">
          <h2 class="text-4xl font-headline font-extrabold text-on-surface tracking-tight mb-2">
            User Directory
          </h2>
          <p class="text-on-surface-variant font-body text-lg">
            Manage Users for GeoLicense
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
          >
            <span class="material-symbols-outlined text-xl">person_add</span>
            Add New User
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
              Total Users
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">
              {{ totalElements.toLocaleString() }}
            </h3>
          </div>
          <div class="w-12 h-12 bg-primary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-primary-fixed-variant">group</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Active Now
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">—</h3>
          </div>
          <div class="w-12 h-12 bg-tertiary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-tertiary-fixed-variant">bolt</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Pending Invitations
            </p>
            <h3 class="text-3xl font-headline font-bold text-on-surface">—</h3>
          </div>
          <div class="w-12 h-12 bg-secondary-fixed rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-secondary-fixed-variant">mail</span>
          </div>
        </div>
        <div
          class="bg-surface-container-lowest p-6 rounded-xl shadow-sm flex items-center justify-between"
        >
          <div>
            <p class="text-on-surface-variant text-sm font-medium uppercase tracking-wider mb-1">
              Suspended
            </p>
            <h3 class="text-3xl font-headline font-bold text-error">—</h3>
          </div>
          <div class="w-12 h-12 bg-error-container rounded-xl flex items-center justify-center">
            <span class="material-symbols-outlined text-on-error-container">warning</span>
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
                >filter_list</span
              >
              <select
                class="pl-10 pr-8 py-2.5 bg-surface-container-lowest border-none rounded-lg text-sm font-medium text-on-surface-variant appearance-none cursor-pointer focus:ring-2 focus:ring-primary/20"
              >
                <option>Filter by Role</option>
                <option>Super Admin</option>
                <option>Editor</option>
                <option>Viewer</option>
              </select>
            </div>
            <div class="relative">
              <span
                class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline"
                >calendar_today</span
              >
              <select
                class="pl-10 pr-8 py-2.5 bg-surface-container-lowest border-none rounded-lg text-sm font-medium text-on-surface-variant appearance-none cursor-pointer focus:ring-2 focus:ring-primary/20"
              >
                <option>Last Login</option>
                <option>Today</option>
                <option>Past Week</option>
                <option>Past Month</option>
              </select>
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
            <button
              class="px-4 py-2 bg-surface-container-high text-on-surface font-bold text-sm rounded-lg hover:bg-surface-container-highest transition active:scale-95"
            >
              Transfer Ownership
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
                    aria-label="Select all users"
                  />
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  FULL NAME
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  EMAIL
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Role
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Status
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Last Login
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest"
                >
                  Created Date
                </th>
                <th
                  class="px-6 py-4 text-xs font-bold text-on-surface-variant uppercase tracking-widest text-right"
                >
                  Actions
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
                    <div class="h-6 w-20 bg-surface-container rounded-full"></div>
                  </td>
                  <td class="px-6 py-4">
                    <div class="h-3 w-20 bg-surface-container rounded"></div>
                  </td>
                  <td class="px-6 py-4 text-right">
                    <div class="h-8 w-20 bg-surface-container rounded-lg ml-auto"></div>
                  </td>
                </tr>
              </template>

              <!-- Empty state -->
              <tr v-else-if="users.length === 0">
                <td colspan="6" class="px-6 py-16 text-center text-on-surface-variant">
                  <span class="material-symbols-outlined text-4xl block mb-2">group_off</span>
                  No users found.
                </td>
              </tr>

              <!-- Data rows -->
              <tr
                v-else
                v-for="user in users"
                :key="user.id"
                class="hover:bg-surface-container-low transition-colors"
              >
                <td class="px-6 py-4">
                  <input
                    class="rounded border-outline-variant text-primary focus:ring-primary h-4 w-4"
                    type="checkbox"
                    :aria-label="`Select ${user.fullName}`"
                  />
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center gap-4">
                    <div
                      class="w-10 h-10 rounded-xl bg-primary-fixed flex items-center justify-center text-on-primary-fixed-variant font-bold text-sm shrink-0"
                    >
                      {{ getInitials(user.fullName) }}
                    </div>
                    <p class="font-headline font-bold text-on-surface leading-tight">{{ user.fullName }}</p>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span class="text-sm font-medium text-on-surface">{{ user.email }}</span>
                </td>
                <td class="px-6 py-4">
                  <span class="text-sm font-medium text-on-surface">{{ user.role }}</span>
                </td>
                <td class="px-6 py-4">
                  <span
                    class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold"
                    :class="statusBadgeClass(user.isSuspend)"
                  >
                    <span
                      class="w-1.5 h-1.5 rounded-full"
                      :class="statusDotClass(user.isSuspend)"
                    ></span>
                    {{ user.isSuspend ? 'Suspended' : 'Active' }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <p class="text-sm text-on-surface-variant">{{ user.lastLogin ?? 'Never' }}</p>
                </td>
                <td class="px-6 py-4">
                  <p class="text-sm text-on-surface-variant">{{ user.createdDate ?? '—' }}</p>
                </td>
                <td class="px-6 py-4 text-right">
                  <button
                    class="p-2 text-outline hover:text-primary hover:bg-primary-fixed transition-all rounded-lg"
                    aria-label="Edit user"
                  >
                    <span class="material-symbols-outlined">edit</span>
                  </button>
                  <button
                    class="p-2 text-outline hover:text-error hover:bg-error-container transition-all rounded-lg"
                    aria-label="Delete user"
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
            users
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
  </DashboardLayout>
</template>
