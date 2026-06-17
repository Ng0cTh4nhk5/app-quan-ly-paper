<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const auth   = useAuthStore()
const router = useRouter()
const route  = useRoute()

const initials = computed(() => {
  const name = auth.user?.hoTen ?? ''
  return name.split(' ').slice(-2).map(n => n[0]).join('').toUpperCase()
})

const pageTitle = computed(() => {
  const map = {
    '/gv/dashboard':       'Tổng quan',
    '/gv/de-tai':          'Đề tài của tôi',
    '/gv/de-tai/tao-moi':  'Đăng ký đề tài mới',
  }
  if (route.path.startsWith('/gv/de-tai/') && route.path !== '/gv/de-tai/tao-moi') return 'Chi tiết đề tài'
  return map[route.path] ?? 'RGMS'
})

function logout() { auth.logout(); router.push('/login') }
</script>

<template>
  <div class="layout-wrapper">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-mark">R</div>
        <div class="logo-text">
          <span class="logo-name">RGMS</span>
          <span class="logo-desc">Research Grants</span>
        </div>
      </div>

      <!-- Nav -->
      <nav class="sidebar-nav">
        <span class="sidebar-section-label">Tổng quan</span>
        <RouterLink to="/gv/dashboard">
          <i class="pi pi-th-large nav-icon"></i> Dashboard
        </RouterLink>

        <span class="sidebar-section-label">Đề tài</span>
        <RouterLink to="/gv/de-tai" exact>
          <i class="pi pi-list nav-icon"></i> Đề tài của tôi
        </RouterLink>
        <RouterLink to="/gv/de-tai/tao-moi">
          <i class="pi pi-plus nav-icon"></i> Đăng ký đề tài
        </RouterLink>
      </nav>

      <!-- Footer user -->
      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="sidebar-user-avatar">{{ initials }}</div>
          <div class="sidebar-user-info">
            <div class="sidebar-user-name">{{ auth.user?.hoTen }}</div>
            <div class="sidebar-user-role">Giảng viên</div>
          </div>
        </div>
        <button class="btn btn-ghost btn-sm sidebar-logout" @click="logout">
          <i class="pi pi-sign-out"></i> Đăng xuất
        </button>
      </div>
    </aside>

    <!-- MAIN -->
    <main class="main-content">
      <!-- Topbar -->
      <header class="topbar">
        <div class="topbar-breadcrumb">
          <span>RGMS</span>
          <span class="sep">/</span>
          <strong>{{ pageTitle }}</strong>
        </div>
        <div class="topbar-actions">
          <div class="topbar-user" title="Tài khoản của tôi">
            <div class="topbar-avatar">{{ initials }}</div>
            <span class="topbar-user-name">{{ auth.user?.hoTen?.split(' ').pop() }}</span>
          </div>
        </div>
      </header>

      <!-- Page content -->
      <div class="page-content">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}
.logo-mark {
  width: 32px; height: 32px;
  background: var(--color-accent);
  color: #fff;
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  font: 700 16px/1 var(--font-sans);
  flex-shrink: 0;
}
.logo-text { display: flex; flex-direction: column; }
.logo-name { font: 700 14px/1 var(--font-sans); color: #fff; }
.logo-desc { font: var(--text-caption); color: #64748B; margin-top: 2px; }

.sidebar-user-info { flex: 1; overflow: hidden; }
.sidebar-logout {
  width: 100%;
  justify-content: center;
  margin-top: var(--space-2);
  color: #64748B;
  font-size: 12px;
}
.sidebar-logout:hover { color: #CBD5E1 !important; }

.page-content {
  flex: 1;
  padding: var(--space-8) var(--space-8);
  overflow-y: auto;
}
</style>
