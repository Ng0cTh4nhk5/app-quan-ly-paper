<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const store  = useDetaiStore()
const router = useRouter()
const { danhSach, loading, error } = storeToRefs(store)

const filterStatus = ref('')
const viewMode = ref('table') // 'table' | 'card'

const STATUS_OPTS = [
  { value: '',                    label: 'Tất cả trạng thái' },
  { value: 'DRAFT',               label: 'Bản nháp' },
  { value: 'CHO_PNCKH_XEM_XET',  label: 'Chờ P.NCKH' },
  { value: 'CHO_BO_SUNG_HO_SO',  label: 'Chờ bổ sung' },
  { value: 'DANG_PHAN_BIEN',      label: 'Đang phản biện' },
  { value: 'DANG_THUC_HIEN',      label: 'Đang thực hiện' },
  { value: 'DA_HOAN_THANH',       label: 'Hoàn thành' },
]

const filtered = computed(() =>
  filterStatus.value
    ? danhSach.value.filter(d => d.trangThai === filterStatus.value)
    : danhSach.value
)

// Progress mapping for card view
function getProgress(status) {
  const map = {
    DRAFT: 5,
    CHO_PNCKH_XEM_XET: 20,
    CHO_BO_SUNG_HO_SO: 15,
    DANG_PHAN_BIEN: 50,
    DANG_THUC_HIEN: 70,
    CHO_NGHIEM_THU: 85,
    DA_HOAN_THANH: 100,
  }
  return map[status] ?? 10
}

function getProgressClass(status) {
  if (status === 'CHO_BO_SUNG_HO_SO') return 'danger'
  if (['DRAFT', 'CHO_PNCKH_XEM_XET'].includes(status)) return 'warning'
  return ''
}

onMounted(() => store.layDanhSach())

function fmt(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('vi-VN')
}

function fmtMoney(n) {
  return n?.toLocaleString('vi-VN') + ' đ'
}
</script>

<template>
  <div>
    <!-- Header -->
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Đề tài của tôi</h1>
        <p class="page-subtitle">Quản lý toàn bộ đề tài nghiên cứu khoa học</p>
      </div>
      <button class="btn btn-primary" @click="router.push('/gv/de-tai/tao-moi')">
        ✚ Đăng ký đề tài
      </button>
    </div>

    <!-- Filter + View toggle bar -->
    <div class="toolbar">
      <div class="toolbar-left">
        <select class="form-select filter-select" v-model="filterStatus">
          <option v-for="o in STATUS_OPTS" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
        <span class="filter-count">{{ filtered.length }} đề tài</span>
      </div>
      <div class="view-toggle">
        <button
          class="view-btn" :class="{ active: viewMode === 'table' }"
          @click="viewMode = 'table'" title="Bảng"
        >☰</button>
        <button
          class="view-btn" :class="{ active: viewMode === 'card' }"
          @click="viewMode = 'card'" title="Thẻ"
        >⊞</button>
      </div>
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="skeleton-wrap">
      <div v-for="i in 4" :key="i" class="skeleton" style="height: 60px; margin-bottom: 2px"></div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- TABLE VIEW -->
    <div v-else-if="filtered.length && viewMode === 'table'" class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width:130px">Mã số</th>
            <th>Tên đề tài</th>
            <th style="width:150px">Trạng thái</th>
            <th style="width:120px">Kỳ NCKH</th>
            <th style="width:110px">Kinh phí</th>
            <th style="width:100px">Cập nhật</th>
            <th style="width:64px"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dt in filtered" :key="dt.id" class="table-row" @click="router.push(`/gv/de-tai/${dt.id}`)">
            <td><span class="mono">{{ dt.maSo || '—' }}</span></td>
            <td>
              <div class="dt-name">{{ dt.tenDeTai }}</div>
              <span v-if="dt.linhVuc" class="dt-linhvuc">{{ dt.linhVuc }}</span>
            </td>
            <td><StatusBadge :status="dt.trangThai" /></td>
            <td class="text-muted text-sm">{{ dt.kyNckh || '—' }}</td>
            <td class="mono text-sm">{{ dt.kinhPhi ? fmtMoney(dt.kinhPhi) : '—' }}</td>
            <td class="text-muted text-sm">{{ fmt(dt.updatedAt) }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click.stop="router.push(`/gv/de-tai/${dt.id}`)">Xem →</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- CARD VIEW -->
    <div v-else-if="filtered.length && viewMode === 'card'" class="projects-grid">
      <div
        v-for="dt in filtered" :key="dt.id"
        class="project-card"
        @click="router.push(`/gv/de-tai/${dt.id}`)"
      >
        <div class="project-card-header">
          <StatusBadge :status="dt.trangThai" />
          <span v-if="dt.maSo" class="mono text-sm" style="color: var(--color-text-muted)">{{ dt.maSo }}</span>
        </div>
        <div class="project-card-title">{{ dt.tenDeTai }}</div>
        <div v-if="dt.tomTat" class="project-card-desc">{{ dt.tomTat }}</div>
        <div class="project-card-meta">
          <span v-if="dt.kyNckh">📅 {{ dt.kyNckh }}</span>
          <span v-if="dt.kinhPhi">💰 {{ fmtMoney(dt.kinhPhi) }}</span>
        </div>
        <!-- Progress -->
        <div class="card-progress">
          <div class="progress-label">
            <span>Tiến độ</span>
            <span>{{ getProgress(dt.trangThai) }}%</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :class="getProgressClass(dt.trangThai)"
              :style="{ width: getProgress(dt.trangThai) + '%' }"
            ></div>
          </div>
        </div>
        <div class="project-card-footer">
          <span class="text-sm text-muted">Cập nhật {{ fmt(dt.updatedAt) }}</span>
          <button class="btn btn-ghost btn-sm" @click.stop="router.push(`/gv/de-tai/${dt.id}`)">
            Xem chi tiết →
          </button>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="!loading" class="table-wrapper">
      <EmptyState
        icon="📝"
        title="Chưa có đề tài nào"
        message="Bắt đầu bằng cách đăng ký đề tài nghiên cứu đầu tiên của bạn."
        action="Đăng ký đề tài"
        @action="router.push('/gv/de-tai/tao-moi')"
      />
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.toolbar-left { display: flex; align-items: center; gap: var(--space-3); }
.filter-select { width: 220px; }
.filter-count  { font: var(--text-sm); color: var(--color-text-muted); }

.view-toggle { display: flex; border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; }
.view-btn {
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface);
  border: none; cursor: pointer;
  font-size: 16px; color: var(--color-text-muted);
  transition: background var(--transition-fast), color var(--transition-fast);
}
.view-btn:hover { background: var(--color-surface-alt); }
.view-btn.active { background: var(--color-primary); color: #fff; }

.skeleton-wrap { border-radius: var(--radius-lg); overflow: hidden; }

/* Table enhancements */
.table-row { cursor: pointer; }
.dt-name { font: var(--text-h4); color: var(--color-text-primary); }
.dt-linhvuc {
  display: inline-block; margin-top: 2px;
  font: var(--text-caption); color: var(--color-text-muted);
  background: var(--color-surface-alt);
  border: 1px solid var(--color-border);
  border-radius: 4px; padding: 0 6px;
}

/* Card progress */
.card-progress { margin-top: var(--space-3); }
.progress-label {
  display: flex; justify-content: space-between;
  font: var(--text-caption); color: var(--color-text-muted);
  margin-bottom: var(--space-1);
}
</style>
