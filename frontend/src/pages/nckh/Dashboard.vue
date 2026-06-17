<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useNckhStore } from '@/stores/nckh.store'
import { useAuthStore } from '@/stores/auth.store'
import StatusBadge from '@/components/StatusBadge.vue'
import { Sparkles, Inbox, Settings, CheckCircle, List, TriangleAlert, CircleAlert, Star } from '@lucide/vue'

const store  = useNckhStore()
const auth   = useAuthStore()
const router = useRouter()

onMounted(() => store.layDanhSach())

const list      = computed(() => store.danhSach ?? [])
const inbox     = computed(() => list.value.filter(d => d.trangThai === 'CHO_PNCKH_XEM_XET'))
const inProcess = computed(() => list.value.filter(d => ['DANG_PHAN_BIEN', 'DANG_LAP_HOP_DONG'].includes(d.trangThai)))
const done      = computed(() => list.value.filter(d => d.trangThai === 'DA_HOAN_THANH'))
const urgent    = computed(() => inbox.value.slice(0, 5))

const stats = [
  { icon: Inbox, label: 'Chờ xem xét',    key: 'inbox',     iconClass: 'stat-icon-amber' },
  { icon: Settings, label: 'Đang xử lý',     key: 'inProcess', iconClass: 'stat-icon-blue'  },
  { icon: CheckCircle, label: 'Đã hoàn thành',  key: 'done',      iconClass: 'stat-icon-green' },
  { icon: List, label: 'Tổng hồ sơ',     key: 'total',     iconClass: 'stat-icon-teal'  },
]

const statValues = computed(() => ({
  inbox:     inbox.value.length,
  inProcess: inProcess.value.length,
  done:      done.value.length,
  total:     list.value.length,
}))

function fmt(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('vi-VN')
}

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return 'Chào buổi sáng'
  if (h < 18) return 'Chào buổi chiều'
  return 'Chào buổi tối'
})
</script>

<template>
  <div>
    <!-- Greeting -->
    <div class="greeting-row">
      <div>
        <h1 class="greeting-title">{{ greeting }}, {{ auth.user?.hoTen?.split(' ').pop() }} <Sparkles style="color: var(--color-amber);" :size="24" /></h1>
        <p class="greeting-sub">Tổng quan hồ sơ đề tài đang chờ xử lý tại Phòng NCKH.</p>
      </div>
      <button class="btn btn-primary" @click="router.push('/nckh/inbox')">
        <Inbox :size="16" /> Xem hộp thư đến
      </button>
    </div>

    <!-- Warning if inbox has items -->
    <div v-if="inbox.length > 0" class="warning-banner">
      <span><TriangleAlert :size="24" /></span>
      <div>
        <strong>Có {{ inbox.length }} hồ sơ</strong> đang chờ xem xét.
        <button class="btn-link" @click="router.push('/nckh/inbox')">Xử lý ngay →</button>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-icon" :class="s.iconClass"><component :is="s.icon" :size="24" /></div>
        <div>
          <div class="stat-value">{{ statValues[s.key] }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- Main content -->
    <div class="dash-grid">
      <!-- Priority Queue -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title"><CircleAlert style="color: var(--color-danger)" :size="20" /> Ưu tiên xử lý</h3>
          <button class="btn btn-ghost btn-sm" @click="router.push('/nckh/inbox')">Xem tất cả →</button>
        </div>

        <div v-if="store.loading" class="card-body">
          <div v-for="i in 3" :key="i" class="skeleton" style="height:52px;margin-bottom:8px"></div>
        </div>

        <div v-else-if="urgent.length === 0" class="card-body empty-msg">
          <span><Star :size="32" style="color: var(--color-amber)" /></span>
          <p>Hộp thư đến trống. Không có hồ sơ nào cần xử lý.</p>
        </div>

        <div v-else class="recent-list">
          <div
            v-for="dt in urgent" :key="dt.id"
            class="recent-item"
            @click="router.push(`/nckh/de-tai/${dt.id}`)"
          >
            <div class="recent-main">
              <div class="recent-title">{{ dt.tenDeTai }}</div>
              <div class="recent-meta">
                <span class="text-sm text-muted">{{ dt.chuNhiem || dt.giangVienId }}</span>
                <span>·</span>
                <span class="mono text-sm">{{ dt.kyNckh || '—' }}</span>
              </div>
            </div>
            <div class="urgency-chip urgency-high">Chờ duyệt</div>
          </div>
        </div>
      </div>

      <!-- Side panel -->
      <div class="side-col">
        <!-- In process -->
        <div class="card mb-4">
          <div class="card-header">
            <h3 class="card-title">Đang xử lý</h3>
            <button class="btn btn-ghost btn-sm" @click="router.push('/nckh/dang-xu-ly')">Chi tiết →</button>
          </div>
          <div v-if="inProcess.length === 0" class="card-body empty-msg">
            <span><Inbox :size="32" style="color: var(--color-text-muted)" /></span><p>Không có hồ sơ nào đang xử lý.</p>
          </div>
          <div v-else class="recent-list">
            <div
              v-for="dt in inProcess.slice(0,4)" :key="dt.id"
              class="recent-item"
              @click="router.push(`/nckh/de-tai/${dt.id}`)"
            >
              <div class="recent-main">
                <div class="recent-title">{{ dt.tenDeTai }}</div>
              </div>
              <StatusBadge :status="dt.trangThai" />
            </div>
          </div>
        </div>

        <!-- Quick actions -->
        <div class="card">
          <div class="card-header"><h3 class="card-title">Thao tác nhanh</h3></div>
          <div class="quick-actions">
            <button class="quick-action-btn" @click="router.push('/nckh/inbox')">
              <span class="qa-icon"><Inbox :size="18" /></span>
              <div>
                <div class="qa-title">Hộp thư đến</div>
                <div class="qa-sub">{{ inbox.length }} hồ sơ chờ xem xét</div>
              </div>
            </button>
            <button class="quick-action-btn" @click="router.push('/nckh/dang-xu-ly')">
              <span class="qa-icon"><Settings :size="18" /></span>
              <div>
                <div class="qa-title">Đang xử lý</div>
                <div class="qa-sub">{{ inProcess.length }} hồ sơ trong tiến trình</div>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.greeting-row {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: var(--space-4); margin-bottom: var(--space-6);
}
.greeting-title { font: var(--text-h1); letter-spacing: var(--letter-h1); color: var(--color-text-primary); }
.greeting-sub   { font: var(--text-body); color: var(--color-text-muted); margin-top: var(--space-1); }

.dash-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: var(--space-6);
  align-items: start;
}
@media (max-width: 1024px) { .dash-grid { grid-template-columns: 1fr; } }

.recent-list { display: flex; flex-direction: column; }
.recent-item {
  display: flex; align-items: center; justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.recent-item:last-child { border-bottom: none; }
.recent-item:hover { background: var(--color-surface-alt); }
.recent-main { flex: 1; min-width: 0; }
.recent-title { font: var(--text-h4); color: var(--color-text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.recent-meta  { display: flex; align-items: center; gap: var(--space-2); margin-top: 2px; }

.quick-actions { padding: var(--space-2) 0; }
.quick-action-btn {
  display: flex; align-items: center; gap: var(--space-3);
  width: 100%; padding: var(--space-3) var(--space-5);
  background: none; border: none; border-bottom: 1px solid var(--color-border);
  cursor: pointer; text-align: left;
  transition: background var(--transition-fast);
}
.quick-action-btn:last-child { border-bottom: none; }
.quick-action-btn:hover { background: var(--color-surface-alt); }
.qa-icon  { font-size: 18px; width: 28px; text-align: center; flex-shrink: 0; }
.qa-title { font: var(--text-h4); color: var(--color-text-primary); }
.qa-sub   { font: var(--text-sm); color: var(--color-text-muted); }

.empty-msg {
  padding: var(--space-6);
  display: flex; align-items: center; justify-content: center; flex-direction: column;
  gap: var(--space-2); color: var(--color-text-muted); text-align: center;
}
.empty-msg span { font-size: 24px; }
.empty-msg p    { font: var(--text-body); }

.btn-link { background: none; border: none; cursor: pointer; color: var(--color-accent); font: inherit; font-weight: 600; padding: 0; }
.btn-link:hover { color: var(--color-accent-hover); }
.side-col { display: flex; flex-direction: column; }
</style>
