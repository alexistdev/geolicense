import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import LicenseView from '@/modules/user/license/views/LicenseView.vue'
import MasterUserView from '@/modules/administrator/master/views/MasterUserView.vue'
import MasterProductView from '@/modules/administrator/master/views/MasterProductView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: DashboardView,
    },
    {
      path: '/admin/users',
      name: 'admin-user',
      component: MasterUserView,
    },
    {
      path: '/admin/products',
      name: 'admin-product',
      component: MasterProductView,
    },
    {
      path: '/user/dashboard',
      name: 'user-dashboard',
      component: DashboardView,
    },
    {
      path: '/users/license',
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
