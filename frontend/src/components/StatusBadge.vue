<script setup>
import { computed } from 'vue'
const props = defineProps({ status: { type: String, required: true } })

const CONFIG = {
  DRAFT:                      { label: 'Bản nháp',        cls: 'badge-secondary', dot: '#94A3B8' },
  CHO_PNCKH_XEM_XET:         { label: 'Chờ P.NCKH',      cls: 'badge-info',      dot: '#0284C7' },
  DANG_XEM_XET_BOI_PNCKH:   { label: 'Đang xem xét',    cls: 'badge-info',      dot: '#0284C7' },
  CHO_BO_SUNG_HO_SO:         { label: 'Chờ bổ sung',     cls: 'badge-warning',   dot: '#D97706' },
  DANG_PHAN_BIEN:             { label: 'Đang phản biện',  cls: 'badge-info',      dot: '#0D9488' },
  CHO_CHINH_SUA_THUYET_MINH: { label: 'Chờ sửa TM',      cls: 'badge-warning',   dot: '#D97706' },
  DANG_LAP_HOP_DONG:         { label: 'Đang lập HĐ',     cls: 'badge-info',      dot: '#06B6D4' },
  DANG_THUC_HIEN:             { label: 'Đang thực hiện',  cls: 'badge-success',   dot: '#10B981' },
  BI_TREO:                    { label: 'Bị treo',         cls: 'badge-danger',    dot: '#EF4444' },
  BI_TU_CHOI:                 { label: 'Bị từ chối',      cls: 'badge-danger',    dot: '#991B1B' },
  DA_RUT:                     { label: 'Đã rút',          cls: 'badge-secondary', dot: '#94A3B8' },
}

const cfg = computed(() => CONFIG[props.status] ?? { label: props.status, cls: 'badge-secondary', dot: '#94A3B8' })
</script>

<template>
  <span :class="['badge', cfg.cls]">
    <span class="badge-dot" :style="{ background: cfg.dot }"></span>
    {{ cfg.label }}
  </span>
</template>

<style scoped>
.badge-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
