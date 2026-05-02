<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const fullName = ref('')
const organization = ref('')
const email = ref('')
const password = ref('')
const showPassword = ref(false)

const handleRegister = () => {
  // Mock registration logic
  console.log('Registering:', fullName.value, email.value)
  router.push('/login')
}
</script>

<template>
  <div class="bg-surface-container-lowest text-on-surface selection:bg-primary selection:text-on-primary min-h-screen flex flex-col">
    <!-- TopAppBar -->
    <header class="fixed top-0 w-full z-50 flex justify-between items-center px-6 py-4 bg-[#0b1326]/60 backdrop-blur-xl shadow-2xl shadow-black/20 font-['Manrope'] tracking-tight">
      <div class="text-2xl font-bold tracking-tighter text-slate-100 uppercase">
        GeoLicense
      </div>
      <div class="hidden md:flex items-center gap-8">
        <nav class="flex gap-6">
          <a href="#" class="text-slate-400 hover:text-slate-200 transition-colors text-sm">Support</a>
          <a href="#" class="text-slate-400 hover:text-slate-200 transition-colors text-sm">Enterprise</a>
        </nav>
        <button class="bg-white/5 px-5 py-2 rounded-lg text-blue-400 border border-white/10 hover:bg-white/10 transition-all active:scale-95 duration-200 text-sm font-semibold">
          Contact Sales
        </button>
      </div>
    </header>

    <!-- Main Canvas -->
    <main class="flex-grow flex items-center justify-center pt-24 pb-12 px-4 relative overflow-hidden">
      <!-- Abstract Background Elements -->
      <div class="absolute top-1/4 -left-24 w-96 h-96 bg-primary/10 blur-[120px] rounded-full pointer-events-none"></div>
      <div class="absolute bottom-1/4 -right-24 w-96 h-96 bg-secondary-container/20 blur-[120px] rounded-full pointer-events-none"></div>

      <div class="w-full max-w-5xl grid md:grid-cols-2 bg-surface rounded-xl overflow-hidden shadow-2xl relative z-10">
        <!-- Left Side: Authority & Context -->
        <div class="hidden md:flex flex-col justify-between p-12 bg-gradient-to-br from-surface-container-high to-surface">
          <div>
            <h1 class="text-4xl font-extrabold text-on-surface leading-tight mb-4 tracking-tighter">
              Sovereign Grade<br/>Infrastructure.
            </h1>
            <p class="text-on-surface-variant text-lg leading-relaxed max-w-xs">
              Deployment at scale requires precision. Establish your authority within the GeoLicense ecosystem.
            </p>
          </div>

          <div class="space-y-6">
            <div class="flex items-start gap-4">
              <span class="material-symbols-outlined text-primary" style="font-variation-settings: 'FILL' 1;">verified_user</span>
              <div>
                <h3 class="text-sm font-bold text-on-surface uppercase tracking-widest">Encrypted Ledger</h3>
                <p class="text-xs text-on-surface-variant">Military-grade protection for every issued seat.</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <span class="material-symbols-outlined text-primary" style="font-variation-settings: 'FILL' 1;">analytics</span>
              <div>
                <h3 class="text-sm font-bold text-on-surface uppercase tracking-widest">Real-time Audits</h3>
                <p class="text-xs text-on-surface-variant">Instant transparency across global deployments.</p>
              </div>
            </div>
          </div>

          <div class="mt-8">
            <div class="bg-surface-container-highest/40 backdrop-blur-md p-4 rounded-lg border-l-2 border-primary">
              <p class="text-sm italic text-on-surface-variant">"The most robust licensing solution we've ever integrated. It's built like a fortress."</p>
              <p class="text-xs font-bold mt-2 text-primary">— CTO, Vertex Dynamics</p>
            </div>
          </div>
        </div>

        <!-- Right Side: Registration Form -->
        <div class="p-8 md:p-12 flex flex-col justify-center">
          <div class="mb-8">
            <h2 class="text-2xl font-bold text-on-surface mb-2">Create Authority Account</h2>
            <p class="text-on-surface-variant text-sm">Fill in your credentials to begin provisioning.</p>
          </div>

          <form @submit.prevent="handleRegister" class="space-y-6">
            <div class="space-y-4">
              <!-- Full Name -->
              <div class="group">
                <label class="block text-xs font-medium text-on-surface-variant uppercase tracking-widest mb-2 transition-colors group-focus-within:text-primary font-bold">Full Name</label>
                <input 
                  v-model="fullName"
                  type="text" 
                  placeholder="e.g. Marcus Aurelius" 
                  required
                  class="w-full bg-surface-container-highest border-none rounded-lg py-3 px-4 text-on-surface placeholder:text-outline focus:ring-1 focus:ring-primary transition-all outline-none"
                />
              </div>

              <!-- Organization -->
              <div class="group">
                <label class="block text-xs font-medium text-on-surface-variant uppercase tracking-widest mb-2 transition-colors group-focus-within:text-primary font-bold">Organization Name</label>
                <input 
                  v-model="organization"
                  type="text" 
                  placeholder="Enter formal entity name" 
                  required
                  class="w-full bg-surface-container-highest border-none rounded-lg py-3 px-4 text-on-surface placeholder:text-outline focus:ring-1 focus:ring-primary transition-all outline-none"
                />
              </div>

              <!-- Email -->
              <div class="group">
                <label class="block text-xs font-medium text-on-surface-variant uppercase tracking-widest mb-2 transition-colors group-focus-within:text-primary font-bold">Work Email</label>
                <input 
                  v-model="email"
                  type="email" 
                  placeholder="name@organization.com" 
                  required
                  class="w-full bg-surface-container-highest border-none rounded-lg py-3 px-4 text-on-surface placeholder:text-outline focus:ring-1 focus:ring-primary transition-all outline-none"
                />
              </div>

              <!-- Password -->
              <div class="group">
                <label class="block text-xs font-medium text-on-surface-variant uppercase tracking-widest mb-2 transition-colors group-focus-within:text-primary font-bold">Password</label>
                <div class="relative">
                  <input 
                    v-model="password"
                    :type="showPassword ? 'text' : 'password'" 
                    placeholder="••••••••" 
                    required
                    class="w-full bg-surface-container-highest border-none rounded-lg py-3 px-4 text-on-surface placeholder:text-outline focus:ring-1 focus:ring-primary transition-all outline-none"
                  />
                  <button 
                    type="button"
                    @click="showPassword = !showPassword"
                    class="absolute right-4 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors"
                  >
                    <span class="material-symbols-outlined text-lg">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Agreement -->
            <div class="flex items-center gap-3 py-2">
              <div class="relative flex items-center">
                <input type="checkbox" id="terms" required class="peer h-5 w-5 appearance-none rounded border border-outline-variant bg-surface-container-highest checked:bg-primary transition-all" />
                <span class="material-symbols-outlined absolute text-on-primary text-[1rem] hidden peer-checked:block pointer-events-none left-[2px]" style="font-variation-settings: 'FILL' 1;">check</span>
              </div>
              <label class="text-xs text-on-surface-variant leading-tight" for="terms">
                I agree to the <a href="#" class="text-primary hover:underline">GeoLicense Terms of Service</a> and <a href="#" class="text-primary hover:underline">Security Protocols</a>.
              </label>
            </div>

            <!-- Submit -->
            <button 
              type="submit"
              class="w-full bg-gradient-to-r from-primary to-primary-container text-on-primary font-bold py-4 rounded-lg shadow-xl shadow-primary/10 hover:shadow-primary/20 active:scale-[0.98] transition-all duration-200"
            >
              Create Account
            </button>
          </form>

          <div class="mt-8 pt-8 border-t border-outline-variant/10 text-center">
            <p class="text-sm text-on-surface-variant">
              Already part of the network? 
              <RouterLink to="/login" class="text-primary font-semibold hover:text-surface-tint ml-1">Login here</RouterLink>
            </p>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="w-full py-8 bg-[#060e20] flex flex-col md:flex-row justify-between items-center px-12 gap-4">
      <div class="text-sm font-black text-slate-500">
        © 2024 GeoLicense. Sovereign Grade Infrastructure.
      </div>
      <div class="flex gap-8">
        <a href="#" class="font-['Manrope'] text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100">Privacy Policy</a>
        <a href="#" class="font-['Manrope'] text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100">Terms of Service</a>
        <a href="#" class="font-['Manrope'] text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100">Security Architecture</a>
      </div>
    </footer>
  </div>
</template>
