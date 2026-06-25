<script setup>
import { ref, watch, computed } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { LayoutDashboard, List, Plus, LogOut, Menu, X } from '@lucide/vue'

const auth   = useAuthStore()
const router = useRouter()
const route  = useRoute()

// ── Mobile Menu State ────────────────────────────────
const isMobileMenuOpen = ref(false)

// Tự động đóng menu khi chuyển trang
watch(() => route.path, () => {
  isMobileMenuOpen.value = false
})

// Khoá scroll body khi menu mở
watch(isMobileMenuOpen, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})

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
    <!-- ── Overlay mờ (mobile/tablet) ─────────────── -->
    <Transition name="overlay">
      <div
        v-if="isMobileMenuOpen"
        class="sidebar-overlay"
        @click="isMobileMenuOpen = false"
      />
    </Transition>

    <!-- ── SIDEBAR ────────────────────────────────── -->
    <aside class="sidebar" :class="{ 'is-open': isMobileMenuOpen }">
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
          <LayoutDashboard class="nav-icon" :size="18" /> Dashboard
        </RouterLink>

        <span class="sidebar-section-label">Đề tài</span>
        <RouterLink to="/gv/de-tai" exact>
          <List class="nav-icon" :size="18" /> Đề tài của tôi
        </RouterLink>
        <RouterLink to="/gv/de-tai/tao-moi">
          <Plus class="nav-icon" :size="18" /> Đăng ký đề tài
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
          <LogOut :size="16" /> Đăng xuất
        </button>
      </div>
    </aside>

    <!-- ── MAIN ───────────────────────────────────── -->
    <main class="main-content">
      <!-- Topbar -->
      <header class="topbar">
        <!-- Nút Hamburger (hiện trên mobile/tablet <= 1024px) -->
        <button
          class="btn-menu-toggle"
          :aria-label="isMobileMenuOpen ? 'Đóng menu' : 'Mở menu'"
          :aria-expanded="isMobileMenuOpen"
          @click="isMobileMenuOpen = !isMobileMenuOpen"
        >
          <X v-if="isMobileMenuOpen" :size="20" />
          <Menu v-else :size="20" />
        </button>

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
  min-height: var(--topbar-height);
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
  padding: var(--space-8) var(--space-10);
  overflow-y: auto;
}

/* Transition cho overlay */
.overlay-enter-active,
.overlay-leave-active {
  transition: opacity 0.2s ease;
}
.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

/* Tablet padding */
@media (max-width: 1024px) {
  .page-content { padding: var(--space-6) var(--space-6); }
}

/* Mobile padding */
@media (max-width: 640px) {
  .page-content { padding: var(--space-4) var(--space-4); }
}
</style>
