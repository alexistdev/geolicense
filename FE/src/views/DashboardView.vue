<script setup lang="ts">
import DashboardLayout from '@/layouts/DashboardLayout.vue'

const activeClearances = [
  { id: '#L-992-X', holder: 'AquaDynamics Corp.', initials: 'AQ', status: 'Active', date: 'Aug 12, 2023', value: '$142,000' },
  { id: '#L-042-S', holder: 'Nebula Systems', initials: 'NS', status: 'Active', date: 'Jul 28, 2023', value: '$89,200' },
  { id: '#L-811-C', holder: 'CoreTech Indust.', initials: 'CT', status: 'Pending', date: 'Sep 01, 2023', value: '$210,000' }
]

const securityPulse = [
  { type: 'primary', title: 'Key Generated: UNIT_42', time: '2 minutes ago by System' },
  { type: 'tertiary', title: 'Failed Login Attempt', time: '14 minutes ago from IP: 192.0.0.1' },
  { type: 'primary_fade', title: 'Licence Renewed: Ent_Sovereign', time: '1 hour ago' },
  { type: 'primary_fade', title: 'Migration Complete', time: '3 hours ago' }
]
</script>

<template>
  <DashboardLayout>
    <div class="p-8 space-y-8">
      <!-- Hero Section: Stats Ledger -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-surface-container p-6 rounded-xl flex flex-col justify-between transition-all hover:bg-surface-container-high group cursor-default">
          <div>
            <span class="text-[0.6875rem] font-bold text-on-surface-variant uppercase tracking-[0.1em]">Active Licenses</span>
            <h2 class="text-4xl font-extrabold mt-2 text-white tracking-tight">12,842</h2>
          </div>
          <div class="mt-4 flex items-center gap-2 text-primary">
            <span class="material-symbols-outlined text-sm">trending_up</span>
            <span class="text-xs font-bold">+14.2% from last cycle</span>
          </div>
        </div>

        <div class="bg-surface-container p-6 rounded-xl flex flex-col justify-between transition-all hover:bg-surface-container-high group cursor-default">
          <div>
            <span class="text-[0.6875rem] font-bold text-on-surface-variant uppercase tracking-[0.1em]">Total Revenue</span>
            <h2 class="text-4xl font-extrabold mt-2 text-white tracking-tight">$4.2M</h2>
          </div>
          <div class="mt-4 flex items-center gap-2 text-primary">
            <span class="material-symbols-outlined text-sm">payments</span>
            <span class="text-xs font-bold">Invoiced Monthly</span>
          </div>
        </div>

        <div class="bg-surface-container p-6 rounded-xl flex flex-col justify-between transition-all hover:bg-surface-container-high group cursor-default">
          <div>
            <span class="text-[0.6875rem] font-bold text-on-surface-variant uppercase tracking-[0.1em]">Pending Clearances</span>
            <h2 class="text-4xl font-extrabold mt-2 text-tertiary tracking-tight">84</h2>
          </div>
          <div class="mt-4 flex items-center gap-2 text-tertiary">
            <span class="material-symbols-outlined text-sm">priority_high</span>
            <span class="text-xs font-bold">12 Critical items</span>
          </div>
        </div>

        <div class="bg-surface-container p-6 rounded-xl flex flex-col justify-between transition-all hover:bg-surface-container-high group cursor-default">
          <div>
            <span class="text-[0.6875rem] font-bold text-on-surface-variant uppercase tracking-[0.1em]">Security ScoreL</span>
            <h2 class="text-4xl font-extrabold mt-2 text-primary tracking-tight">98%</h2>
          </div>
          <div class="mt-4 flex items-center gap-2 text-primary">
            <span class="material-symbols-outlined text-sm">verified</span>
            <span class="text-xs font-bold">Optimal Integrity</span>
          </div>
        </div>
      </div>

      <!-- Main Bento Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Performance Visualization -->
        <div class="lg:col-span-2 bg-surface p-8 rounded-2xl relative overflow-hidden">
          <div class="flex justify-between items-start mb-10">
            <div>
              <h3 class="text-2xl font-bold text-white mb-1">License Distribution</h3>
              <p class="text-sm text-on-surface-variant">Real-time throughput and allocation volume across clusters.</p>
            </div>
            <div class="flex gap-2">
              <span class="px-3 py-1 bg-surface-container-highest text-primary text-[0.6875rem] font-bold rounded-full">30 DAYS</span>
              <span class="px-3 py-1 text-on-surface-variant text-[0.6875rem] font-bold rounded-full hover:bg-surface-container-high cursor-pointer transition-colors">ALL TIME</span>
            </div>
          </div>

          <!-- Mock Chart Area -->
          <div class="h-64 flex items-end gap-3 px-2">
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[40%] group relative transition-all duration-500 hover:h-[45%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[65%] group relative transition-all duration-500 hover:h-[70%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[55%] group relative transition-all duration-500 hover:h-[60%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            <div class="flex-1 bg-primary rounded-t-sm h-[85%] group relative transition-all duration-500 hover:h-[90%]">
              <div class="absolute inset-0 bg-white/20 opacity-0 group-hover:opacity-100 transition-opacity shadow-[0_0_40px_-5px_rgba(173,198,255,0.4)]"></div>
            </div>
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[45%] group relative transition-all duration-500 hover:h-[50%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[70%] group relative transition-all duration-500 hover:h-[75%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[60%] group relative transition-all duration-500 hover:h-[65%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
            <div class="flex-1 bg-surface-container-high rounded-t-sm h-[90%] group relative transition-all duration-500 hover:h-[95%]">
              <div class="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </div>
          </div>

          <div class="mt-6 flex justify-between text-[0.625rem] text-on-surface-variant font-bold tracking-widest uppercase opacity-50">
            <span>AUG 01</span>
            <span>AUG 08</span>
            <span>AUG 15</span>
            <span>AUG 22</span>
            <span>AUG 29</span>
          </div>
        </div>

        <!-- Side Activity Stream -->
        <div class="bg-surface-container-low p-8 rounded-2xl">
          <h3 class="text-lg font-bold text-white mb-6">Security Pulse</h3>
          <div class="space-y-6">
            <div v-for="(item, index) in securityPulse" :key="index" class="flex gap-4">
              <div
                class="w-2 h-2 mt-1.5 rounded-full flex-shrink-0"
                :class="[
                  item.type === 'primary' ? 'bg-primary shadow-[0_0_10px_rgba(173,198,255,0.8)]' :
                  item.type === 'tertiary' ? 'bg-tertiary' : 'bg-primary opacity-40'
                ]"
              ></div>
              <div>
                <p class="text-sm font-bold text-on-surface">{{ item.title }}</p>
                <p class="text-xs text-on-surface-variant mt-1">{{ item.time }}</p>
              </div>
            </div>
          </div>
          <button class="mt-8 w-full py-2.5 text-sm font-bold text-primary border border-primary/20 rounded-lg hover:bg-primary/5 transition-colors">
            View Security Audit
          </button>
        </div>
      </div>

      <!-- Ledger Records: No-Line Lists -->
      <div class="bg-surface p-8 rounded-2xl">
        <div class="flex justify-between items-center mb-8">
          <h3 class="text-xl font-bold text-white">Active Clearances Ledger</h3>
          <span class="material-symbols-outlined text-on-surface-variant cursor-pointer hover:text-white transition-colors">more_horiz</span>
        </div>

        <div class="overflow-x-auto no-scrollbar">
          <table class="w-full text-left">
            <thead>
              <tr class="text-[0.6875rem] text-on-surface-variant font-black uppercase tracking-[0.15em] border-b border-white/5">
                <th class="pb-4 px-2">License ID</th>
                <th class="pb-4">Holder</th>
                <th class="pb-4">Status</th>
                <th class="pb-4">Issued</th>
                <th class="pb-4 text-right">Value</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-transparent">
              <tr v-for="item in activeClearances" :key="item.id" class="group hover:bg-surface-container-low transition-colors cursor-pointer">
                <td class="py-5 px-2">
                  <span class="text-sm font-bold text-primary tracking-mono">{{ item.id }}</span>
                </td>
                <td class="py-5">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded bg-surface-container-high flex items-center justify-center text-[10px] font-bold text-white">
                      {{ item.initials }}
                    </div>
                    <span class="text-sm font-bold">{{ item.holder }}</span>
                  </div>
                </td>
                <td class="py-5">
                  <span
                    class="px-2.5 py-1 rounded-full text-[0.625rem] font-black uppercase tracking-widest"
                    :class="[
                      item.status === 'Active' ? 'bg-primary-container/20 text-primary' : 'bg-tertiary-container/20 text-tertiary'
                    ]"
                  >
                    {{ item.status }}
                  </span>
                </td>
                <td class="py-5 text-sm text-on-surface-variant">{{ item.date }}</td>
                <td class="py-5 text-right font-bold text-white">{{ item.value }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </DashboardLayout>
</template>
