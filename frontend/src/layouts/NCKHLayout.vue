<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import api from '@/api/axios'
import { LayoutDashboard, Inbox, Settings, LogOut, Menu, X } from '@lucide/vue'

const auth   = useAuthStore()
const router = useRouter()
const route  = useRoute()
const inboxCount = ref(0)

// ── Mobile Menu State ────────────────────────────────
const isMobileMenuOpen = ref(false)

watch(() => route.path, () => {
  isMobileMenuOpen.value = false
})

watch(isMobileMenuOpen, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})

const initials = computed(() => {
  const name = auth.user?.hoTen ?? ''
  return name.split(' ').slice(-2).map(n => n[0]).join('').toUpperCase()
})

const pageTitle = computed(() => {
  const map = {
    '/nckh/dashboard':  'Tổng quan',
    '/nckh/inbox':      'Hộp thư đến',
    '/nckh/dang-xu-ly': 'Đang xử lý',
  }
  return map[route.path] ?? 'P.NCKH'
})

onMounted(async () => {
  try {
    const res = await api.get('/de-tai', { params: { trangThai: 'CHO_PNCKH_XEM_XET' } })
    const list = Array.isArray(res.data) ? res.data : res.data.content
    inboxCount.value = list.length
  } catch {}
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
      <div class="sidebar-logo">
        <img
          src="/logo.png"
          alt="Trường Đại học Mở TP. Hồ Chí Minh"
          class="logo-image"
        />
        <div class="logo-text">
          <span class="logo-name">QLNCKH</span>
          <span class="logo-desc">Phòng NCKH</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <span class="sidebar-section-label">Tổng quan</span>
        <RouterLink to="/nckh/dashboard">
          <LayoutDashboard class="nav-icon" :size="18" /> Dashboard
        </RouterLink>

        <span class="sidebar-section-label">Xử lý hồ sơ</span>
        <RouterLink to="/nckh/inbox">
          <Inbox class="nav-icon" :size="18" /> Hộp thư đến
          <span v-if="inboxCount > 0" class="inbox-badge">{{ inboxCount }}</span>
        </RouterLink>
        <RouterLink to="/nckh/dang-xu-ly">
          <Settings class="nav-icon" :size="18" /> Đang xử lý
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="sidebar-user-avatar">{{ initials }}</div>
          <div class="sidebar-user-info">
            <div class="sidebar-user-name">{{ auth.user?.hoTen }}</div>
            <div class="sidebar-user-role">P.NCKH</div>
          </div>
        </div>
        <button class="btn btn-ghost btn-sm sidebar-logout" @click="logout">
          <LogOut :size="16" /> Đăng xuất
        </button>
      </div>
    </aside>

    <!-- ── MAIN ───────────────────────────────────── -->
    <main class="main-content">
      <header class="topbar">
        <!-- Nút Hamburger -->
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
          <span>P.NCKH</span>
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
  min-height: var(--topbar-height);
}
.logo-image {
  height: 40px;
  width: auto;
  flex-shrink: 0;
  display: block;
}
.logo-text { display: flex; flex-direction: column; }
.logo-name { font: 700 14px/1 var(--font-sans); color: #fff; }
.logo-desc { font: var(--text-caption); color: #64748B; margin-top: 2px; }

.inbox-badge {
  margin-left: auto;
  background: var(--color-amber);
  color: #fff;
  border-radius: 999px;
  font: var(--text-caption);
  font-weight: 600;
  padding: 1px 7px;
  min-width: 20px;
  text-align: center;
}
.sidebar-user-info { flex: 1; overflow: hidden; }
.sidebar-logout {
  width: 100%;
  justify-content: center;
  margin-top: var(--space-2);
  color: #64748B;
  font-size: 12px;
}
.sidebar-logout:hover { color: #CBD5E1 !important; }

.page-content { flex: 1; padding: var(--space-8) var(--space-10); overflow-y: auto; }

.overlay-enter-active,
.overlay-leave-active { transition: opacity 0.2s ease; }
.overlay-enter-from,
.overlay-leave-to     { opacity: 0; }

@media (max-width: 1024px) {
  .page-content { padding: var(--space-6) var(--space-6); }
}
@media (max-width: 640px) {
  .page-content { padding: var(--space-4) var(--space-4); }
}
</style>
