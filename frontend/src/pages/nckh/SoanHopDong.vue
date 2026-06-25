<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import { useToast } from '@/composables/useToast'
import { PenTool, ArrowLeft } from '@lucide/vue'

const route  = useRoute()
const router = useRouter()
const store  = useNckhStore()
const toast  = useToast()
const { chiTiet, loading } = storeToRefs(store)
const submitting = ref(false)

const form = ref({ kinhPhi: null, thoiGian: 12, ghiChu: '' })

onMounted(async () => {
  await store.layChiTiet(route.params.id)
  if (chiTiet.value) form.value.kinhPhi = chiTiet.value.kinhPhi
})

async function submit() {
  submitting.value = true
  try {
    await store.soanHopDong(route.params.id, form.value)
    toast.add({ severity: 'success', summary: 'Hợp đồng đã gửi cho GV xem xét', life: 4000 })
    router.push('/nckh/dang-xu-ly')
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: e.message, life: 5000 })
  } finally { submitting.value = false }
}
function fmtM(n) { return n?.toLocaleString('vi-VN') + ' đ' }
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4 flex items-center gap-2" @click="router.back()">
      <ArrowLeft size="16" /> Quay lại
    </button>

    <div class="page-header">
      <h1 class="page-title">Soạn hợp đồng</h1>
    </div>

    <div v-if="loading" class="skeleton" style="height:200px"></div>

    <div v-else-if="chiTiet" class="form-grid">
      <!-- Preview -->
      <div class="hop-dong-preview">
        <h3 class="preview-title">Xem trước</h3>
        <div class="preview-body">
          <p><strong>Đề tài:</strong> {{ chiTiet.tenDeTai }}</p>
          <p><strong>Mã số:</strong> <span class="mono">{{ chiTiet.maSo }}</span></p>
          <p><strong>Chủ nhiệm:</strong> {{ chiTiet.giangVien?.hoTen }}</p>
          <p><strong>Kinh phí:</strong> {{ form.kinhPhi ? fmtM(form.kinhPhi) : '—' }}</p>
          <p><strong>Thời gian:</strong> {{ form.thoiGian }} tháng</p>
          <p v-if="form.ghiChu"><strong>Ghi chú:</strong> {{ form.ghiChu }}</p>
        </div>
      </div>

      <!-- Form -->
      <div class="form-card">
        <form @submit.prevent="submit">
          <div class="form-group">
            <label class="form-label">Kinh phí phê duyệt (đ)</label>
            <input v-model.number="form.kinhPhi" type="number" class="form-input" min="0" step="100000" />
          </div>
          <div class="form-group">
            <label class="form-label">Thời gian thực hiện (tháng)</label>
            <input v-model.number="form.thoiGian" type="number" class="form-input" min="1" max="36" />
          </div>
          <div class="form-group">
            <label class="form-label">Ghi chú hợp đồng</label>
            <textarea v-model="form.ghiChu" class="form-textarea" rows="4" placeholder="Điều khoản đặc biệt..."></textarea>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="router.back()">Hủy</button>
            <button type="submit" class="btn btn-primary flex items-center gap-2" :disabled="submitting">
              <span v-if="submitting">Đang gửi...</span>
              <span v-else class="flex items-center gap-2"><PenTool size="16" /> Lưu và gửi GV xem xét</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-6); align-items: start; }
.hop-dong-preview {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-5);
}
.preview-title { font: var(--text-h4); color: var(--color-text-primary); margin-bottom: var(--space-4); }
.preview-body  { display: flex; flex-direction: column; gap: var(--space-2); font: var(--text-body); color: var(--color-text-secondary); }
.form-card {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-6);
}
.form-actions {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding-top: var(--space-5); border-top: 1px solid var(--color-border); margin-top: var(--space-5);
}
@media (max-width: 768px) { .form-grid { grid-template-columns: 1fr; } }
</style>
