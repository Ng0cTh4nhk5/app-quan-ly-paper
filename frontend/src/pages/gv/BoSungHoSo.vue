<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'
import { useToast } from 'primevue/usetoast'
import api from '@/api/axios'

const route  = useRoute()
const router = useRouter()
const store  = useDetaiStore()
const toast  = useToast()
const { chiTiet } = storeToRefs(store)
const submitting = ref(false)

const form = reactive({ moTaBoSung: '', kinhPhiBoSung: null })

onMounted(() => {
  if (!chiTiet.value || chiTiet.value.id !== parseInt(route.params.id))
    store.layChiTiet(route.params.id)
})

async function submit() {
  submitting.value = true
  try {
    await api.post(`/de-tai/${route.params.id}/bo-sung`, form)
    toast.add({ severity: 'success', summary: 'Đã gửi bổ sung', life: 4000 })
    router.push(`/gv/de-tai/${route.params.id}`)
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: e.response?.data?.message ?? 'Lỗi không xác định', life: 5000 })
  } finally { submitting.value = false }
}
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại</button>

    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Bổ sung hồ sơ</h1>
        <p v-if="chiTiet" class="page-subtitle mono">{{ chiTiet.maSo }} — {{ chiTiet.tenDeTai }}</p>
      </div>
    </div>

    <!-- Yêu cầu P.NCKH -->
    <div v-if="chiTiet?.yeuCauBoSung" class="alert alert-warning mb-4">
      <strong>📋 Yêu cầu từ P.NCKH:</strong>
      <p class="mt-1">{{ chiTiet.yeuCauBoSung }}</p>
    </div>

    <div class="form-card">
      <form @submit.prevent="submit">
        <div class="form-group">
          <label class="form-label">Nội dung bổ sung <span class="required">*</span></label>
          <textarea v-model="form.moTaBoSung" class="form-textarea" rows="6"
            placeholder="Mô tả tài liệu/thông tin bổ sung theo yêu cầu..."></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Điều chỉnh kinh phí (nếu cần)</label>
          <input v-model.number="form.kinhPhiBoSung" type="number" class="form-input" placeholder="0" min="0" step="100000" />
          <span class="form-hint">Để trống nếu không thay đổi</span>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="router.back()">Hủy</button>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? 'Đang gửi...' : '📤 Gửi bổ sung' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.form-card {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-8); max-width: 640px;
}
.form-actions {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding-top: var(--space-6); border-top: 1px solid var(--color-border); margin-top: var(--space-6);
}
</style>
