<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePBStore } from '@/stores/pb.store'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import { Search } from '@lucide/vue'

const store  = usePBStore()
const router = useRouter()
const { danhSachAssigned, loading } = storeToRefs(store)
onMounted(() => store.layDanhSachAssigned())

function fmt(iso) { return new Date(iso).toLocaleDateString('vi-VN') }
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Đề tài được phân công</h1>
        <p class="page-subtitle">Danh sách đề tài cần phản biện</p>
      </div>
    </div>

    <div v-if="loading" class="skeleton" style="height:150px"></div>

    <div v-else-if="!danhSachAssigned.length">
      <EmptyState :icon="Search" title="Chưa có đề tài nào" message="Bạn chưa được phân công phản biện đề tài nào." />
    </div>

    <div v-else class="assigned-list">
      <div
        v-for="dt in danhSachAssigned" :key="dt.id"
        class="assigned-card"
        @click="router.push(`/pb/de-tai/${dt.id}`)"
      >
        <div class="assigned-card-body">
          <div class="assigned-name">{{ dt.tenDeTai }}</div>
          <div class="assigned-meta">
            <span class="mono text-sm">{{ dt.maSo }}</span> ·
            <span class="text-muted text-sm">{{ dt.giangVien?.hoTen ?? dt.chuNhiem }}</span> ·
            <span class="text-muted text-sm">{{ dt.kyNckh }}</span>
          </div>
        </div>
        <div class="assigned-right">
          <StatusBadge :status="dt.trangThai" />
          <span class="text-muted text-sm">{{ fmt(dt.updatedAt) }}</span>
          <button class="btn btn-primary btn-sm">Phản biện →</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.assigned-list { display: flex; flex-direction: column; gap: var(--space-3); }
.assigned-card {
  display: flex; align-items: center; gap: var(--space-4);
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-4) var(--space-5);
  cursor: pointer;
  transition: box-shadow var(--transition-fast), border-color var(--transition-fast);
}
.assigned-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-sm);
}
.assigned-card-body { flex: 1; min-width: 0; }
.assigned-name { font: var(--text-h4); color: var(--color-text-primary); }
.assigned-meta { font: var(--text-sm); color: var(--color-text-muted); margin-top: 4px; }
.assigned-right {
  display: flex; align-items: center; gap: var(--space-3); flex-shrink: 0;
}
</style>
