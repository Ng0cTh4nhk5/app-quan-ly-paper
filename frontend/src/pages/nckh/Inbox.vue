<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import { normalizeContractStatus } from '@/utils/sopDGuards'
import { ChevronRight, ClipboardList, FileText, Inbox, Paperclip, Search } from '@lucide/vue'

const store = useNckhStore()
const router = useRouter()
const toast = useToast()
const { inbox, loading, error } = storeToRefs(store)

const TABS = [
  { key: 'CHO_PNCKH_XEM_XET', label: 'Chờ tiếp nhận', icon: Inbox },
  { key: 'DANG_XEM_XET_BOI_PNCKH', label: 'Đang xem xét', icon: Search },
  { key: 'CHO_BO_SUNG_HO_SO', label: 'Chờ bổ sung', icon: Paperclip },
  { key: 'DANG_PHAN_BIEN', label: 'Đang phản biện', icon: ClipboardList },
  { key: 'DANG_LAP_HOP_DONG', label: 'Đang lập HĐ', icon: FileText },
]

const activeTab = ref('CHO_PNCKH_XEM_XET')

onMounted(() => loadInbox())

async function loadInbox() {
  try {
    await store.layTatCa()
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Không thể tải hộp thư', detail: e.response?.data?.message ?? error.value })
  }
}

const filteredInbox = computed(() => inbox.value.filter(d => d.trangThai === activeTab.value))
const activeLabel = computed(() => TABS.find(t => t.key === activeTab.value)?.label ?? '')
const countByTab = computed(() => Object.fromEntries(
  TABS.map(tab => [tab.key, inbox.value.filter(d => d.trangThai === tab.key).length]),
))

function ownerName(dt) {
  return dt.giangVien?.hoTen ?? dt.chuNhiem ?? '---'
}

function contractHint(dt) {
  if (dt.trangThai !== 'DANG_LAP_HOP_DONG') return ''
  const labels = {
    CHUA_SOAN: 'Cần soạn hợp đồng để gửi GV xem',
    CHO_GV_XEM: 'Đã gửi hợp đồng, chờ GV phản hồi',
    CHO_PHAN_HOI: 'Đã gửi hợp đồng, chờ GV phản hồi',
    CAN_SUA: 'GV yêu cầu chỉnh sửa hợp đồng',
    YEU_CAU_SUA: 'GV yêu cầu chỉnh sửa hợp đồng',
    CHO_KY: 'GV đã đồng ý, chờ P.NCKH xác nhận ký',
    DA_KY: 'Hợp đồng đã ký',
  }
  return labels[normalizeContractStatus(dt)] ?? ''
}

function fmt(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '---'
}

function fmtM(n) {
  return n ? `${Number(n).toLocaleString('vi-VN')} đ` : '---'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header mb-4">
      <div class="page-header-left">
        <h1 class="page-title">Hộp thư đến</h1>
        <p class="page-subtitle">Hồ sơ đề tài cần xem xét và xử lý trong SOP D</p>
      </div>
      <button class="btn btn-secondary btn-sm" :disabled="loading" @click="loadInbox">
        {{ loading ? 'Đang tải...' : 'Làm mới' }}
      </button>
    </div>

    <div class="tab-bar mb-4">
      <button
        v-for="tab in TABS"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="tab-label">
          <component :is="tab.icon" :size="16" />
          <span>{{ tab.label }}</span>
        </span>
        <span v-if="countByTab[tab.key]" class="tab-count">{{ countByTab[tab.key] }}</span>
      </button>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 4" :key="i" class="skeleton" style="height:76px;border-radius:10px"></div>
    </div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else-if="!filteredInbox.length">
      <EmptyState
        :icon="Inbox"
        title="Không có hồ sơ nào"
        :message="`Không có hồ sơ nào ở tab ${activeLabel}.`"
      />
    </div>

    <div v-else class="inbox-list">
      <div
        v-for="dt in filteredInbox"
        :key="dt.id"
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
              <span class="text-muted text-sm">{{ ownerName(dt) }}</span>
              <span class="meta-sep">·</span>
              <span class="text-muted text-sm">{{ dt.kyNckh }}</span>
            </div>
            <div v-if="dt.yeuCauBoSung" class="yeu-cau-hint">
              <Paperclip :size="14" />
              <span>{{ typeof dt.yeuCauBoSung === 'string' ? dt.yeuCauBoSung : dt.yeuCauBoSung?.noiDung }}</span>
            </div>
            <div v-if="contractHint(dt)" class="contract-hint">
              <FileText :size="14" />
              <span>{{ contractHint(dt) }}</span>
            </div>
          </div>
        </div>
        <div class="inbox-card-right">
          <div class="inbox-amount" v-if="dt.kinhPhi">{{ fmtM(dt.kinhPhi) }}</div>
          <StatusBadge :status="dt.trangThai" />
          <div class="inbox-date">{{ fmt(dt.updatedAt) }}</div>
          <ChevronRight :size="16" class="text-muted" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tab-bar {
  display: flex;
  gap: var(--space-1);
  background: var(--color-surface-alt);
  border-radius: var(--radius-lg);
  padding: var(--space-1);
  border: 1px solid var(--color-border);
  overflow-x: auto;
}

.tab-btn {
  flex: 1;
  min-width: 142px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: none;
  background: none;
  border-radius: var(--radius-md);
  font: var(--text-sm);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
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
  font-size: 11px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 999px;
  min-width: 18px;
  text-align: center;
}

.skeleton-list,
.inbox-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.inbox-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
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
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.inbox-unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-accent);
  flex-shrink: 0;
  margin-top: 6px;
}

.inbox-body {
  flex: 1;
  min-width: 0;
}

.inbox-name {
  font: var(--text-h4);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.inbox-meta {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  flex-wrap: wrap;
}

.meta-sep {
  color: var(--color-text-muted);
}

.yeu-cau-hint {
  margin-top: var(--space-2);
  font: var(--text-sm);
  color: var(--color-warning);
  background: color-mix(in srgb, var(--color-warning) 8%, transparent);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  max-width: min(480px, 100%);
}

.yeu-cau-hint span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contract-hint {
  margin-top: var(--space-2);
  font: var(--text-sm);
  color: var(--color-accent);
  background: color-mix(in srgb, var(--color-accent) 8%, transparent);
  border-radius: var(--radius-sm);
  padding: 2px 8px;
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  max-width: min(480px, 100%);
}

.contract-hint span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inbox-card-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.inbox-amount,
.inbox-date {
  font: var(--text-sm);
  color: var(--color-text-muted);
  white-space: nowrap;
}

@media (max-width: 700px) {
  .inbox-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .inbox-card-right {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }
}
</style>
