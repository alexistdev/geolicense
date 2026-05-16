import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import LicenseView from '@/modules/user/license/views/LicenseView.vue'
import MasterUserView from '@/modules/administrator/master/views/MasterUserView.vue'
import MasterProductView from '@/modules/administrator/master/views/MasterProductView.vue'
import MasterLicenseTypeView from '@/modules/administrator/master/views/MasterLicenseTypeView.vue'
import LicenseDetailView from '@/modules/user/license/views/LicenseDetailView.vue'
import MarketplaceView from '@/modules/user/marketplace/views/MarketplaceView.vue'
import MarketplaceDetailView from '@/modules/user/marketplace/views/MarketplaceDetailView.vue'


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
      path: '/admin/license_types',
      name: 'admin-license-type',
      component: MasterLicenseTypeView,
    },
    {
      path: '/user/dashboard',
      name: 'user-dashboard',
      component: DashboardView,
    },
    {
      path: '/user/marketplace',
      name: 'user-marketplace',
      component: MarketplaceView,
    },
    {
      path: '/user/marketplace/:productId',
      name: 'user-marketplace-detail',
      component: MarketplaceDetailView,
    },
    {
      path: '/user/license',
      name: 'user-license',
      component: LicenseView,
    },
    {
      path: '/user/license/:id',
      name: 'user-license-detail',
      component: LicenseDetailView,
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
