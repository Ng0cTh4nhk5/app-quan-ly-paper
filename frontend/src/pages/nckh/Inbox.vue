<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const store  = useNckhStore()
const router = useRouter()
const { inbox, loading } = storeToRefs(store)
onMounted(() => store.layInbox('CHO_PNCKH_XEM_XET'))

function fmt(iso) { return new Date(iso).toLocaleDateString('vi-VN') }
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Hộp thư đến</h1>
        <p class="page-subtitle">Danh sách hồ sơ đề tài cần xem xét</p>
      </div>
      <span class="inbox-count-chip">{{ inbox.length }} hồ sơ mới</span>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton" style="height:72px;margin-bottom:2px"></div>
    </div>

    <div v-else-if="!inbox.length">
      <EmptyState icon="pi pi-inbox" title="Hộp thư trống" message="Không có hồ sơ nào cần xem xét." />
    </div>

    <div v-else class="inbox-list">
      <div
        v-for="dt in inbox" :key="dt.id"
        class="inbox-item"
        @click="router.push(`/nckh/de-tai/${dt.id}`)"
      >
        <div class="inbox-item-dot"></div>
        <div class="inbox-item-body">
          <div class="inbox-item-name">{{ dt.tenDeTai }}</div>
          <div class="inbox-item-meta">
            <span class="mono text-sm">{{ dt.maSo }}</span>
            <span class="text-muted text-sm">·</span>
            <span class="text-muted text-sm">{{ dt.giangVien?.hoTen }}</span>
            <span class="text-muted text-sm">·</span>
            <span class="text-muted text-sm">{{ fmt(dt.updatedAt) }}</span>
          </div>
        </div>
        <StatusBadge :status="dt.trangThai" />
        <span class="inbox-item-arrow"><i class="pi pi-chevron-right"></i></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.inbox-count-chip {
  background: var(--color-amber);
  color: #fff;
  font: var(--text-sm);
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 999px;
}
.inbox-list { display: flex; flex-direction: column; gap: 2px; }
.inbox-item {
  display: flex; align-items: center; gap: var(--space-3);
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-4) var(--space-5);
  cursor: pointer;
  transition: background var(--transition-fast), box-shadow var(--transition-fast);
}
.inbox-item:hover {
  background: var(--color-surface-alt);
  box-shadow: var(--shadow-sm);
}
.inbox-item-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--color-accent); flex-shrink: 0;
}
.inbox-item-body { flex: 1; min-width: 0; }
.inbox-item-name { font: var(--text-h4); color: var(--color-text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.inbox-item-meta { display: flex; gap: var(--space-2); align-items: center; margin-top: 2px; }
.inbox-item-arrow { color: var(--color-text-muted); font-size: 16px; }
</style>
