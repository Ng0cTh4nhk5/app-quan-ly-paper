<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import { useToast } from '@/composables/useToast'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import { normalizeContractStatus } from '@/mock/sopDGuards'
import { Settings } from '@lucide/vue'

const store = useNckhStore()
const router = useRouter()
const toast = useToast()
const { inbox, loading, error } = storeToRefs(store)

const DANG_XU_LY = [
  'DANG_XEM_XET_BOI_PNCKH',
  'DANG_PHAN_BIEN',
  'CHO_CHINH_SUA_THUYET_MINH',
  'DANG_LAP_HOP_DONG',
]

onMounted(async () => {
  try {
    await store.layTatCa()
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Không thể tải danh sách', detail: e.response?.data?.message ?? error.value })
  }
})

const filtered = computed(() => inbox.value.filter(d => DANG_XU_LY.includes(d.trangThai)))

function ownerName(dt) {
  return dt.giangVien?.hoTen ?? dt.chuNhiem ?? '---'
}

function contractLabel(dt) {
  if (dt.trangThai !== 'DANG_LAP_HOP_DONG') return '---'
  const labels = {
    CHUA_SOAN: 'Chưa soạn',
    CHO_GV_XEM: 'Chờ GV phản hồi',
    CHO_PHAN_HOI: 'Chờ GV phản hồi',
    CAN_SUA: 'GV yêu cầu sửa',
    YEU_CAU_SUA: 'GV yêu cầu sửa',
    CHO_KY: 'Chờ ký',
    DA_KY: 'Đã ký',
  }
  return labels[normalizeContractStatus(dt)] ?? '---'
}

function fmt(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '---'
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Đang xử lý</h1>
        <p class="page-subtitle">Hồ sơ đề tài đang trong quy trình sơ thẩm, phản biện và hợp đồng</p>
      </div>
    </div>

    <div v-if="loading" class="skeleton" style="height:200px"></div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else-if="!filtered.length">
      <EmptyState :icon="Settings" title="Không có hồ sơ đang xử lý" />
    </div>

    <div v-else class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th>Mã số</th>
            <th>Tên đề tài</th>
            <th>Chủ nhiệm</th>
            <th>Trạng thái</th>
            <th>Hợp đồng</th>
            <th>Cập nhật</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dt in filtered" :key="dt.id">
            <td class="mono text-sm">{{ dt.maSo }}</td>
            <td class="topic-name">{{ dt.tenDeTai }}</td>
            <td class="text-muted">{{ ownerName(dt) }}</td>
            <td><StatusBadge :status="dt.trangThai" /></td>
            <td class="text-muted text-sm">{{ contractLabel(dt) }}</td>
            <td class="text-muted text-sm">{{ fmt(dt.updatedAt) }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="router.push(`/nckh/de-tai/${dt.id}`)">Xem</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.topic-name {
  min-width: 220px;
  overflow-wrap: anywhere;
}
</style>
