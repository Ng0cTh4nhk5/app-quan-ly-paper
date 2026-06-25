<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDetaiStore } from '@/stores/detai.store'
import { useToast } from '@/composables/useToast'
import { KY_NCKH_LIST } from '@/mock/db.js'

const router    = useRouter()
const store     = useDetaiStore()
const toast     = useToast()
const submitting = ref(false)

const form = reactive({ tenDeTai: '', linhVuc: '', kyNckhId: '', moTa: '' })
const errors = reactive({ tenDeTai: '', kyNckhId: '' })

const LINH_VUC = [
  'Khoa học máy tính', 'Toán học', 'Vật lý', 'Hóa học',
  'Điện tử - Tự động hóa', 'Hệ thống thông tin', 'Giáo dục học',
  'Vật liệu học', 'Kinh tế học', 'Khác',
]

function validate() {
  errors.tenDeTai = form.tenDeTai.trim().length < 10 ? 'Tên đề tài phải có ít nhất 10 ký tự.' : ''
  errors.kyNckhId = !form.kyNckhId ? 'Vui lòng chọn kỳ NCKH.' : ''
  return !errors.tenDeTai && !errors.kyNckhId
}

async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    const dt = await store.taoDeTai({
      ...form,
      kyNckhId: Number(form.kyNckhId),
    })
    toast.add({ severity: 'success', summary: 'Tạo thành công',
      detail: `Mã đề tài: ${dt.maSo}`, life: 4000 })
    router.push(`/gv/de-tai/${dt.id}`)
  } catch (e) {
    const msg = e.response?.data?.message ?? 'Có lỗi xảy ra.'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 5000 })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Tạo đề tài mới</h1>
        <p class="page-subtitle">Điền thông tin cơ bản để khởi tạo đề tài nghiên cứu</p>
      </div>
    </div>

    <div class="form-card">
      <form @submit.prevent="submit">
        <!-- Tên đề tài -->
        <div class="form-group">
          <label class="form-label">Tên đề tài <span class="required">*</span></label>
          <input
            v-model="form.tenDeTai"
            class="form-input" :class="{ 'is-error': errors.tenDeTai }"
            placeholder="Nhập tên đề tài nghiên cứu..."
            maxlength="500"
          />
          <span v-if="errors.tenDeTai" class="form-error">{{ errors.tenDeTai }}</span>
          <span class="form-hint">{{ form.tenDeTai.length }}/500 ký tự</span>
        </div>

        <div class="form-row">
          <!-- Lĩnh vực -->
          <div class="form-group">
            <label class="form-label">Lĩnh vực nghiên cứu</label>
            <select v-model="form.linhVuc" class="form-select">
              <option value="">— Chọn lĩnh vực —</option>
              <option v-for="lv in LINH_VUC" :key="lv" :value="lv">{{ lv }}</option>
            </select>
          </div>

          <!-- Kỳ NCKH -->
          <div class="form-group">
            <label class="form-label">Kỳ NCKH <span class="required">*</span></label>
            <select
              v-model="form.kyNckhId"
              class="form-select" :class="{ 'is-error': errors.kyNckhId }"
            >
              <option value="">— Chọn kỳ —</option>
              <option v-for="k in KY_NCKH_LIST" :key="k.id" :value="k.id">{{ k.ten }}</option>
            </select>
            <span v-if="errors.kyNckhId" class="form-error">{{ errors.kyNckhId }}</span>
          </div>
        </div>

        <!-- Mô tả -->
        <div class="form-group">
          <label class="form-label">Mô tả sơ bộ</label>
          <textarea v-model="form.moTa" class="form-textarea" rows="5"
            placeholder="Mô tả ngắn gọn về mục tiêu, phương pháp và kết quả dự kiến của đề tài...">
          </textarea>
        </div>

        <!-- Actions -->
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="router.back()">Hủy</button>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            <span v-if="submitting" class="spinner" style="width:14px;height:14px"></span>
            {{ submitting ? 'Đang tạo...' : 'Tạo đề tài' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.form-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  max-width: 720px;
}
.form-row {
  display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);
}
.form-hint { font: var(--text-caption); color: var(--color-text-muted); margin-top: 4px; }
.form-actions {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding-top: var(--space-6); border-top: 1px solid var(--color-border);
  margin-top: var(--space-6);
}
@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; }
}
</style>
