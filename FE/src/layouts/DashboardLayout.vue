<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'

const router = useRouter()

const logout = () => {
  // Simple logout logic
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen bg-surface-container-lowest text-on-surface">
    <!-- SideNavBar (Desktop) -->
    <aside class="hidden md:flex flex-col h-screen w-64 bg-[#060e20] fixed left-0 top-0 z-50">
      <div class="flex flex-col h-full py-8 space-y-2">
        <div class="px-6 mb-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded bg-primary-container flex items-center justify-center">
              <span class="material-symbols-outlined text-on-primary">verified_user</span>
            </div>
            <div>
              <h1 class="text-xl font-black text-white tracking-widest uppercase">Sovereign</h1>
              <p class="text-[0.6875rem] font-medium text-on-surface-variant tracking-wider uppercase">License Management</p>
            </div>
          </div>
        </div>

        <nav class="flex-1 space-y-1">
          <RouterLink
            to="/dashboard"
            class="py-3 px-6 flex items-center gap-3 transition-all duration-200"
            :class="[
              $route.path === '/dashboard'
                ? 'bg-gradient-to-r from-blue-500/10 to-transparent text-blue-400 border-l-4 border-blue-500'
                : 'text-[#c2c6d6] hover:bg-[#171f33] hover:text-white'
            ]"
          >
            <span class="material-symbols-outlined">dashboard</span>
            <span class="font-manrope text-[0.875rem] font-medium">Dashboard</span>
          </RouterLink>

          <RouterLink to="/user/license" class="text-[#c2c6d6] py-3 px-6 flex items-center gap-3 hover:bg-[#171f33] hover:text-white transition-colors duration-200">
            <span class="material-symbols-outlined">vpn_key</span>
            <span class="font-manrope text-[0.875rem] font-medium">Licenses</span>
          </RouterLink>

          <RouterLink to="#" class="text-[#c2c6d6] py-3 px-6 flex items-center gap-3 hover:bg-[#171f33] hover:text-white transition-colors duration-200">
            <span class="material-symbols-outlined">group</span>
            <span class="font-manrope text-[0.875rem] font-medium">Users</span>
          </RouterLink>

          <RouterLink to="#" class="text-[#c2c6d6] py-3 px-6 flex items-center gap-3 hover:bg-[#171f33] hover:text-white transition-colors duration-200">
            <span class="material-symbols-outlined">inventory_2</span>
            <span class="font-manrope text-[0.875rem] font-medium">Inventory</span>
          </RouterLink>

          <RouterLink to="#" class="text-[#c2c6d6] py-3 px-6 flex items-center gap-3 hover:bg-[#171f33] hover:text-white transition-colors duration-200">
            <span class="material-symbols-outlined">verified_user</span>
            <span class="font-manrope text-[0.875rem] font-medium">Security</span>
          </RouterLink>
        </nav>

        <div class="px-6 mt-auto">
          <button class="w-full py-3 px-4 bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold rounded-lg transition-transform active:scale-95 shadow-lg shadow-primary/20">
            Generate Report
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content Canvas -->
    <main class="flex-1 md:ml-64 min-h-screen bg-surface-container-lowest pb-20 md:pb-0">
      <!-- TopNavBar -->
      <header class="sticky top-0 z-40 w-full bg-[#0b1326]/60 backdrop-blur-xl border-b border-white/5 flex justify-between items-center px-6 py-3 shadow-2xl shadow-[#060e20]">
        <div class="flex items-center gap-8">
          <span class="text-lg font-extrabold tracking-tighter text-[#adc6ff]">GeoLicense</span>
          <div class="relative hidden lg:block">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm">search</span>
            <input
              type="text"
              placeholder="Quick search ledger..."
              class="bg-surface-container-high border-none rounded-full py-1.5 pl-10 pr-4 text-sm w-64 focus:ring-1 focus:ring-primary text-on-surface placeholder:text-on-surface-variant/50"
            />
          </div>
        </div>

        <div class="flex items-center gap-6">
          <div class="flex items-center gap-4 text-[#c2c6d6]">
            <button class="hover:text-white transition-colors"><span class="material-symbols-outlined">notifications</span></button>
            <button class="hover:text-white transition-colors"><span class="material-symbols-outlined">help</span></button>
            <button class="hover:text-white transition-colors"><span class="material-symbols-outlined">settings</span></button>
          </div>

          <div class="flex items-center gap-3 pl-6 border-l border-white/10">
            <div class="text-right">
              <p class="text-sm font-bold text-white">Admin Unit 01</p>
              <p class="text-[0.6875rem] text-primary uppercase tracking-widest">Super Administrator</p>
            </div>
            <img
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuAxwsVf3ZSwq4oh0BsGy1KmLIRywLnlKLQ-je4ICjYKy59IHecnS2W6QJOgz8ziZBrenVG7l-HgNUp1xxvWy9d4QVGk_PrnVRSvcuSscR_mksvM9Jp4PF0upjiqWcA2ptGLqPOxpkMisPUs2fOkD28UCXlFuPOtdxZhNXrywGEEiojAmI-tfWfY9LXqRLDOgQATeTSZjI04F2G9gtE25H7EPt-ozLpJ_c2xu6TVKr4TIEp5TCop7-VwsZAHktWgOsVHOHQmCdA3RNQ"
              alt="Profile"
              class="w-10 h-10 rounded-full border-2 border-primary/20 object-cover"
            />
            <button @click="logout" class="ml-2 hover:text-error transition-colors">
              <span class="material-symbols-outlined">logout</span>
            </button>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <slot />
    </main>

    <!-- BottomNavBar for Mobile -->
    <nav class="md:hidden fixed bottom-0 left-0 right-0 glass-panel border-t border-white/5 flex justify-around items-center h-16 z-50">
      <RouterLink to="/dashboard" class="flex flex-col items-center gap-1" :class="[$route.path === '/dashboard' ? 'text-primary' : 'text-on-surface-variant']">
        <span class="material-symbols-outlined">dashboard</span>
        <span class="text-[10px] font-bold">Home</span>
      </RouterLink>
      <RouterLink to="#" class="flex flex-col items-center gap-1 text-on-surface-variant">
        <span class="material-symbols-outlined">vpn_key</span>
        <span class="text-[10px] font-bold">Keys</span>
      </RouterLink>
      <RouterLink to="#" class="flex flex-col items-center gap-1 text-on-surface-variant">
        <span class="material-symbols-outlined">group</span>
        <span class="text-[10px] font-bold">Users</span>
      </RouterLink>
      <RouterLink to="#" class="flex flex-col items-center gap-1 text-on-surface-variant">
        <span class="material-symbols-outlined">settings</span>
        <span class="text-[10px] font-bold">Config</span>
      </RouterLink>
    </nav>
  </div>
</template>
