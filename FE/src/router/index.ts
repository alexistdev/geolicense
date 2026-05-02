import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import LicenseView from '@/modules/user/license/views/LicenseView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/staff/dashboard',
      name: 'staff-dashboard',
      component: DashboardView,
    },
    {
      path: '/user/dashboard',
      name: 'user-dashboard',
      component: DashboardView,
    },
    {
      path: '/user/license',
      name: 'user-license',
      component: LicenseView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
  ],
})

export default router
