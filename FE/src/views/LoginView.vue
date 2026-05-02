<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const password = ref('')
const showPassword = ref(false)

const handleLogin = () => {
  // Mock login logic
  console.log('Logging in with:', email.value, password.value)
  router.push('/dashboard')
}
</script>

<template>
  <div class="bg-surface-container-lowest min-h-screen flex flex-col items-center justify-center text-on-surface antialiased overflow-hidden selection:bg-primary/30 relative">
    <!-- Ambient Background Elements -->
    <div class="fixed inset-0 tech-pattern z-0"></div>
    <div class="fixed top-[-10%] left-[-10%] w-[50%] h-[50%] bg-primary/5 blur-[120px] rounded-full z-0"></div>
    <div class="fixed bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-secondary-container/10 blur-[120px] rounded-full z-0"></div>

    <main class="relative z-10 w-full max-w-md px-6 py-12 flex flex-col items-center">
      <!-- Brand Header -->
      <div class="mb-12 text-center">
        <div class="flex items-center justify-center gap-3 mb-4">
          <div class="w-12 h-12 bg-gradient-to-br from-primary to-primary-container rounded-xl flex items-center justify-center shadow-lg shadow-primary/20">
            <span class="material-symbols-outlined text-on-primary text-3xl" style="font-variation-settings: 'FILL' 1;">security</span>
          </div>
        </div>
        <h1 class="text-3xl font-black tracking-tighter text-slate-100 mb-1 uppercase">GeoLicense</h1>
        <p class="text-on-surface-variant text-sm tracking-widest font-medium uppercase opacity-70">Sovereign Management Portal</p>
      </div>

      <!-- Auth Card -->
      <div class="w-full bg-surface-container/60 backdrop-blur-2xl p-8 rounded-xl shadow-2xl shadow-black/40 border-t border-white/5 relative group transition-all duration-500">
        <div class="absolute inset-0 bg-gradient-to-b from-white/5 to-transparent rounded-xl pointer-events-none"></div>
        <div class="relative z-10">
          <div class="mb-8">
            <h2 class="text-2xl font-bold text-on-surface tracking-tight">Access Control</h2>
            <p class="text-on-surface-variant text-sm mt-1">Authorized personnel only. Encrypted session.</p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-6">
            <!-- Email Field -->
            <div class="space-y-2">
              <label class="block text-xs font-bold uppercase tracking-widest text-on-surface-variant" for="email">Identity Identifier</label>
              <div class="relative">
                <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant text-xl">alternate_email</span>
                <input 
                  v-model="email"
                  type="email" 
                  id="email" 
                  placeholder="name@organization.gov" 
                  required
                  class="w-full bg-surface-container-highest/50 border-none rounded-lg py-3.5 pl-12 pr-4 text-on-surface placeholder:text-on-surface-variant/40 focus:ring-2 focus:ring-primary/50 transition-all duration-200 outline-none"
                />
              </div>
            </div>

            <!-- Password Field -->
            <div class="space-y-2">
              <label class="block text-xs font-bold uppercase tracking-widest text-on-surface-variant" for="password">Security Protocol</label>
              <div class="relative">
                <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant text-xl">lock</span>
                <input 
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'" 
                  id="password" 
                  placeholder="••••••••••••" 
                  required
                  class="w-full bg-surface-container-highest/50 border-none rounded-lg py-3.5 pl-12 pr-12 text-on-surface placeholder:text-on-surface-variant/40 focus:ring-2 focus:ring-primary/50 transition-all duration-200 outline-none"
                />
                <button 
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-4 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-primary transition-colors"
                >
                  <span class="material-symbols-outlined text-xl">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
                </button>
              </div>
            </div>

            <!-- Options Row -->
            <div class="flex items-center justify-between">
              <label class="flex items-center gap-3 cursor-pointer group">
                <div class="relative flex items-center justify-center">
                  <input type="checkbox" class="peer appearance-none w-5 h-5 rounded border border-outline-variant/30 bg-surface-container-highest checked:bg-primary transition-all duration-200 cursor-pointer" />
                  <span class="material-symbols-outlined absolute text-[16px] text-on-primary scale-0 peer-checked:scale-100 transition-transform pointer-events-none" style="font-variation-settings: 'wght' 700;">check</span>
                </div>
                <span class="text-sm text-on-surface-variant group-hover:text-on-surface transition-colors">Maintain Persistence</span>
              </label>
              <a href="#" class="text-sm font-semibold text-primary hover:text-primary-container transition-all">Recovery Protocol?</a>
            </div>

            <!-- Sign In Button -->
            <button 
              type="submit"
              class="w-full py-4 bg-gradient-to-r from-primary to-primary-container text-on-primary font-bold rounded-lg shadow-lg shadow-primary/20 hover:shadow-primary/30 hover:scale-[1.01] active:scale-[0.98] transition-all duration-200 uppercase tracking-widest text-xs flex items-center justify-center gap-2"
            >
              Establish Connection
              <span class="material-symbols-outlined text-lg">login</span>
            </button>
          </form>

          <div class="mt-8 pt-8 border-t border-white/5 flex flex-col items-center gap-4">
            <p class="text-xs text-on-surface-variant">Require enterprise integration?</p>
            <button class="px-6 py-2 bg-surface-container-high/50 hover:bg-surface-container-high text-on-surface text-xs font-bold rounded-full transition-all flex items-center gap-2 border border-outline-variant/20">
              <span class="material-symbols-outlined text-sm">hub</span>
              SSO Authentication
            </button>
          </div>
        </div>
      </div>

      <!-- System Message -->
      <div class="mt-8 flex items-center gap-2 px-4 py-2 bg-surface-container-low/80 rounded-full border border-white/5">
        <span class="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
        <span class="text-[10px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">Global Core Systems: Operational</span>
      </div>
    </main>

    <!-- Footer -->
    <footer class="fixed bottom-0 w-full py-8 bg-[#060e20] flex flex-col md:flex-row justify-between items-center px-12 gap-4 z-20">
      <div class="text-sm font-black text-slate-500">GeoLicense</div>
      <div class="flex gap-8">
        <a href="#" class="text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100">Privacy Policy</a>
        <a href="#" class="text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100">Terms of Service</a>
        <a href="#" class="text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100">Security Architecture</a>
      </div>
      <div class="text-slate-100 text-xs uppercase tracking-widest">
        © 2024 GeoLicense. Sovereign Grade Infrastructure.
      </div>
    </footer>

    <!-- Imagery Decoration -->
    <div class="hidden lg:block fixed right-0 top-0 bottom-0 w-1/3 z-0 pointer-events-none overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-r from-surface-container-lowest via-transparent to-transparent z-10"></div>
      <img 
        src="https://lh3.googleusercontent.com/aida-public/AB6AXuCREepqt05Wd6lSmmDITYTrobI8SXhb_fOgi40eqmnxKuN5PN-r34iHd7t7oSIs5yEg0W6AdcTVLjGw00z-tUNubI5ZuyR2xQjlhsCBApARS7Yebu-NCv3h16Z5NXSChM4FEaAFFHSqWpFbYJMFRpjPEMVOQVqCGPGpPVRkSvI8cO5-G2SzCgqCt6IQP5gUKNbXhNNzEGQmsQUBT-vgOsQg2JcD9LT8h36Y40bKvYs-jjHPMbMZ3-PMMSG-W8bvNlTQ7t9YKHE3B6M" 
        alt="Tech Infrastructure Background" 
        class="h-full object-cover opacity-20 grayscale"
      />
    </div>
  </div>
</template>
