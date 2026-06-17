<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const auth   = useAuthStore()
const router = useRouter()

const DEMO_ACCOUNTS = [
  {
    key: 'gv_a',
    label: 'Giảng Viên A',
    sub: 'TS. Nguyễn Văn Anh — Khoa CNTT',
    icon: 'pi pi-user',
    role: 'Giảng viên',
    color: 'var(--color-accent)',
  },
  {
    key: 'gv_b',
    label: 'Giảng Viên B',
    sub: 'PGS. Trần Thị Bình — Khoa Toán',
    icon: 'pi pi-user',
    role: 'Giảng viên',
    color: 'var(--color-accent)',
  },
  {
    key: 'nckh',
    label: 'Nhân viên P.NCKH',
    sub: 'CN. Lê Văn Cường',
    icon: 'pi pi-building',
    role: 'P.NCKH',
    color: 'var(--color-amber)',
  },
  {
    key: 'pb_1',
    label: 'Phản Biện 1',
    sub: 'TS. Phạm Quang Đức',
    icon: 'pi pi-search',
    role: 'Tổ Phản Biện',
    color: '#0284C7',
  },
  {
    key: 'pb_2',
    label: 'Phản Biện 2',
    sub: 'PGS. Vũ Thị Em',
    icon: 'pi pi-search',
    role: 'Tổ Phản Biện',
    color: '#0284C7',
  },
]

function loginAs(key) {
  auth.loginAs(key)
  router.push('/')
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
          <span class="brand-icon">R</span>
          <span class="brand-name">RGMS</span>
        </div>
        <h1 class="brand-title">Hệ thống Quản lý Nghiên cứu Khoa học</h1>
        <p class="brand-desc">
          Quản lý toàn bộ vòng đời đề tài NCKH — từ đăng ký, thẩm định, phản biện đến ký hợp đồng và nghiệm thu.
        </p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>Theo dõi tiến độ theo thời gian thực</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>Quy trình phê duyệt tự động hóa</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>Quản lý phản biện & hội đồng</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>Báo cáo & thống kê tổng hợp</span>
          </div>
        </div>
      </div>

      <!-- Right: login panel -->
      <div class="login-panel">
        <div class="login-card">
          <!-- Header -->
          <div class="login-card-header">
            <div class="login-logo">
              <span class="login-logo-icon">R</span>
              <span class="login-logo-name">RGMS</span>
            </div>
            <div class="login-badge">Mockup Mode</div>
          </div>

          <h2 class="login-title">Đăng nhập hệ thống</h2>
          <p class="login-sub">Chọn tài khoản demo để trải nghiệm từng vai trò</p>

          <!-- Demo accounts -->
          <div class="demo-accounts">
            <button
              v-for="acc in DEMO_ACCOUNTS"
              :key="acc.key"
              class="demo-card"
              :style="{ '--acc-color': acc.color }"
              @click="loginAs(acc.key)"
            >
              <div class="demo-avatar" :style="{ background: acc.color, color: '#fff' }">
                <i :class="acc.icon"></i>
              </div>
              <div class="demo-info">
                <div class="demo-name">{{ acc.label }}</div>
                <div class="demo-sub">{{ acc.sub }}</div>
              </div>
              <div class="demo-role-chip" :style="{ color: acc.color, borderColor: acc.color + '40', background: acc.color + '12' }">
                {{ acc.role }}
              </div>
            </button>
          </div>

          <!-- Note -->
          <div class="login-note">
            <span class="note-icon"><i class="pi pi-info-circle"></i></span>
            <span>Nhấn <kbd>F5</kbd> để reset toàn bộ dữ liệu về trạng thái ban đầu</span>
          </div>
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
.brand-icon {
  width: 44px; height: 44px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-lg);
  display: flex; align-items: center; justify-content: center;
  font: 700 22px/1 var(--font-sans);
  border: 1px solid rgba(255,255,255,0.1);
}
.brand-name {
  font: 700 24px/1 var(--font-sans);
  color: var(--color-text-primary);
  letter-spacing: -0.03em;
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
.login-logo-icon {
  width: 28px; height: 28px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  font: 700 14px/1 var(--font-sans);
}
.login-logo-name {
  font: 700 16px/1 var(--font-sans);
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
}

.login-badge {
  font: var(--text-caption);
  font-weight: 600;
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
  border: 1px solid var(--color-warning-border);
  border-radius: 999px;
  padding: 2px 10px;
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

/* ── DEMO ACCOUNTS ────────────────────────────────── */
.demo-accounts {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-bottom: var(--space-5);
}

.demo-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  text-align: left;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), transform var(--transition-fast);
}
.demo-card:hover {
  border-color: var(--acc-color);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--acc-color) 12%, transparent);
  transform: translateX(2px);
}

.demo-avatar {
  width: 36px; height: 36px;
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  opacity: 0.9;
}

.demo-info { flex: 1; min-width: 0; }
.demo-name { font: var(--text-h4); color: var(--color-text-primary); }
.demo-sub  { font: var(--text-sm); color: var(--color-text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.demo-role-chip {
  font: var(--text-caption);
  font-weight: 600;
  border: 1px solid;
  border-radius: 999px;
  padding: 2px 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* ── NOTE ─────────────────────────────────────────── */
.login-note {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface-alt);
  border-radius: var(--radius-md);
  font: var(--text-sm);
  color: var(--color-text-muted);
}
.note-icon { font-size: 14px; flex-shrink: 0; }
kbd {
  display: inline-block;
  padding: 0 5px;
  font: var(--text-mono);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 3px;
  font-size: 11px;
}
</style>
