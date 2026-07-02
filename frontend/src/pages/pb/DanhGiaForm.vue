<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePBStore } from '@/stores/pb.store'
import { useToast } from '@/composables/useToast'
import EmptyState from '@/components/EmptyState.vue'
import { AlertCircle, CheckCircle, Edit2, FileText, Send, XCircle } from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const store = usePBStore()
const toast = useToast()
const { chiTiet: dt, canActions, loading, error } = storeToRefs(store)
const submitting = ref(false)

const form = ref({
  diemKhoaHoc: 70,
  diemCongNghe: 70,
  diemTinhKhaDung: 70,
  nhanXet: '',
  ketQua: 'CHAP_NHAN',
})

const DE_XUAT_OPTS = [
  { v: 'CHAP_NHAN', l: 'Chấp nhận', icon: CheckCircle },
  { v: 'CAN_SUA', l: 'Cần sửa', icon: Edit2 },
  { v: 'TU_CHOI', l: 'Từ chối', icon: XCircle },
]

const totalScore = computed(() => Math.round(
  form.value.diemKhoaHoc * 0.4 +
  form.value.diemCongNghe * 0.3 +
  form.value.diemTinhKhaDung * 0.3,
))

const nhanXetError = computed(() =>
  form.value.nhanXet.trim().length > 0 && form.value.nhanXet.trim().length < 20
    ? 'Nhận xét cần ít nhất 20 ký tự.'
    : ''
)

const canSubmit = computed(() => canActions.value.nopKetQuaPB && form.value.nhanXet.trim().length >= 20)
const chuNhiemName = computed(() => dt.value?.giangVien?.hoTen ?? dt.value?.chuNhiem ?? '---')

onMounted(async () => {
  try {
    await store.layChiTiet(route.params.id)
    if (!store.canActions.nopKetQuaPB) {
      toast.add({ severity: 'warning', summary: 'Không thể nộp phản biện', detail: 'Đề tài này đã nộp kết quả hoặc bạn không có quyền.' })
      router.replace('/pb/de-tai')
    }
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Không thể tải đề tài', detail: e.response?.data?.message ?? error.value })
    router.replace('/pb/de-tai')
  }
})

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await store.nopKetQua(route.params.id, {
      ...form.value,
      nhanXet: form.value.nhanXet.trim(),
      diemTong: totalScore.value,
    })
    toast.add({ severity: 'success', summary: 'Đã nộp kết quả phản biện' })
    router.push('/pb/de-tai')
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Nộp kết quả thất bại', detail: e.response?.data?.message ?? e.message })
  } finally {
    submitting.value = false
  }
}

function scoreColor(s) {
  if (s >= 80) return 'var(--color-success)'
  if (s >= 60) return 'var(--color-warning)'
  return 'var(--color-danger)'
}

function fmt(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '---'
}
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại</button>

    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">Phản biện đề tài</h1>
        <p class="page-subtitle">Điền điểm và nhận xét chuyên môn để gửi P.NCKH tổng hợp</p>
      </div>
    </div>

    <div v-if="loading" class="skeleton" style="height:220px"></div>

    <div v-else-if="error" class="alert alert-danger">
      <AlertCircle :size="18" /> {{ error }}
    </div>

    <div v-else-if="dt" class="pb-layout">
      <section class="detail-card">
        <h3 class="detail-card-title">Thông tin đề tài</h3>
        <p class="pb-name">{{ dt.tenDeTai }}</p>
        <dl class="info-list mt-3">
          <dt>Mã số</dt><dd class="mono">{{ dt.maSo }}</dd>
          <dt>Chủ nhiệm</dt><dd>{{ chuNhiemName }}</dd>
          <dt>Lĩnh vực</dt><dd>{{ dt.linhVuc || '---' }}</dd>
          <dt>Kỳ NCKH</dt><dd>{{ dt.kyNckh }}</dd>
          <dt>Deadline</dt><dd>{{ fmt(dt.deadlineNopPhanBien) }}</dd>
        </dl>
        <div v-if="dt.moTa" class="mt-4">
          <p class="form-label">Mô tả đề tài</p>
          <p class="detail-desc">{{ dt.moTa }}</p>
        </div>
        <div class="mt-4">
          <p class="form-label">Hồ sơ / tài liệu</p>
          <div v-if="dt.taiLieu?.length" class="doc-list">
            <a v-for="doc in dt.taiLieu" :key="doc.id" :href="doc.downloadUrl || '#'" target="_blank" class="doc-item">
              <FileText :size="16" />
              <span>{{ doc.tenFile || doc.name }}</span>
            </a>
          </div>
          <EmptyState v-else :icon="FileText" title="Chưa có tài liệu" />
        </div>
      </section>

      <section class="form-card">
        <form @submit.prevent="submit">
          <div class="score-meter mb-6">
            <div class="score-meter-value" :style="{ color: scoreColor(totalScore) }">
              {{ totalScore }}
            </div>
            <div class="score-meter-label">Điểm tổng theo trọng số 40/30/30</div>
          </div>

          <div
            v-for="crit in [
              ['diemKhoaHoc', 'Tính khoa học', '40%'],
              ['diemCongNghe', 'Tính công nghệ', '30%'],
              ['diemTinhKhaDung', 'Tính khả dụng', '30%'],
            ]"
            :key="crit[0]"
            class="form-group"
          >
            <label class="form-label">{{ crit[1] }} - {{ crit[2] }} ({{ form[crit[0]] }}/100)</label>
            <input
              type="range"
              v-model.number="form[crit[0]]"
              min="0"
              max="100"
              step="5"
              class="score-slider"
              :style="{ '--val': form[crit[0]] + '%' }"
            />
          </div>

          <div class="form-group">
            <label class="form-label">Nhận xét chi tiết <span class="required">*</span></label>
            <textarea
              v-model="form.nhanXet"
              class="form-textarea"
              :class="{ 'is-error': nhanXetError }"
              rows="6"
              placeholder="Phân tích điểm mạnh, điểm yếu và kiến nghị chuyên môn..."
            ></textarea>
            <span v-if="nhanXetError" class="form-error">{{ nhanXetError }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Kết quả đề xuất</label>
            <div class="deXuat-group">
              <label v-for="opt in DE_XUAT_OPTS" :key="opt.v" class="deXuat-option" :class="{ active: form.ketQua === opt.v }">
                <input type="radio" :value="opt.v" v-model="form.ketQua" hidden />
                <span class="flex items-center justify-center gap-2">
                  <component :is="opt.icon" :size="16" />
                  <span>{{ opt.l }}</span>
                </span>
              </label>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" :disabled="submitting" @click="router.back()">Hủy</button>
            <button type="submit" class="btn btn-primary" :disabled="!canSubmit || submitting">
              <span v-if="submitting" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              <Send v-else :size="16" />
              {{ submitting ? 'Đang nộp...' : 'Nộp kết quả' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<style scoped>
.pb-layout {
  display: grid;
  grid-template-columns: minmax(280px, 360px) 1fr;
  gap: var(--space-6);
  align-items: start;
}

.detail-card,
.form-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}

.detail-card-title {
  font: var(--text-h4);
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}

.pb-name {
  font: var(--text-h3);
  color: var(--color-text-primary);
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.info-list {
  display: grid;
  grid-template-columns: auto 1fr;
  row-gap: var(--space-2);
  column-gap: var(--space-3);
}

.info-list dt {
  font: var(--text-sm);
  color: var(--color-text-muted);
}

.info-list dd,
.detail-desc {
  color: var(--color-text-secondary);
}

.detail-desc {
  font: var(--text-sm);
  line-height: 1.6;
  white-space: pre-wrap;
}

.doc-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.doc-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
}

.doc-item span {
  min-width: 0;
  overflow-wrap: anywhere;
}

.score-meter {
  text-align: center;
  padding: var(--space-4);
  background: var(--color-surface-alt);
  border-radius: var(--radius-md);
}

.score-meter-value {
  font: 800 48px/1 var(--font-sans);
  transition: color 0.3s;
}

.score-meter-label {
  font: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: 4px;
}

.score-slider {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: linear-gradient(to right, var(--color-accent) var(--val), var(--color-border) var(--val));
  cursor: pointer;
}

.score-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--color-accent);
  border: 2px solid #fff;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.deXuat-group {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.deXuat-option {
  flex: 1;
  min-width: 104px;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  text-align: center;
  cursor: pointer;
  font: var(--text-sm);
  transition: all var(--transition-fast);
}

.deXuat-option.active {
  border-color: var(--color-accent);
  background: color-mix(in srgb, var(--color-accent) 10%, transparent);
  color: var(--color-accent);
  font-weight: 600;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-5);
  border-top: 1px solid var(--color-border);
  margin-top: var(--space-5);
}

.form-textarea.is-error {
  border-color: #EF4444;
  background: var(--color-error-bg);
}

@media (max-width: 768px) {
  .pb-layout {
    grid-template-columns: 1fr;
  }
}
</style>
