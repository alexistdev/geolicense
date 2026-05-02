<script setup lang="ts">
import DashboardLayout from '@/layouts/DashboardLayout.vue';
</script>

<template>
  <DashboardLayout>
    <div class="p-8 space-y-8">
      <!-- Page Header/Editorial -->
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <span class="text-[0.6875rem] font-bold uppercase tracking-[0.2em] text-primary">Operational Repository</span>
          <h2 class="text-4xl font-black tracking-tight text-white mt-1">Enterprise Licenses</h2>
          <p class="text-on-surface-variant max-w-xl mt-2 text-sm leading-relaxed">
            Secure ledger of client entities, active product keys, and regulatory compliance status. Real-time monitoring of organizational deployment metrics.
          </p>
        </div>
        <div class="flex gap-3">
          <button class="px-6 py-2 bg-surface-container hover:bg-surface-container-high text-on-surface text-sm font-semibold rounded transition-all flex items-center gap-2">
            <span class="material-symbols-outlined text-sm">filter_list</span>
            Filter View
          </button>
          <button class="px-6 py-2 bg-primary text-on-primary text-sm font-bold rounded shadow-xl shadow-primary/20 flex items-center gap-2 transition-transform active:scale-95">
            <span class="material-symbols-outlined text-sm">add</span>
            New License
          </button>
        </div>
      </div>

      <!-- Bento Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-surface p-6 rounded-xl border border-white/5 shadow-2xl">
          <p class="text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant/60">Total Active</p>
          <div class="flex items-baseline gap-2 mt-1">
            <span class="text-3xl font-black text-white">2,482</span>
            <span class="text-primary text-xs font-bold">+4.2%</span>
          </div>
        </div>
        <div class="bg-surface p-6 rounded-xl border border-white/5 shadow-2xl">
          <p class="text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant/60">Compliance Risk</p>
          <div class="flex items-baseline gap-2 mt-1">
            <span class="text-3xl font-black text-error">12</span>
            <span class="text-error/60 text-xs font-bold">Critical</span>
          </div>
        </div>
        <div class="bg-surface-container-low p-6 rounded-xl border border-white/5 md:col-span-2 overflow-hidden relative group">
          <div class="relative z-10">
            <p class="text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant/60">Renewal Pipeline</p>
            <p class="text-2xl font-bold text-white mt-1">$1.2M <span class="text-sm font-medium text-on-surface-variant">Due Q3</span></p>
          </div>
          <div class="absolute right-0 bottom-0 top-0 w-1/2 opacity-20 group-hover:opacity-40 transition-opacity">
            <div class="w-full h-full bg-gradient-to-l from-primary/30 to-transparent"></div>
          </div>
        </div>
      </div>

      <!-- High-Contrast Data Repository Table -->
      <div class="bg-surface rounded-2xl shadow-2xl overflow-hidden border border-white/5">
        <div class="p-6 flex items-center justify-between bg-surface-container-low/50 border-b border-white/5">
          <h3 class="font-bold text-on-surface flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">database</span>
            Entity Ledger
          </h3>
          <div class="flex gap-4">
            <span class="text-xs font-medium text-on-surface-variant">Showing 1-10 of 142</span>
          </div>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-surface-container-lowest/30">
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Client Entity</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Primary Product</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">License Key</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant">Status</th>
                <th class="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-widest text-on-surface-variant text-right">Expiration</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/[0.03]">
              <!-- Row 1 -->
              <tr class="hover:bg-surface-container-low transition-colors group">
                <td class="px-6 py-5">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-surface-variant flex items-center justify-center text-primary font-bold">A</div>
                    <div>
                      <p class="text-sm font-bold text-white">Aether Systems Corp</p>
                      <p class="text-[0.65rem] text-on-surface-variant uppercase tracking-tighter">Enterprise Tier</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-5">
                  <span class="text-sm text-on-surface">CloudCore Orchestrator</span>
                </td>
                <td class="px-6 py-5 font-mono text-[0.7rem] text-on-surface-variant">
                  <span class="px-2 py-1 bg-surface-container rounded border border-white/5">AE-992-KLA-12</span>
                </td>
                <td class="px-6 py-5">
                  <span class="px-3 py-1 rounded-full bg-primary/10 text-primary text-[0.65rem] font-black uppercase tracking-wider">Compliant</span>
                </td>
                <td class="px-6 py-5 text-right">
                  <span class="text-sm text-on-surface font-medium">Oct 12, 2025</span>
                </td>
              </tr>
              <!-- Row 2 -->
              <tr class="hover:bg-surface-container-low transition-colors group">
                <td class="px-6 py-5">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-surface-variant flex items-center justify-center text-primary font-bold">N</div>
                    <div>
                      <p class="text-sm font-bold text-white">Nebula Dynamics</p>
                      <p class="text-[0.65rem] text-on-surface-variant uppercase tracking-tighter">Defense Sector</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-5">
                  <span class="text-sm text-on-surface">Sentience-X Neural</span>
                </td>
                <td class="px-6 py-5 font-mono text-[0.7rem] text-on-surface-variant">
                  <span class="px-2 py-1 bg-surface-container rounded border border-white/5">NB-004-XRT-88</span>
                </td>
                <td class="px-6 py-5">
                  <span class="px-3 py-1 rounded-full bg-error/10 text-error text-[0.65rem] font-black uppercase tracking-wider">Over-Limit</span>
                </td>
                <td class="px-6 py-5 text-right">
                  <span class="text-sm text-error font-bold italic">EXPIRED</span>
                </td>
              </tr>
              <!-- Row 3 -->
              <tr class="hover:bg-surface-container-low transition-colors group">
                <td class="px-6 py-5">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-surface-variant flex items-center justify-center text-primary font-bold">O</div>
                    <div>
                      <p class="text-sm font-bold text-white">OmniLogistics</p>
                      <p class="text-[0.65rem] text-on-surface-variant uppercase tracking-tighter">Global Logistics</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-5">
                  <span class="text-sm text-on-surface">FleetWatch Pro</span>
                </td>
                <td class="px-6 py-5 font-mono text-[0.7rem] text-on-surface-variant">
                  <span class="px-2 py-1 bg-surface-container rounded border border-white/5">OL-451-LGT-01</span>
                </td>
                <td class="px-6 py-5">
                  <span class="px-3 py-1 rounded-full bg-secondary-container/30 text-secondary text-[0.65rem] font-black uppercase tracking-wider">Pending</span>
                </td>
                <td class="px-6 py-5 text-right">
                  <span class="text-sm text-on-surface font-medium">Dec 01, 2024</span>
                </td>
              </tr>
              <!-- Row 4 -->
              <tr class="hover:bg-surface-container-low transition-colors group">
                <td class="px-6 py-5">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-surface-variant flex items-center justify-center text-primary font-bold">V</div>
                    <div>
                      <p class="text-sm font-bold text-white">Vortex Media</p>
                      <p class="text-[0.65rem] text-on-surface-variant uppercase tracking-tighter">Media Group</p>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-5">
                  <span class="text-sm text-on-surface">VisionStream 4K</span>
                </td>
                <td class="px-6 py-5 font-mono text-[0.7rem] text-on-surface-variant">
                  <span class="px-2 py-1 bg-surface-container rounded border border-white/5">VM-772-STR-99</span>
                </td>
                <td class="px-6 py-5">
                  <span class="px-3 py-1 rounded-full bg-primary/10 text-primary text-[0.65rem] font-black uppercase tracking-wider">Compliant</span>
                </td>
                <td class="px-6 py-5 text-right">
                  <span class="text-sm text-on-surface font-medium">Jan 18, 2026</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="px-6 py-4 flex items-center justify-between border-t border-white/5 bg-surface-container-low/20">
          <button class="text-xs font-bold text-on-surface-variant hover:text-white flex items-center gap-1 transition-colors">
            <span class="material-symbols-outlined text-[1rem]">chevron_left</span>
            Previous
          </button>
          <div class="flex gap-2">
            <span class="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary text-xs font-bold">1</span>
            <span class="w-8 h-8 flex items-center justify-center rounded hover:bg-surface-container text-on-surface-variant text-xs font-bold cursor-pointer transition-colors">2</span>
            <span class="w-8 h-8 flex items-center justify-center rounded hover:bg-surface-container text-on-surface-variant text-xs font-bold cursor-pointer transition-colors">3</span>
          </div>
          <button class="text-xs font-bold text-on-surface-variant hover:text-white flex items-center gap-1 transition-colors">
            Next
            <span class="material-symbols-outlined text-[1rem]">chevron_right</span>
          </button>
        </div>
      </div>

      <!-- Contextual Insight Footer -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8 py-8 border-t border-white/5">
        <div class="space-y-4">
          <h4 class="text-sm font-black uppercase tracking-widest text-on-surface-variant">System Intelligence</h4>
          <div class="p-5 bg-surface-container rounded-xl flex gap-4">
            <div class="mt-1">
              <span class="material-symbols-outlined text-primary" style="font-variation-settings: 'FILL' 1;">auto_awesome</span>
            </div>
            <div>
              <p class="text-sm font-bold text-white">Predictive Under-utilization</p>
              <p class="text-xs text-on-surface-variant mt-1 leading-relaxed">License for Nebula Dynamics is currently under-utilized by 40%. Recommend downgrading to Standard Tier at next renewal cycle.</p>
            </div>
          </div>
        </div>
        <div class="space-y-4">
          <h4 class="text-sm font-black uppercase tracking-widest text-on-surface-variant">Security Alerts</h4>
          <div class="p-5 bg-surface-container rounded-xl flex gap-4 border-l-4 border-tertiary">
            <div class="mt-1">
              <span class="material-symbols-outlined text-tertiary" style="font-variation-settings: 'FILL' 1;">warning</span>
            </div>
            <div>
              <p class="text-sm font-bold text-white">Unrecognized Key Access</p>
              <p class="text-xs text-on-surface-variant mt-1 leading-relaxed">Multiple failed authentication attempts detected for license AE-992-KLA-12 from unfamiliar IP addresses.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </DashboardLayout>
</template>

