<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const store  = useNckhStore()
const router = useRouter()
const { inbox, loading } = storeToRefs(store)

const TABS = [
  { key: 'CHO_PNCKH_XEM_XET',     label: 'Chờ tiếp nhận',     icon: '📬' },
  { key: 'DANG_XEM_XET_BOI_PNCKH', label: 'Đang xem xét',      icon: '🔍' },
  { key: 'CHO_BO_SUNG_HO_SO',      label: 'Chờ bổ sung',        icon: '📎' },
]
const activeTab = ref('CHO_PNCKH_XEM_XET')

onMounted(() => loadInbox())

async function loadInbox() {
  await store.layTatCa()
}

const filteredInbox = computed(() =>
  inbox.value.filter(d => d.trangThai === activeTab.value)
)

const countByTab = computed(() => {
  const counts = {}
  for (const tab of TABS) {
    counts[tab.key] = inbox.value.filter(d => d.trangThai === tab.key).length
  }
  return counts
})

function fmt(iso) { return iso ? new Date(iso).toLocaleDateString('vi-VN') : '—' }
function fmtM(n)  { return n ? n.toLocaleString('vi-VN') + ' đ' : '—' }
</script>

<template>
  <div class="page-container">
    <!-- Header -->
    <div class="page-header mb-4">
      <div class="page-header-left">
        <h1 class="page-title">Hộp thư đến</h1>
        <p class="page-subtitle">Hồ sơ đề tài cần xem xét và xử lý</p>
      </div>
      <button class="btn btn-secondary btn-sm" @click="loadInbox">⟳ Làm mới</button>
    </div>

    <!-- Tabs -->
    <div class="tab-bar mb-4">
      <button
        v-for="tab in TABS" :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span>{{ tab.icon }} {{ tab.label }}</span>
        <span v-if="countByTab[tab.key]" class="tab-count">{{ countByTab[tab.key] }}</span>
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 4" :key="i" class="skeleton" style="height:76px;border-radius:10px"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="!filteredInbox.length">
      <EmptyState
        icon="pi pi-inbox"
        title="Không có hồ sơ nào"
        :message="`Không có hồ sơ nào ở trạng thái '${TABS.find(t => t.key === activeTab)?.label}'.`"
      />
    </div>

    <!-- List -->
    <div v-else class="inbox-list">
      <div
        v-for="dt in filteredInbox" :key="dt.id"
        class="inbox-card"
        @click="router.push(`/nckh/de-tai/${dt.id}`)"
      >
        <div class="inbox-card-left">
          <div class="inbox-unread-dot"></div>
          <div class="inbox-body">
            <div class="inbox-name">{{ dt.tenDeTai }}</div>
            <div class="inbox-meta">
              <span class="mono text-sm">{{ dt.maSo }}</span>
              <span class="meta-sep">·</span>
              <span class="text-muted text-sm">{{ dt.chuNhiem }}</span>
              <span class="meta-sep">·</span>
              <span class="text-muted text-sm">{{ dt.kyNckh }}</span>
            </div>
            <!-- Yêu cầu bổ sung hint -->
            <div v-if="dt.yeuCauBoSung" class="yeu-cau-hint">
              📎 {{ typeof dt.yeuCauBoSung === 'string' ? dt.yeuCauBoSung : dt.yeuCauBoSung?.noiDung }}
            </div>
          </div>
        </div>
        <div class="inbox-card-right">
          <div class="inbox-amount" v-if="dt.kinhPhi">{{ fmtM(dt.kinhPhi) }}</div>
          <StatusBadge :status="dt.trangThai" />
          <div class="inbox-date">{{ fmt(dt.updatedAt) }}</div>
          <i class="pi pi-chevron-right text-muted"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tab-bar {
  display: flex; gap: var(--space-1);
  background: var(--color-surface-alt);
  border-radius: var(--radius-lg);
  padding: var(--space-1);
  border: 1px solid var(--color-border);
}
.tab-btn {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border: none; background: none; border-radius: var(--radius-md);
  font: var(--text-sm); color: var(--color-text-muted);
  cursor: pointer; transition: all var(--transition-fast);
}
.tab-btn.active {
  background: var(--color-surface);
  color: var(--color-accent);
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}
.tab-count {
  background: var(--color-accent);
  color: #fff;
  font-size: 11px; font-weight: 700;
  padding: 1px 6px; border-radius: 999px;
  min-width: 18px; text-align: center;
}

.skeleton-list { display: flex; flex-direction: column; gap: var(--space-2); }
.inbox-list    { display: flex; flex-direction: column; gap: var(--space-2); }

.inbox-card {
  display: flex; align-items: center; gap: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  cursor: pointer;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.inbox-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-sm);
}

.inbox-card-left {
  display: flex; align-items: flex-start; gap: var(--space-3); flex: 1; min-width: 0;
}
.inbox-unread-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--color-accent); flex-shrink: 0; margin-top: 6px;
}
.inbox-body { flex: 1; min-width: 0; }
.inbox-name {
  font: var(--text-h4); color: var(--color-text-primary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  margin-bottom: 4px;
}
.inbox-meta { display: flex; gap: var(--space-2); align-items: center; flex-wrap: wrap; }
.meta-sep { color: var(--color-text-muted); }
.yeu-cau-hint {
  margin-top: var(--space-2);
  font: var(--text-sm); color: var(--color-warning);
  background: color-mix(in srgb, var(--color-warning) 8%, transparent);
  border-radius: var(--radius-sm); padding: 2px 8px;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  max-width: 480px;
}

.inbox-card-right {
  display: flex; align-items: center; gap: var(--space-3); flex-shrink: 0;
}
.inbox-amount { font: var(--text-sm); color: var(--color-text-muted); white-space: nowrap; }
.inbox-date   { font: var(--text-sm); color: var(--color-text-muted); white-space: nowrap; }
</style>
