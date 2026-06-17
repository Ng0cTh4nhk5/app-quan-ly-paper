<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'

const store  = useNckhStore()
const router = useRouter()
const { inbox, loading } = storeToRefs(store)

const DANG_XU_LY = [
  'DANG_XEM_XET_BOI_PNCKH', 'DANG_PHAN_BIEN',
  'CHO_CHINH_SUA_THUYET_MINH', 'DANG_LAP_HOP_DONG',
]

onMounted(() => store.layTatCa())

const filtered = computed(() => inbox.value.filter(d => DANG_XU_LY.includes(d.trangThai)))
function fmt(iso) { return new Date(iso).toLocaleDateString('vi-VN') }
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Đang xử lý</h1>
        <p class="page-subtitle">Hồ sơ đề tài đang trong quy trình xét duyệt</p>
      </div>
    </div>

    <div v-if="loading" class="skeleton" style="height:200px"></div>

    <div v-else-if="!filtered.length">
      <EmptyState icon="⚙️" title="Không có hồ sơ đang xử lý" />
    </div>

    <div v-else class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th>Mã số</th>
            <th>Tên đề tài</th>
            <th>Giảng viên</th>
            <th>Trạng thái</th>
            <th>Cập nhật</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dt in filtered" :key="dt.id">
            <td class="mono text-sm">{{ dt.maSo }}</td>
            <td>{{ dt.tenDeTai }}</td>
            <td class="text-muted">{{ dt.giangVien?.hoTen }}</td>
            <td><StatusBadge :status="dt.trangThai" /></td>
            <td class="text-muted text-sm">{{ fmt(dt.updatedAt) }}</td>
            <td>
              <button class="btn btn-ghost btn-sm" @click="router.push(`/nckh/de-tai/${dt.id}`)">Xem →</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
