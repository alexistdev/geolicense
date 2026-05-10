<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter, type LocationQueryValue } from 'vue-router'
import AuthService from '@/modules/auth/services/auth.service'
import { AuthException } from '@/modules/auth/exception/auth.exception'
import type { LoginResponse } from '@/modules/auth/models/login.response'

const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loginError = ref('')
const isSubmitting = ref(false)
const rememberMe = ref(false)

const FALLBACK_HOME_BY_ROLE: Record<LoginResponse['role'], string> = {
  ADMIN: '/admin/dashboard',
  USER: '/user/dashboard',
}

const isSafeInternalPath = (path: unknown): path is string =>
  typeof path === 'string' && path.startsWith('/') && !path.startsWith('//')

const resolveRedirectTarget = (payload: LoginResponse): string => {
  const redirectQuery = route.query.redirect
  const redirectParam = Array.isArray(redirectQuery)
    ? (redirectQuery[0] as LocationQueryValue)
    : (redirectQuery as LocationQueryValue)

  if (isSafeInternalPath(redirectParam)) return redirectParam
  if (isSafeInternalPath(payload.homeURL)) return payload.homeURL
  return FALLBACK_HOME_BY_ROLE[payload.role] ?? '/login'
}

const handleLogin = async () => {
  if (isSubmitting.value) return

  loginError.value = ''
  isSubmitting.value = true

  try {
    const response = await AuthService.login({
      email: email.value.trim(),
      password: password.value,
    })

    const payload = response.payload
    if (!payload?.role || !payload?.sessionToken) {
      loginError.value = 'Login failed: invalid response from server.'
      return
    }

    saveCredentials()

    await router.push(resolveRedirectTarget(payload))
  } catch (error: unknown) {
    if (error instanceof AuthException) {
      loginError.value = error.message
    } else if (error instanceof Error) {
      loginError.value = error.message
    } else if (typeof error === 'object' && error !== null) {
      const errObj = error as Record<string, unknown>
      if (Array.isArray(errObj.messages) && errObj.messages.length > 0) {
        loginError.value = String(errObj.messages[0])
      } else if ('message' in errObj && errObj.message) {
        loginError.value = String(errObj.message)
      } else {
        loginError.value = 'An unexpected error occurred during login.'
      }
    } else {
      loginError.value = 'An unexpected error occurred during login.'
    }
  } finally {
    isSubmitting.value = false
  }
}

const saveCredentials = () => {
  if (rememberMe.value) {
    localStorage.setItem('email', email.value)
  } else {
    localStorage.removeItem('email')
  }
}

const loadCredentials = () => {
  const saveEmail = localStorage.getItem('email')
  if (saveEmail) {
    email.value = saveEmail
    rememberMe.value = true
  }
}

onMounted(async () => {
  loadCredentials()
})
</script>

<template>
  <div
    class="bg-surface-container-lowest min-h-screen flex flex-col items-center justify-center text-on-surface antialiased overflow-hidden selection:bg-primary/30 relative"
  >
    <!-- Ambient Background Elements -->
    <div class="fixed inset-0 tech-pattern z-0"></div>
    <div
      class="fixed top-[-10%] left-[-10%] w-[50%] h-[50%] bg-primary/5 blur-[120px] rounded-full z-0"
    ></div>
    <div
      class="fixed bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-secondary-container/10 blur-[120px] rounded-full z-0"
    ></div>

    <main class="relative z-10 w-full max-w-md px-6 py-12 flex flex-col items-center">
      <!-- Brand Header -->
      <div class="mb-12 text-center">
        <div class="flex items-center justify-center gap-3 mb-4">
          <div
            class="w-12 h-12 bg-gradient-to-br from-primary to-primary-container rounded-xl flex items-center justify-center shadow-lg shadow-primary/20"
          >
            <span
              class="material-symbols-outlined text-on-primary text-3xl"
              style="font-variation-settings: 'FILL' 1"
              >security</span
            >
          </div>
        </div>
        <h1 class="text-3xl font-black tracking-tighter text-slate-100 mb-1 uppercase">
          GeoLicense
        </h1>
        <p class="text-on-surface-variant text-sm tracking-widest font-medium uppercase opacity-70">
          Management License
        </p>
      </div>

      <!-- Auth Card -->
      <div
        class="w-full bg-surface-container/60 backdrop-blur-2xl p-8 rounded-xl shadow-2xl shadow-black/40 border-t border-white/5 relative group transition-all duration-500"
      >
        <div
          class="absolute inset-0 bg-gradient-to-b from-white/5 to-transparent rounded-xl pointer-events-none"
        ></div>
        <div class="relative z-10">
          <div class="mb-8">
            <h2 class="text-2xl font-bold text-on-surface tracking-tight">LOGIN</h2>
            <p class="text-on-surface-variant text-sm mt-1">
              Enter your email and password to authenticate
            </p>
          </div>

          <div
            v-if="loginError"
            class="bg-error-container text-on-error-container px-4 py-3 rounded-lg mb-6 text-sm font-medium"
          >
            {{ loginError }}
          </div>

          <form @submit.prevent="handleLogin" class="space-y-6">
            <!-- Email Field -->
            <div class="space-y-2">
              <label
                class="block text-xs font-bold uppercase tracking-widest text-on-surface-variant"
                for="email"
                >Email</label
              >
              <div class="relative">
                <span
                  class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant text-xl"
                  >alternate_email</span
                >
                <input
                  v-model="email"
                  type="email"
                  id="email"
                  placeholder="email@mail.com"
                  required
                  class="w-full bg-surface-container-highest/50 border-none rounded-lg py-3.5 pl-12 pr-4 text-on-surface placeholder:text-on-surface-variant/40 focus:ring-2 focus:ring-primary/50 transition-all duration-200 outline-none"
                />
              </div>
            </div>

            <!-- Password Field -->
            <div class="space-y-2">
              <label
                class="block text-xs font-bold uppercase tracking-widest text-on-surface-variant"
                for="password"
                >Password</label
              >
              <div class="relative">
                <span
                  class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant text-xl"
                  >lock</span
                >
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
                  <span class="material-symbols-outlined text-xl">{{
                    showPassword ? 'visibility_off' : 'visibility'
                  }}</span>
                </button>
              </div>
            </div>

            <!-- Options Row -->
            <div class="flex items-center justify-between">
              <label class="flex items-center gap-3 cursor-pointer group">
                <div class="relative flex items-center justify-center">
                  <input
                    v-model="rememberMe"
                    type="checkbox"
                    class="peer appearance-none w-5 h-5 rounded border border-outline-variant/30 bg-surface-container-highest checked:bg-primary transition-all duration-200 cursor-pointer"
                  />
                  <span
                    class="material-symbols-outlined absolute text-[16px] text-on-primary scale-0 peer-checked:scale-100 transition-transform pointer-events-none"
                    style="font-variation-settings: 'wght' 700"
                    >check</span
                  >
                </div>
                <span
                  class="text-sm text-on-surface-variant group-hover:text-on-surface transition-colors"
                  >Remember</span
                >
              </label>
              <a
                href="#"
                class="text-sm font-semibold text-primary hover:text-primary-container transition-all"
                >Recovery Password?</a
              >
            </div>

            <!-- Sign In Button -->
            <button
              type="submit"
              :disabled="isSubmitting"
              :aria-busy="isSubmitting"
              class="w-full py-4 bg-gradient-to-r from-primary to-primary-container text-on-primary font-bold rounded-lg shadow-lg shadow-primary/20 hover:shadow-primary/30 hover:scale-[1.01] active:scale-[0.98] transition-all duration-200 uppercase tracking-widest text-xs flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed disabled:hover:scale-100"
            >
              <template v-if="isSubmitting">
                <span class="material-symbols-outlined text-lg animate-spin" aria-hidden="true"
                  >progress_activity</span
                >
                AUTHENTICATING
              </template>
              <template v-else>
                LOGIN
                <span class="material-symbols-outlined text-lg" aria-hidden="true">login</span>
              </template>
            </button>
          </form>
        </div>
      </div>

      <!-- System Message -->
      <div
        class="mt-8 flex items-center gap-2 px-4 py-2 bg-surface-container-low/80 rounded-full border border-white/5"
      >
        <span class="w-2 h-2 rounded-full bg-green-300 animate-pulse"></span>
        <span class="text-[10px] font-bold uppercase tracking-[0.2em] text-on-surface-variant"
          >Status: ONLINE</span
        >
      </div>
    </main>

    <!-- Footer -->
    <footer
      class="fixed bottom-0 w-full py-8 bg-[#060e20] flex flex-col md:flex-row justify-between items-center px-12 gap-4 z-20"
    >
      <div class="text-sm font-black text-slate-500">GeoLicense</div>
      <div class="flex gap-8">
        <a
          href="#"
          class="text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100"
          >Privacy Policy</a
        >
        <a
          href="#"
          class="text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100"
          >Terms of Service</a
        >
        <a
          href="#"
          class="text-xs uppercase tracking-widest text-slate-500 hover:text-blue-400 transition-all opacity-80 hover:opacity-100"
          >Security Architecture</a
        >
      </div>
      <div class="text-slate-100 text-xs uppercase tracking-widest">
        © 2026 GeoLicense. Created By AlexistDev
      </div>
    </footer>

    <!-- Imagery Decoration -->
    <div
      class="hidden lg:block fixed right-0 top-0 bottom-0 w-1/3 z-0 pointer-events-none overflow-hidden"
    >
      <div
        class="absolute inset-0 bg-gradient-to-r from-surface-container-lowest via-transparent to-transparent z-10"
      ></div>
      <img
        src="https://lh3.googleusercontent.com/aida-public/AB6AXuCREepqt05Wd6lSmmDITYTrobI8SXhb_fOgi40eqmnxKuN5PN-r34iHd7t7oSIs5yEg0W6AdcTVLjGw00z-tUNubI5ZuyR2xQjlhsCBApARS7Yebu-NCv3h16Z5NXSChM4FEaAFFHSqWpFbYJMFRpjPEMVOQVqCGPGpPVRkSvI8cO5-G2SzCgqCt6IQP5gUKNbXhNNzEGQmsQUBT-vgOsQg2JcD9LT8h36Y40bKvYs-jjHPMbMZ3-PMMSG-W8bvNlTQ7t9YKHE3B6M"
        alt="Tech Infrastructure Background"
        class="h-full object-cover opacity-20 grayscale"
      />
    </div>
  </div>
</template>
