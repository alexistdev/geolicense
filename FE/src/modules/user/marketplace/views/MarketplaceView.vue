<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import marketplaceService from '@/modules/user/marketplace/services/marketplace.service.ts'
import type { LicenseCatalogItem } from '@/modules/user/marketplace/models/marketplace.response.ts'

const items = ref<LicenseCatalogItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    const response = await marketplaceService.getAll({
      page: 0,
      size: 20,
      sortBy: 'name',
      direction: 'asc',
    })
    items.value = response.payload.content
  } catch {
    error.value = 'Failed to load license catalog. Please try again.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <DashboardLayout>
    <div class="max-w-6xl mx-auto space-y-12">
      <!-- Header Section -->
      <div class="space-y-4">
        <h2 class="font-display text-4xl lg:text-5xl font-extrabold text-on-surface tracking-tight">
          License Catalog
        </h2>
        <p class="font-body text-body-lg text-on-surface-variant max-w-2xl">
          Choose the license plan that fits your needs. Select a plan below to get started.
        </p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="n in 3"
          :key="n"
          class="bg-surface rounded-xl p-6 h-64 animate-pulse flex flex-col gap-4"
        >
          <div class="w-16 h-16 rounded-xl bg-surface-container-high"></div>
          <div class="h-5 bg-surface-container-high rounded w-2/3"></div>
          <div class="h-4 bg-surface-container-high rounded w-full"></div>
          <div class="h-4 bg-surface-container-high rounded w-4/5"></div>
        </div>
      </div>

      <!-- Error -->
      <div
        v-else-if="error"
        class="rounded-xl border border-error/30 bg-error/5 p-6 text-error font-body text-body-md"
      >
        {{ error }}
      </div>

      <!-- Empty -->
      <div
        v-else-if="items.length === 0"
        class="text-center py-20 text-on-surface-variant font-body text-body-lg"
      >
        No license plans available at the moment.
      </div>

      <!-- Catalog Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="item in items"
          :key="item.id"
          class="bg-surface rounded-xl p-6 flex flex-col h-full group relative overflow-hidden transition-all duration-300 hover:bg-surface-container"
        >
          <div
            class="absolute inset-0 bg-gradient-to-br from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"
          ></div>

          <!-- Icon -->
          <div
            class="w-16 h-16 rounded-xl bg-surface-container-highest flex items-center justify-center mb-6 text-primary border border-outline-variant/15 shadow-[0_8px_32px_-8px_rgba(6,14,32,0.8)]"
          >
            <span
              class="material-symbols-outlined text-3xl"
              style="font-variation-settings: 'FILL' 1"
            >{{ item.isTrial ? 'science' : 'workspace_premium' }}</span>
          </div>

          <!-- Name -->
          <h3 class="font-display text-2xl font-bold text-on-surface mb-3 tracking-tight">
            {{ item.name }}
          </h3>

          <!-- Badges -->
          <div class="flex items-center gap-2 mb-4 flex-wrap">
            <span
              v-if="item.isTrial"
              class="font-label text-label-sm uppercase tracking-widest text-tertiary bg-tertiary/10 px-2 py-1 rounded"
            >Trial</span>
            <span
              class="font-label text-label-sm uppercase tracking-widest text-primary bg-primary/10 px-2 py-1 rounded"
            >{{ item.durationDays }} days</span>
            <span
              class="font-label text-label-sm uppercase tracking-widest text-secondary bg-secondary/10 px-2 py-1 rounded"
            >{{ item.maxSeats }} seats</span>
          </div>

          <!-- Description -->
          <p class="font-body text-body-md text-on-surface-variant mb-8 flex-1">
            {{ item.description }}
          </p>

          <button
            class="w-full py-3 px-4 rounded-lg border border-outline-variant/20 text-primary font-bold hover:bg-surface-container-high transition-colors mt-auto flex items-center justify-center gap-2"
          >
            Select Plan
            <span class="material-symbols-outlined text-sm">arrow_forward</span>
          </button>
        </div>
      </div>
    </div>
  </DashboardLayout>
</template>