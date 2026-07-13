<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const auth   = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const loginError = ref('')
const loggingIn = ref(false)

async function login() {
  loginError.value = ''
  loggingIn.value = true
  try {
    await auth.login(form)
    router.push('/')
  } catch (error) {
    loginError.value = error.response?.data?.message ?? error.message ?? 'Đăng nhập thất bại.'
  } finally {
    loggingIn.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- Background grid pattern -->
    <div class="login-bg-grid"></div>

    <!-- Card wrapper -->
    <div class="login-layout">
      <!-- Left: brand panel -->
      <div class="login-brand">
        <div class="brand-logo">
          <img
            src="/logo.png"
            alt="Trường Đại học Mở TP. Hồ Chí Minh"
            class="brand-logo-image"
          />
        </div>
        <h1 class="brand-title">Hệ thống Quản lý Đề tài NCKH cấp cơ sở</h1>
      </div>

      <!-- Right: login panel -->
      <div class="login-panel">
        <div class="login-card">
          <!-- Header -->
          <div class="login-card-header">
            <div class="login-logo">
              <img
                src="/logo.png"
                alt="Trường Đại học Mở TP. Hồ Chí Minh"
                class="login-logo-image"
              />
            </div>
          </div>

          <h2 class="login-title">Đăng nhập hệ thống</h2>
          <p class="login-sub">Sử dụng tài khoản được cấp để đăng nhập</p>

          <form class="real-login-form" @submit.prevent="login">
            <div class="form-group">
              <label class="form-label">Tài khoản</label>
              <input v-model="form.username" class="form-input" type="text" autocomplete="username" placeholder="Nhập tên tài khoản..." required />
            </div>
            <div class="form-group">
              <label class="form-label">Mật khẩu</label>
              <input v-model="form.password" class="form-input" type="password" autocomplete="current-password" placeholder="Nhập mật khẩu..." required />
            </div>
            <div v-if="loginError" class="alert alert-danger">{{ loginError }}</div>
            <button class="btn btn-primary w-full" type="submit" :disabled="loggingIn">
              {{ loggingIn ? 'Đang đăng nhập...' : 'Đăng nhập' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── PAGE ─────────────────────────────────────────── */
.login-page {
  min-height: 100svh;
  background: var(--color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  position: relative;
  overflow: hidden;
}

.login-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--color-border) 1px, transparent 1px),
    linear-gradient(90deg, var(--color-border) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.4;
  pointer-events: none;
}

/* ── LAYOUT ───────────────────────────────────────── */
.login-layout {
  display: grid;
  grid-template-columns: 1fr 440px;
  gap: var(--space-12);
  width: 100%;
  max-width: 960px;
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .login-layout { grid-template-columns: 1fr; }
  .login-brand  { display: none; }
}

/* ── BRAND PANEL ──────────────────────────────────── */
.login-brand {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-6);
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.brand-logo-image {
  height: 64px;
  width: auto;
  display: block;
}

.brand-title {
  font: 700 32px/1.2 var(--font-sans);
  letter-spacing: -0.03em;
  color: var(--color-text-primary);
}

.brand-desc {
  font: var(--text-body-lg);
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-top: var(--space-2);
}
.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font: var(--text-body);
  color: var(--color-text-secondary);
}
.feature-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: var(--color-accent);
  flex-shrink: 0;
}

/* ── LOGIN CARD ───────────────────────────────────── */
.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-8) var(--space-8);
  width: 100%;
}

.login-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.login-logo {
  display: flex; align-items: center; gap: var(--space-2);
}
.login-logo-image {
  height: 48px;
  width: auto;
  display: block;
}

.login-title {
  font: var(--text-h2);
  letter-spacing: var(--letter-h2);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}
.login-sub {
  font: var(--text-body);
  color: var(--color-text-muted);
  margin-bottom: var(--space-5);
}

/* ── LOGIN FORM ──────────────────────────────────── */
.real-login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
</style>
