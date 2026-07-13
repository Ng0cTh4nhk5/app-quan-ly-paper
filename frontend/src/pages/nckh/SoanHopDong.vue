<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import { useToast } from '@/composables/useToast'
import EmptyState from '@/components/EmptyState.vue'
import {
  CONTRACT_DRAFTABLE_STATUSES,
  MAX_ADVANCE_RATE_PERCENT,
  normalizeContractStatus,
} from '@/utils/sopDGuards'
import { AlertCircle, ArrowLeft, FileText, PenTool } from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const store = useNckhStore()
const toast = useToast()
const { chiTiet, loading, error, actionLoading } = storeToRefs(store)
const todayIso = new Date().toISOString().slice(0, 10)
const originalNgayBatDau = ref('')

const form = ref({
  kinhPhi: null,
  ngayBatDau: '',
  ngayKetThuc: '',
  // FIXED(TL): Default 0 thay vì 50 để tránh auto-select 50% khi soạn mới.
  tyLeTamUng: 0,
  ghiChu: '',
})

const approvedBudget = computed(() => Number(chiTiet.value?.kinhPhi ?? form.value.kinhPhi ?? 0))
const contractStatus = computed(() => normalizeContractStatus(chiTiet.value))
const canEditContract = computed(() =>
  chiTiet.value?.trangThai === 'DANG_LAP_HOP_DONG' &&
  CONTRACT_DRAFTABLE_STATUSES.includes(contractStatus.value)
)
const isRevision = computed(() => ['CAN_SUA', 'YEU_CAU_SUA'].includes(contractStatus.value))
const hasPastRevisionStart = computed(() =>
  isRevision.value &&
  Boolean(originalNgayBatDau.value) &&
  originalNgayBatDau.value < todayIso
)
const canKeepExistingPastStart = computed(() =>
  hasPastRevisionStart.value &&
  form.value.ngayBatDau === originalNgayBatDau.value
)
const minNgayBatDau = computed(() =>
  hasPastRevisionStart.value ? originalNgayBatDau.value : todayIso
)
const readOnlyContractState = computed(() => {
  const states = {
    CHO_GV_XEM: {
      title: 'Dự thảo hợp đồng đã gửi GV',
      message: 'Đang chờ giảng viên xem xét và phản hồi hợp đồng.',
    },
    CHO_PHAN_HOI: {
      title: 'Dự thảo hợp đồng đã gửi GV',
      message: 'Đang chờ giảng viên xem xét và phản hồi hợp đồng.',
    },
    CHO_KY: {
      title: 'GV đã đồng ý nội dung hợp đồng',
      message: 'Không chỉnh sửa dự thảo ở bước này. P.NCKH xác nhận ký hợp đồng tại màn chi tiết đề tài.',
    },
    DA_KY: {
      title: 'Hợp đồng đã ký',
      message: 'Hợp đồng đã hoàn tất và đề tài chuyển sang giai đoạn thực hiện.',
    },
  }
  return states[contractStatus.value] ?? {
    title: 'Không thể soạn hợp đồng',
    message: 'Hợp đồng không còn ở trạng thái cho phép soạn hoặc chỉnh sửa.',
  }
})

const errors = computed(() => ({
  kinhPhi: !approvedBudget.value || approvedBudget.value <= 0 ? 'Kinh phí phê duyệt phải lớn hơn 0.' : '',
  ngayBatDau: !form.value.ngayBatDau
    ? 'Vui lòng chọn ngày bắt đầu.'
    : form.value.ngayBatDau < todayIso && !canKeepExistingPastStart.value
      ? 'Ngày bắt đầu không được trước ngày hiện tại.'
      : '',
  ngayKetThuc: !form.value.ngayKetThuc
    ? 'Vui lòng chọn ngày kết thúc.'
    : new Date(form.value.ngayKetThuc) <= new Date(form.value.ngayBatDau)
      ? 'Ngày kết thúc phải sau ngày bắt đầu.'
      : '',
  tyLeTamUng: form.value.tyLeTamUng < 0 || form.value.tyLeTamUng > MAX_ADVANCE_RATE_PERCENT ? `Tỷ lệ tạm ứng tối đa từ 0 đến ${MAX_ADVANCE_RATE_PERCENT}%.` : '',
}))

const canSubmit = computed(() => canEditContract.value && !Object.values(errors.value).some(Boolean))
const chuNhiemName = computed(() => chiTiet.value?.giangVien?.hoTen ?? chiTiet.value?.chuNhiem ?? '---')
const feedbackMeta = computed(() => {
  const feedback = chiTiet.value?.hopDongFeedback
  if (!feedback) return ''
  const actor = feedback.actor || 'Giảng viên'
  const createdAt = feedback.createdAt ? new Date(feedback.createdAt).toLocaleString('vi-VN') : ''
  return createdAt ? `${actor} - ${createdAt}` : actor
})

onMounted(async () => {
  try {
    await store.layChiTiet(route.params.id)
    if (chiTiet.value) {
      form.value.kinhPhi = chiTiet.value.kinhPhi
      form.value.ngayBatDau = chiTiet.value.ngayBatDau ?? ''
      originalNgayBatDau.value = chiTiet.value.ngayBatDau ?? ''
      form.value.ngayKetThuc = chiTiet.value.ngayKetThuc ?? ''
      // FIXED(TL): Fallback về 0 thay vì MAX_ADVANCE_RATE_PERCENT khi chưa có dữ liệu tỷ lệ tạm ứng.
      const savedRate = Number(chiTiet.value.tyLeTamUng ?? 0)
      form.value.tyLeTamUng = savedRate <= 1 ? savedRate * 100 : savedRate
      form.value.ghiChu = chiTiet.value.ghiChuHopDong ?? ''
    }
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Không thể tải đề tài', detail: e.response?.data?.message ?? error.value })
  }
})

async function submit() {
  if (!canSubmit.value) return
  try {
    await store.soanHopDong(route.params.id, {
      ...form.value,
      kinhPhi: approvedBudget.value,
      tyLeTamUng: Number(form.value.tyLeTamUng) / 100,
    })
    toast.add({ severity: 'success', summary: isRevision.value ? 'Hợp đồng đã cập nhật và gửi lại cho GV' : 'Hợp đồng đã gửi cho GV xem xét' })
    router.push(`/nckh/de-tai/${route.params.id}`)
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Soạn hợp đồng thất bại', detail: e.response?.data?.message ?? e.message })
  }
}

function fmtM(n) {
  return n ? `${Number(n).toLocaleString('vi-VN')} đ` : '---'
}

function fmtDate(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '---'
}
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4 flex items-center gap-2" @click="router.back()">
      <ArrowLeft :size="16" /> Quay lại
    </button>

    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">{{ isRevision ? 'Chỉnh sửa hợp đồng' : 'Soạn hợp đồng' }}</h1>
        <p class="page-subtitle">Lập dự thảo hợp đồng để gửi giảng viên xem và phản hồi</p>
      </div>
    </div>

    <div v-if="loading" class="skeleton" style="height:200px"></div>

    <div v-else-if="error" class="alert alert-danger">
      <AlertCircle :size="18" /> {{ error }}
    </div>

    <div v-else-if="chiTiet && !canEditContract" class="contract-readonly-card">
      <div class="contract-readonly-icon">
        <AlertCircle :size="20" />
      </div>
      <div class="contract-readonly-body">
        <h2>{{ readOnlyContractState.title }}</h2>
        <p>{{ readOnlyContractState.message }}</p>
        <div class="contract-readonly-actions">
          <button class="btn btn-secondary" @click="router.push(`/nckh/de-tai/${route.params.id}`)">
            Về chi tiết đề tài
          </button>
        </div>
      </div>
    </div>

    <div v-else-if="chiTiet" class="form-grid">
      <div class="hop-dong-preview">
        <h3 class="preview-title">Xem trước hợp đồng</h3>
        <div class="preview-body">
          <p><strong>Đề tài:</strong> {{ chiTiet.tenDeTai }}</p>
          <p><strong>Mã số:</strong> <span class="mono">{{ chiTiet.maSo }}</span></p>
          <p><strong>Chủ nhiệm:</strong> {{ chuNhiemName }}</p>
          <p><strong>Kinh phí:</strong> {{ fmtM(approvedBudget) }}</p>
          <p><strong>Thời gian:</strong> {{ fmtDate(form.ngayBatDau) }} - {{ fmtDate(form.ngayKetThuc) }}</p>
          <p><strong>Tạm ứng tối đa:</strong> {{ form.tyLeTamUng }}%</p>
          <p v-if="form.ghiChu"><strong>Ghi chú:</strong> {{ form.ghiChu }}</p>
        </div>
      </div>

      <div class="form-card">
        <div v-if="chiTiet.hopDongFeedback" class="feedback-box mb-4">
          <strong>Phản hồi cần xử lý</strong>
          <p>{{ chiTiet.hopDongFeedback.noiDung }}</p>
          <span v-if="feedbackMeta">{{ feedbackMeta }}</span>
        </div>

        <form @submit.prevent="submit">
          <div class="form-group">
            <label class="form-label">Kinh phí phê duyệt (đ) <span class="required">*</span></label>
            <div class="readonly-field" :class="{ 'is-error': errors.kinhPhi }">
              {{ fmtM(approvedBudget) }}
            </div>
            <span class="form-help">Lấy từ kinh phí đã phê duyệt của đề tài.</span>
            <span v-if="errors.kinhPhi" class="form-error">{{ errors.kinhPhi }}</span>
          </div>

          <div class="form-2col">
            <div class="form-group">
              <label class="form-label">Ngày bắt đầu <span class="required">*</span></label>
              <input v-model="form.ngayBatDau" type="date" class="form-input" :class="{ 'is-error': errors.ngayBatDau }" :min="minNgayBatDau" />
              <span v-if="canKeepExistingPastStart" class="form-help">Đang giữ ngày bắt đầu cũ của hợp đồng cần chỉnh sửa.</span>
              <span v-if="errors.ngayBatDau" class="form-error">{{ errors.ngayBatDau }}</span>
            </div>
            <div class="form-group">
              <label class="form-label">Ngày kết thúc <span class="required">*</span></label>
              <input v-model="form.ngayKetThuc" type="date" class="form-input" :class="{ 'is-error': errors.ngayKetThuc }" :min="form.ngayBatDau || todayIso" />
              <span v-if="errors.ngayKetThuc" class="form-error">{{ errors.ngayKetThuc }}</span>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Tỷ lệ tạm ứng tối đa theo hợp đồng ({{ form.tyLeTamUng }}%)</label>
            <input v-model.number="form.tyLeTamUng" type="range" min="0" :max="MAX_ADVANCE_RATE_PERCENT" step="5" class="advance-slider" />
            <span class="form-help">Các đợt tạm ứng thực tế sẽ được tạo sau khi hợp đồng đã ký.</span>
            <span v-if="errors.tyLeTamUng" class="form-error">{{ errors.tyLeTamUng }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Ghi chú hợp đồng</label>
            <textarea v-model="form.ghiChu" class="form-textarea" rows="4" placeholder="Điều khoản đặc biệt nếu có..."></textarea>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" :disabled="actionLoading.soanHopDong" @click="router.back()">Hủy</button>
            <button type="submit" class="btn btn-primary" :disabled="!canSubmit || actionLoading.soanHopDong">
              <span v-if="actionLoading.soanHopDong" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              <PenTool v-else :size="16" />
              {{ actionLoading.soanHopDong ? 'Đang gửi...' : isRevision ? 'Lưu chỉnh sửa và gửi GV xem' : 'Lưu và gửi GV xem' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <EmptyState v-else :icon="FileText" title="Không tìm thấy đề tài" />
  </div>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: minmax(280px, 1fr) minmax(320px, 1fr);
  gap: var(--space-6);
  align-items: start;
}

.hop-dong-preview,
.form-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}

.preview-title {
  font: var(--text-h4);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
}

.preview-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  color: var(--color-text-secondary);
}

.advance-slider {
  width: 100%;
}

.contract-readonly-card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  background: var(--color-surface);
  border: 1px solid color-mix(in srgb, var(--color-warning) 35%, var(--color-border));
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}

.contract-readonly-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  color: var(--color-warning);
  background: color-mix(in srgb, var(--color-warning) 12%, transparent);
  flex-shrink: 0;
}

.contract-readonly-body h2 {
  font: var(--text-h3);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.contract-readonly-body p {
  font: var(--text-body);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-4);
}

.contract-readonly-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.readonly-field {
  min-height: 40px;
  display: flex;
  align-items: center;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface-alt);
  color: var(--color-text-primary);
  font-weight: 600;
}

.readonly-field.is-error {
  border-color: #EF4444;
  background: var(--color-error-bg);
}

.form-help {
  display: block;
  margin-top: var(--space-1);
  font: var(--text-caption);
  color: var(--color-text-muted);
}

.feedback-box {
  border: 1px solid color-mix(in srgb, var(--color-warning) 35%, var(--color-border));
  border-radius: var(--radius-md);
  padding: var(--space-3);
  background: color-mix(in srgb, var(--color-warning) 8%, var(--color-surface));
  color: var(--color-text-secondary);
}

.feedback-box strong {
  display: block;
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
}

.feedback-box p {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.feedback-box span {
  display: block;
  margin-top: var(--space-2);
  font: var(--text-caption);
  color: var(--color-text-muted);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-5);
  border-top: 1px solid var(--color-border);
  margin-top: var(--space-5);
}

@media (max-width: 800px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .contract-readonly-card {
    flex-direction: column;
  }
}
</style>
