<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePBStore } from '@/stores/pb.store'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import { Search } from '@lucide/vue'

const store = usePBStore()
const router = useRouter()
const toast = useToast()
const { danhSachAssigned, loading, error } = storeToRefs(store)

onMounted(async () => {
  try {
    await store.layDanhSachAssigned()
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Không thể tải danh sách', detail: e.response?.data?.message ?? error.value })
  }
})

function navigate(id) {
  router.push(`/pb/de-tai/${id}`)
}

function ownerName(dt) {
  return dt.giangVien?.hoTen ?? dt.chuNhiem ?? '---'
}

function fmt(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '---'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Đề tài được phân công</h1>
        <p class="page-subtitle">Danh sách đề tài cần phản biện và chưa nộp kết quả</p>
      </div>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton" style="height:84px;border-radius:8px"></div>
    </div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else-if="!danhSachAssigned.length">
      <EmptyState :icon="Search" title="Chưa có đề tài nào" message="Bạn không có đề tài phản biện đang chờ nộp kết quả." />
    </div>

    <div v-else class="assigned-list">
      <div
        v-for="dt in danhSachAssigned"
        :key="dt.id"
        class="assigned-card"
        @click="navigate(dt.id)"
      >
        <div class="assigned-card-body">
          <div class="assigned-name">{{ dt.tenDeTai }}</div>
          <div class="assigned-meta">
            <span class="mono text-sm">{{ dt.maSo }}</span>
            <span>·</span>
            <span>{{ ownerName(dt) }}</span>
            <span>·</span>
            <span>{{ dt.kyNckh }}</span>
          </div>
          <div v-if="dt.deadlineNopPhanBien" class="assigned-deadline">
            Hạn nộp: {{ fmt(dt.deadlineNopPhanBien) }}
          </div>
        </div>
        <div class="assigned-right">
          <StatusBadge :status="dt.trangThai" />
          <span class="text-muted text-sm">{{ fmt(dt.updatedAt) }}</span>
          <button type="button" class="btn btn-primary btn-sm" @click.stop="navigate(dt.id)">Phản biện</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-list,
.assigned-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.assigned-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  cursor: pointer;
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast);
}

.assigned-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-sm);
}

.assigned-card-body {
  flex: 1;
  min-width: 0;
}

.assigned-name {
  font: var(--text-h4);
  color: var(--color-text-primary);
  overflow-wrap: anywhere;
}

.assigned-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  font: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: 4px;
}

.assigned-deadline {
  margin-top: var(--space-2);
  font: var(--text-sm);
  color: var(--color-warning-text);
}

.assigned-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

@media (max-width: 700px) {
  .assigned-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .assigned-right {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }
}
</style>
