<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { Search, LogOut } from '@lucide/vue'

const auth   = useAuthStore()
const router = useRouter()
const route  = useRoute()

const initials = computed(() => {
  const name = auth.user?.hoTen ?? ''
  return name.split(' ').slice(-2).map(n => n[0]).join('').toUpperCase()
})

const pageTitle = computed(() => {
  const map = {
    '/pb/de-tai': 'Đề tài được phân công',
  }
  if (route.path.startsWith('/pb/de-tai/')) return 'Đánh giá đề tài'
  return map[route.path] ?? 'Tổ Phản Biện'
})

function logout() { auth.logout(); router.push('/login') }
</script>

<template>
  <div class="layout-wrapper">
    <!-- SIDEBAR -->
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-mark">P</div>
        <div class="logo-text">
          <span class="logo-name">RGMS</span>
          <span class="logo-desc">Tổ Phản Biện</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <span class="sidebar-section-label">Phản biện</span>
        <RouterLink to="/pb/de-tai">
          <Search class="nav-icon" :size="18" /> Đề tài phân công
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="sidebar-user-avatar">{{ initials }}</div>
          <div class="sidebar-user-info">
            <div class="sidebar-user-name">{{ auth.user?.hoTen }}</div>
            <div class="sidebar-user-role">Tổ Phản Biện</div>
          </div>
        </div>
        <button class="btn btn-ghost btn-sm sidebar-logout" @click="logout">
          <LogOut :size="16" /> Đăng xuất
        </button>
      </div>
    </aside>

    <!-- MAIN -->
    <main class="main-content">
      <header class="topbar">
        <div class="topbar-breadcrumb">
          <span>Tổ Phản Biện</span>
          <span class="sep">/</span>
          <strong>{{ pageTitle }}</strong>
        </div>
        <div class="topbar-actions">
          <div class="topbar-user">
            <div class="topbar-avatar">{{ initials }}</div>
            <span class="topbar-user-name">{{ auth.user?.hoTen?.split(' ').pop() }}</span>
          </div>
        </div>
      </header>
      <div class="page-content">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.sidebar-logo {
  display: flex; align-items: center; gap: var(--space-3);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  flex-shrink: 0;
}
.logo-mark {
  width: 32px; height: 32px;
  background: var(--color-accent);
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  font: 700 16px/1 var(--font-sans);
  color: #fff;
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
.page-content { flex: 1; padding: var(--space-8) var(--space-8); overflow-y: auto; }
</style>
