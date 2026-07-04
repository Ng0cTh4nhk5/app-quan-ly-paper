import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import api from '@/api/axios'

const routes = [
  { path: '/', redirect: () => {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) return '/login'
    if (auth.role === 'GIANG_VIEN')   return '/gv/dashboard'
    if (auth.role === 'NCKH')         return '/nckh/dashboard'
    if (auth.role === 'TO_PHAN_BIEN') return '/pb/de-tai'
    return '/login'
  }},

  { path: '/login',     component: () => import('@/pages/LoginPage.vue') },
  { path: '/forbidden', component: () => import('@/pages/ForbiddenPage.vue') },

  // ── GIẢNG VIÊN ──────────────────────────────────
  {
    path: '/gv',
    component: () => import('@/layouts/GVLayout.vue'),
    meta: { requiresRole: 'GIANG_VIEN' },
    children: [
      { path: '',               redirect: '/gv/dashboard' },
      { path: 'dashboard',      component: () => import('@/pages/gv/Dashboard.vue') },
      { path: 'de-tai',         component: () => import('@/pages/gv/DanhSachDetai.vue') },
      { path: 'de-tai/tao-moi', component: () => import('@/pages/gv/TaoDetai.vue') },
      { path: 'de-tai/:id',     component: () => import('@/pages/gv/ChiTietDetai.vue'), meta: { checkGvOwner: true } },
      { path: 'de-tai/:id/bo-sung',  component: () => import('@/pages/gv/BoSungHoSo.vue'), meta: { checkGvOwner: true } },
      { path: 'de-tai/:id/hop-dong', component: () => import('@/pages/gv/XemHopDong.vue'), meta: { checkGvOwner: true } },
    ]
  },

  // ── P.NCKH ───────────────────────────────────────
  {
    path: '/nckh',
    component: () => import('@/layouts/NCKHLayout.vue'),
    meta: { requiresRole: 'NCKH' },
    children: [
      { path: '',               redirect: '/nckh/dashboard' },
      { path: 'dashboard',      component: () => import('@/pages/nckh/Dashboard.vue') },
      { path: 'inbox',          component: () => import('@/pages/nckh/Inbox.vue') },
      { path: 'dang-xu-ly',     component: () => import('@/pages/nckh/DangXuLy.vue') },
      { path: 'de-tai/:id',     component: () => import('@/pages/nckh/ChiTietDetai.vue') },
      { path: 'de-tai/:id/hop-dong', component: () => import('@/pages/nckh/SoanHopDong.vue') },
    ]
  },

  // ── TỔ PHẢN BIỆN ─────────────────────────────────
  {
    path: '/pb',
    component: () => import('@/layouts/PBLayout.vue'),
    meta: { requiresRole: 'TO_PHAN_BIEN' },
    children: [
      { path: '',       redirect: '/pb/de-tai' },
      { path: 'de-tai',     component: () => import('@/pages/pb/DanhSachAssigned.vue') },
      { path: 'de-tai/:id', component: () => import('@/pages/pb/DanhGiaForm.vue') },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const required = to.meta.requiresRole
  if (!required) return true
  if (!auth.isLoggedIn) return '/login'
  if (auth.role !== required) return '/forbidden'
  if (to.meta.checkGvOwner && to.params.id) {
    try {
      await api.get(`/de-tai/${to.params.id}`)
    } catch (e) {
      if (e.response?.status === 403) return '/forbidden'
      if (e.response?.status === 404) return '/forbidden'
      return false
    }
  }
  return true
})

export default router
