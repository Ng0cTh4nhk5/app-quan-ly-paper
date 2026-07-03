<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'
import { useToast } from '@/composables/useToast'
import { validateThuyetMinhFile } from '@/utils/uploadValidation'
import { ClipboardList, AlertTriangle, Clock, FolderOpen, FileText, Send, ArrowLeft, X } from '@lucide/vue'

const route  = useRoute()
const router = useRouter()
const store  = useDetaiStore()
const { chiTiet, loading } = storeToRefs(store)
const submitting  = ref(false)
const uploadFiles = ref([])
const now = ref(Date.now())
let countdownTimer = null
const { add: toastAdd } = useToast()

const form = reactive({
  moTaBoSung: '',
  kinhPhiBoSung: null,
})

onMounted(async () => {
  await store.layChiTiet(route.params.id)
  countdownTimer = window.setInterval(() => {
    now.value = Date.now()
  }, 60000)
})

onUnmounted(() => {
  if (countdownTimer) window.clearInterval(countdownTimer)
})

const yeuCau = computed(() => {
  const y = chiTiet.value?.yeuCauBoSung
  if (!y) return null
  if (typeof y === 'string') return { noiDung: y }
  return y
})

const deadline = computed(() => {
  if (!yeuCau.value?.deadline) return null
  return new Date(yeuCau.value.deadline).toLocaleDateString('vi-VN')
})

const isOverDue = computed(() => {
  if (!yeuCau.value?.deadline) return false
  return new Date(yeuCau.value.deadline).getTime() < now.value
})

const remainingTime = computed(() => {
  if (!yeuCau.value?.deadline) return ''

  const deadlineTime = new Date(yeuCau.value.deadline).getTime()
  const diffMs = deadlineTime - now.value
  const absMs = Math.abs(diffMs)
  const totalMinutes = Math.max(1, Math.floor(absMs / 60000))
  const days = Math.floor(totalMinutes / 1440)
  const hours = Math.floor((totalMinutes % 1440) / 60)
  const minutes = totalMinutes % 60
  const prefix = diffMs >= 0 ? 'Còn' : 'Đã quá hạn'

  if (days > 0) return `${prefix} ${days} ngày ${String(hours).padStart(2, '0')} giờ`
  if (hours > 0) return `${prefix} ${hours} giờ ${String(minutes).padStart(2, '0')} phút`
  return `${prefix} ${minutes} phút`
})

async function onFileChange(event) {
  const files = Array.from(event.target.files ?? [])
  for (const file of files) {
    const result = await validateThuyetMinhFile(file)
    if (!result.valid) {
      toastAdd({
        severity: 'error',
        summary: 'File khong hop le',
        detail: result.message,
      })
      event.target.value = ''
      return
    }
  }
  uploadFiles.value = files
}

function removeFile(index) {
  uploadFiles.value.splice(index, 1)
}

async function submit() {
  if (isOverDue.value) {
    toastAdd({
      severity: 'warning',
      summary: 'Ho so da qua han bo sung',
      detail: 'Vui long lien he P.NCKH de duoc mo lai han nop.',
    })
    return
  }
  if (!form.moTaBoSung.trim()) return
  submitting.value = true
  try {
    // 1. Upload files (nếu có)
    for (const file of uploadFiles.value) {
      await store.uploadFile(route.params.id, file, 'THUYET_MINH')
    }
    // 2. Gửi bổ sung
    await store.boSungHoSo(route.params.id, {
      moTaBoSung: form.moTaBoSung,
      ...(form.kinhPhiBoSung ? { kinhPhiBoSung: form.kinhPhiBoSung } : {}),
    })
    toastAdd({ severity: 'success', summary: 'Da gui bo sung', detail: 'Ho so da duoc gui lai cho P.NCKH.' })
    router.push(`/gv/de-tai/${route.params.id}`)
  } catch (e) {
    toastAdd({ severity: 'error', summary: 'Lỗi', detail: e.response?.data?.message ?? 'Gửi bổ sung thất bại.' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div>
    <button class="btn btn-ghost btn-sm mb-4 flex items-center gap-2" @click="router.back()">
      <ArrowLeft size="16" /> Quay lại
    </button>

    <!-- Loading -->
    <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
      <div class="skeleton" style="height:100px;border-radius:8px"></div>
      <div class="skeleton" style="height:200px;border-radius:8px"></div>
    </div>

    <template v-else-if="chiTiet">
      <div class="page-header mb-4">
        <div class="page-header-left">
          <h1 class="page-title">Bổ sung hồ sơ</h1>
          <p class="page-subtitle mono">{{ chiTiet.maSo }} — {{ chiTiet.tenDeTai }}</p>
        </div>
      </div>

      <!-- Yêu cầu từ P.NCKH -->
      <div v-if="yeuCau" class="yeu-cau-box mb-4" :class="{ overdue: isOverDue }">
        <div class="yeu-cau-header">
          <span class="yeu-cau-icon"><ClipboardList :size="18" /></span>
          <span class="yeu-cau-title">Yêu cầu từ P.NCKH</span>
          <span v-if="deadline" class="yeu-cau-deadline flex items-center gap-1" :class="{ overdue: isOverDue }">
            <AlertTriangle v-if="isOverDue" :size="14" />
            <Clock v-else :size="14" />
            {{ isOverDue ? 'Quá hạn:' : 'Hạn nộp:' }} {{ deadline }}
          </span>
          <span v-if="remainingTime" class="yeu-cau-countdown" :class="{ overdue: isOverDue }">
            {{ remainingTime }}
          </span>
        </div>
        <p class="yeu-cau-content">{{ yeuCau.noiDung ?? yeuCau.lyDo ?? 'P.NCKH yeu cau bo sung ho so.' }}</p>
      </div>

      <div class="form-card">
        <form @submit.prevent="submit">
          <!-- Upload file -->
          <div class="form-group">
            <label class="form-label">
              Tài liệu bổ sung <span class="text-muted text-sm">(PDF, DOCX — tối đa 20MB/file)</span>
            </label>

            <label class="file-upload-area" :class="{ 'has-files': uploadFiles.length }">
              <input type="file" multiple accept=".pdf,.docx" @change="onFileChange" class="file-input" />
              <div v-if="!uploadFiles.length" class="file-placeholder">
                <span class="file-icon"><FolderOpen :size="32" /></span>
                <span>Kéo thả hoặc <u>chọn file</u></span>
              </div>
              <div v-else class="file-list">
                <div v-for="(f, i) in uploadFiles" :key="i" class="file-item">
                  <span class="file-item-icon"><FileText :size="16" /></span>
                  <span class="file-item-name">{{ f.name }}</span>
                  <span class="file-item-size text-muted text-sm">{{ (f.size / 1048576).toFixed(1) }} MB</span>
                  <button type="button" class="file-item-remove" @click.stop="removeFile(i)"><X size="14" /></button>
                </div>
                <label class="btn btn-ghost btn-sm" style="cursor:pointer">
                  <input type="file" multiple accept=".pdf,.docx" @change="onFileChange" style="display:none" />
                  + Thêm file
                </label>
              </div>
            </label>
          </div>

          <!-- Nội dung bổ sung -->
          <div class="form-group">
            <label class="form-label">Mô tả nội dung bổ sung <span class="required">*</span></label>
            <textarea
              v-model="form.moTaBoSung"
              class="form-textarea"
              rows="5"
              placeholder="Giải thích tài liệu đính kèm và nêu rõ những điểm đã bổ sung theo yêu cầu của P.NCKH..."
              required
            ></textarea>
          </div>

          <!-- Điều chỉnh kinh phí -->
          <div class="form-group">
            <label class="form-label">Điều chỉnh kinh phí <span class="text-muted text-sm">(nếu cần)</span></label>
            <div class="input-with-addon">
              <input
                v-model.number="form.kinhPhiBoSung"
                type="number" class="form-input"
                placeholder="Để trống nếu không thay đổi"
                min="0" step="100000"
              />
              <span class="input-addon">VNĐ</span>
            </div>
            <span class="form-hint">Kinh phí hiện tại: {{ chiTiet.kinhPhi?.toLocaleString('vi-VN') }} đ</span>
          </div>

          <!-- Actions -->
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="router.back()">Hủy</button>
            <button
              type="submit"
              class="btn btn-primary flex items-center gap-2"
              :disabled="submitting || isOverDue || !form.moTaBoSung.trim()"
            >
              <span v-if="submitting" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              <Send v-else :size="16" />
              <span>{{ submitting ? 'Đang gửi...' : 'Gửi bổ sung' }}</span>
            </button>
          </div>
        </form>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* Yêu cầu box */
.yeu-cau-box {
  background: color-mix(in srgb, var(--color-warning) 8%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-warning) 40%, transparent);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
}
.yeu-cau-box.overdue {
  background: color-mix(in srgb, var(--color-danger) 8%, transparent);
  border-color: color-mix(in srgb, var(--color-danger) 40%, transparent);
}
.yeu-cau-header {
  display: flex; align-items: center; gap: var(--space-2); margin-bottom: var(--space-2);
}
.yeu-cau-icon { font-size: 18px; }
.yeu-cau-title { font: var(--text-h4); color: var(--color-text-primary); flex: 1; }
.yeu-cau-deadline {
  font: var(--text-sm); color: var(--color-warning);
  background: color-mix(in srgb, var(--color-warning) 15%, transparent);
  padding: 2px 8px; border-radius: 999px;
}
.yeu-cau-deadline.overdue { color: var(--color-danger); background: color-mix(in srgb, var(--color-danger) 15%, transparent); }
.yeu-cau-countdown {
  font: var(--text-sm);
  font-weight: 600;
  color: var(--color-warning);
  white-space: nowrap;
}
.yeu-cau-countdown.overdue { color: var(--color-danger); }
.yeu-cau-content { font: var(--text-body); color: var(--color-text-secondary); white-space: pre-wrap; line-height: 1.7; }

/* Form card */
.form-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  max-width: 720px;
}

/* File upload */
.file-upload-area {
  display: block;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  cursor: pointer;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}
.file-upload-area:hover,
.file-upload-area.has-files { border-color: var(--color-accent); }
.file-input { display: none; }
.file-placeholder {
  display: flex; flex-direction: column; align-items: center;
  gap: var(--space-2); padding: var(--space-4);
  font: var(--text-body); color: var(--color-text-muted);
  text-align: center;
}
.file-icon { font-size: 32px; }
.file-list { display: flex; flex-direction: column; gap: var(--space-2); }
.file-item {
  display: flex; align-items: center; gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface-alt);
  border-radius: var(--radius-md);
}
.file-item-icon { font-size: 16px; }
.file-item-name { flex: 1; font: var(--text-body); color: var(--color-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-item-remove {
  background: none; border: none; cursor: pointer;
  color: var(--color-text-muted); font-size: 14px; padding: 2px 6px;
  border-radius: var(--radius-sm);
}
.file-item-remove:hover { background: var(--color-danger-bg, #fff0f0); color: var(--color-danger); }

/* Input with addon */
.input-with-addon { display: flex; align-items: center; }
.input-with-addon .form-input { flex: 1; border-radius: var(--radius-md) 0 0 var(--radius-md); border-right: none; }
.input-addon {
  padding: 0 var(--space-3); height: 40px;
  border: 1px solid var(--color-border); border-left: none;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  display: flex; align-items: center;
  background: var(--color-surface-alt);
  font: var(--text-sm); color: var(--color-text-muted);
}

.form-actions {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding-top: var(--space-5); border-top: 1px solid var(--color-border); margin-top: var(--space-6);
}

@media (max-width: 640px) {
  .yeu-cau-header {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .yeu-cau-title {
    flex-basis: calc(100% - 32px);
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
