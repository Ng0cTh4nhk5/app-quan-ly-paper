<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useDetaiStore } from '@/stores/detai.store'
import { useAuthStore } from '@/stores/auth.store'
import StatusBadge from '@/components/StatusBadge.vue'
import { List, Clock, Settings, Search, Sparkles, Plus, TriangleAlert, FileEdit, Paperclip } from '@lucide/vue'

const store  = useDetaiStore()
const auth   = useAuthStore()
const router = useRouter()

onMounted(() => store.layDanhSach())

const list        = computed(() => store.danhSach ?? [])
const pending     = computed(() => list.value.filter(d => d.trangThai === 'CHO_PNCKH_XEM_XET').length)
const active      = computed(() => list.value.filter(d => d.trangThai === 'DANG_THUC_HIEN').length)
const inReview    = computed(() => list.value.filter(d => d.trangThai === 'DANG_PHAN_BIEN').length)
const needsAction = computed(() => list.value.filter(d => d.trangThai === 'CHO_BO_SUNG_HO_SO'))
const recent      = computed(() => [...list.value]
  .sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
  .slice(0, 5)
)

const stats = computed(() => [
  { icon: List, label: 'Tổng đề tài',      value: list.value.length, iconClass: 'stat-icon-teal',  delta: '+2 kỳ này' },
  { icon: Clock, label: 'Chờ phê duyệt',    value: pending.value,     iconClass: 'stat-icon-amber'  },
  { icon: Settings, label: 'Đang thực hiện',   value: active.value,      iconClass: 'stat-icon-blue'   },
  { icon: Search, label: 'Đang phản biện',   value: inReview.value,    iconClass: 'stat-icon-green'  },
])

const processSteps = [
  { num: '1', title: 'Đăng ký đề tài', desc: 'Điền thông tin và nộp hồ sơ', active: true },
  { num: '2', title: 'P.NCKH xem xét', desc: 'Phòng NCKH tiếp nhận, thẩm định' },
  { num: '3', title: 'Phản biện',       desc: 'Tổ phản biện đánh giá chuyên môn' },
  { num: '4', title: 'Ký hợp đồng',    desc: 'Ký kết và triển khai đề tài' },
  { num: '5', title: 'Nghiệm thu',      desc: 'Báo cáo kết quả, nghiệm thu' },
]

function fmt(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
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
        <p class="greeting-sub">Đây là tổng quan hoạt động nghiên cứu của bạn.</p>
      </div>
      <button class="btn btn-primary" @click="router.push('/gv/de-tai/tao-moi')">
        <Plus :size="16" style="margin-right: 4px;" /> Đăng ký đề tài
      </button>
    </div>

    <!-- Warning banner -->
    <div v-if="needsAction.length > 0" class="warning-banner">
      <TriangleAlert :size="20" style="margin-right: 8px;" />
      <div>
        <strong>Cần bổ sung hồ sơ:</strong> Bạn có {{ needsAction.length }} đề tài đang chờ bổ sung từ P.NCKH.
        <button class="btn-link" @click="router.push('/gv/de-tai')">Xem ngay →</button>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-icon" :class="s.iconClass"><component :is="s.icon" :size="24" /></div>
        <div>
          <div class="stat-value">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
          <div v-if="s.delta" class="stat-delta">{{ s.delta }}</div>
        </div>
      </div>
    </div>

    <!-- Main + Sidebar -->
    <div class="dash-grid">
      <!-- Recent projects -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Đề tài gần đây</h3>
          <button class="btn btn-ghost btn-sm" @click="router.push('/gv/de-tai')">Xem tất cả →</button>
        </div>

        <div v-if="store.loading" class="card-body">
          <div v-for="i in 3" :key="i" class="skeleton" style="height: 52px; margin-bottom: 8px"></div>
        </div>

        <div v-else-if="recent.length === 0" class="card-body empty-dash">
          <FileEdit :size="32" style="color: var(--color-text-muted)" />
          <p>Chưa có đề tài nào.
            <button class="btn-link" @click="router.push('/gv/de-tai/tao-moi')">Tạo ngay</button>
          </p>
        </div>

        <div v-else class="recent-list">
          <div
            v-for="dt in recent" :key="dt.id"
            class="recent-item"
            @click="router.push(`/gv/de-tai/${dt.id}`)"
          >
            <div class="recent-main">
              <div class="recent-title">{{ dt.tenDeTai }}</div>
              <div class="recent-meta">
                <span class="mono text-sm">{{ dt.maSo || '—' }}</span>
                <span>·</span>
                <span class="text-sm text-muted">{{ fmt(dt.updatedAt) }}</span>
              </div>
            </div>
            <StatusBadge :status="dt.trangThai" />
          </div>
        </div>
      </div>

      <!-- Right sidebar -->
      <div class="side-col">
        <!-- Quick actions -->
        <div class="card mb-4">
          <div class="card-header">
            <h3 class="card-title">Thao tác nhanh</h3>
          </div>
          <div class="quick-actions">
            <button class="quick-action-btn" @click="router.push('/gv/de-tai/tao-moi')">
              <span class="qa-icon"><Plus :size="18" /></span>
              <div>
                <div class="qa-title">Đăng ký đề tài</div>
                <div class="qa-sub">Nộp đề tài nghiên cứu mới</div>
              </div>
            </button>
            <button class="quick-action-btn" @click="router.push('/gv/de-tai')">
              <span class="qa-icon"><List :size="18" /></span>
              <div>
                <div class="qa-title">Xem danh sách</div>
                <div class="qa-sub">Quản lý tất cả đề tài</div>
              </div>
            </button>
            <button
              v-if="needsAction.length > 0"
              class="quick-action-btn warn-action"
              @click="router.push('/gv/de-tai')"
            >
              <span class="qa-icon"><Paperclip :size="18" /></span>
              <div>
                <div class="qa-title warn-text">Bổ sung hồ sơ</div>
                <div class="qa-sub">{{ needsAction.length }} đề tài cần bổ sung</div>
              </div>
            </button>
          </div>
        </div>

        <!-- Process guide -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Quy trình đề tài</h3>
          </div>
          <div class="card-body" style="padding-top: var(--space-4)">
            <div class="timeline">
              <div v-for="(step, idx) in processSteps" :key="step.title" class="timeline-item">
                <div class="timeline-dot" :class="{ active: step.active }">{{ step.num }}</div>
                <div class="timeline-body">
                  <div class="timeline-title">{{ step.title }}</div>
                  <div class="timeline-desc">{{ step.desc }}</div>
                </div>
              </div>
            </div>
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
.warn-text { color: var(--color-warning-text) !important; }

.empty-dash { display: flex; flex-direction: column; align-items: center; gap: var(--space-2); color: var(--color-text-muted); text-align: center; }
.empty-dash p { font: var(--text-body); }
.btn-link { background: none; border: none; cursor: pointer; color: var(--color-accent); font: inherit; font-weight: 600; padding: 0; }
.btn-link:hover { color: var(--color-accent-hover); }
.side-col { display: flex; flex-direction: column; }
</style>
