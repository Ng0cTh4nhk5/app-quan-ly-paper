<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePBStore } from '@/stores/pb.store'
import { useToast } from '@/composables/useToast'
import api from '@/api/axios'
import { CheckCircle, Edit2, XCircle, Send } from '@lucide/vue'

const route  = useRoute()
const router = useRouter()
const store  = usePBStore()
const toast  = useToast()

const dt         = ref(null)
const loading    = ref(false)
const submitting = ref(false)

const form = ref({
  diemKhoaHoc: 70,
  diemCongNghe: 70,
  diemTinhKhaDung: 70,
  nhanXet: '',
  deXuat: 'CHAP_THUAN',
})

const DE_XUAT_OPTS = [
  { v: 'CHAP_THUAN', l: 'Chấp thuận', icon: CheckCircle },
  { v: 'YEU_CAU_CHINH_SUA', l: 'Yêu cầu sửa', icon: Edit2 },
  { v: 'TU_CHOI', l: 'Từ chối', icon: XCircle }
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await api.get(`/de-tai/${route.params.id}`)
    dt.value = res.data
  } finally { loading.value = false }
})

async function submit() {
  submitting.value = true
  try {
    await store.nopKetQua(route.params.id, form.value)
    toast.add({ severity: 'success', summary: 'Đã nộp kết quả phản biện', life: 4000 })
    router.push('/pb/de-tai')
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: e.message, life: 5000 })
  } finally { submitting.value = false }
}

const totalScore = computed(() =>
  Math.round((form.value.diemKhoaHoc + form.value.diemCongNghe + form.value.diemTinhKhaDung) / 3)
)

function scoreColor(s) {
  if (s >= 80) return 'var(--color-success)'
  if (s >= 60) return 'var(--color-warning)'
  return 'var(--color-danger)'
}
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại</button>

    <div class="page-header">
      <h1 class="page-title">Phản biện đề tài</h1>
    </div>

    <div v-if="loading" class="skeleton" style="height:200px"></div>

    <div v-else-if="dt" class="pb-layout">
      <!-- Info panel -->
      <div class="detail-card">
        <h3 class="detail-card-title">Thông tin đề tài</h3>
        <p class="pb-name">{{ dt.tenDeTai }}</p>
        <dl class="info-list mt-3">
          <dt>Mã số</dt>    <dd class="mono">{{ dt.maSo }}</dd>
          <dt>GV CN</dt>    <dd>{{ dt.giangVien?.hoTen }}</dd>
          <dt>Lĩnh vực</dt><dd>{{ dt.linhVuc || '—' }}</dd>
          <dt>Kỳ NCKH</dt> <dd>{{ dt.kyNckh }}</dd>
        </dl>
        <div v-if="dt.moTa" class="mt-4">
          <p class="form-label">Mô tả đề tài</p>
          <p class="detail-desc">{{ dt.moTa }}</p>
        </div>
      </div>

      <!-- Scoring form -->
      <div class="form-card">
        <form @submit.prevent="submit">
          <!-- Score meter -->
          <div class="score-meter mb-6">
            <div class="score-meter-value" :style="{ color: scoreColor(totalScore) }">
              {{ totalScore }}
            </div>
            <div class="score-meter-label">Điểm trung bình</div>
          </div>

          <!-- Criteria -->
          <div v-for="(crit, key) in { diemKhoaHoc: 'Tính khoa học', diemCongNghe: 'Tính công nghệ', diemTinhKhaDung: 'Tính khả dụng' }"
               :key="key" class="form-group">
            <label class="form-label">{{ crit }} ({{ form[key] }}/100)</label>
            <input type="range" v-model.number="form[key]" min="0" max="100" step="5"
                   class="score-slider" :style="{ '--val': form[key] + '%' }" />
          </div>

          <!-- Nhận xét -->
          <div class="form-group">
            <label class="form-label">Nhận xét chi tiết</label>
            <textarea v-model="form.nhanXet" class="form-textarea" rows="6"
              placeholder="Phân tích điểm mạnh, điểm yếu, và góc độ chuyên môn..."></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">Đề xuất</label>
            <div class="deXuat-group">
              <label v-for="opt in DE_XUAT_OPTS"
                     :key="opt.v" class="deXuat-option" :class="{ active: form.deXuat === opt.v }">
                <input type="radio" :value="opt.v" v-model="form.deXuat" hidden />
                <div class="flex items-center justify-center gap-2">
                  <component :is="opt.icon" :size="16" />
                  <span>{{ opt.l }}</span>
                </div>
              </label>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="router.back()">Hủy</button>
            <button type="submit" class="btn btn-primary flex items-center gap-2" :disabled="submitting">
              <span v-if="submitting" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              <Send v-else :size="16" />
              <span>{{ submitting ? 'Đang nộp...' : 'Nộp kết quả' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pb-layout {
  display: grid; grid-template-columns: 360px 1fr; gap: var(--space-6); align-items: start;
}
.detail-card, .form-card {
  background: var(--color-surface); border: 1px solid var(--color-border);
  border-radius: var(--radius-lg); padding: var(--space-5);
}
.detail-card-title { font: var(--text-h4); color: var(--color-text-primary); margin-bottom: var(--space-3); }
.pb-name  { font: var(--text-h3); color: var(--color-text-primary); line-height: 1.4; }
.info-list {
  display: grid; grid-template-columns: auto 1fr; row-gap: var(--space-2); column-gap: var(--space-3);
}
.info-list dt { font: var(--text-sm); color: var(--color-text-muted); }
.info-list dd { font: var(--text-body); color: var(--color-text-primary); }
.detail-desc  { font: var(--text-sm); color: var(--color-text-secondary); }

/* Score meter */
.score-meter { text-align: center; padding: var(--space-4); background: var(--color-surface-alt); border-radius: var(--radius-md); }
.score-meter-value { font: 800 48px/1 var(--font-sans); transition: color 0.3s; }
.score-meter-label { font: var(--text-sm); color: var(--color-text-muted); margin-top: 4px; }

/* Slider */
.score-slider {
  width: 100%; height: 6px; border-radius: 3px;
  -webkit-appearance: none; appearance: none;
  background: linear-gradient(to right, var(--color-accent) var(--val), var(--color-border) var(--val));
  cursor: pointer;
}
.score-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px; height: 18px; border-radius: 50%;
  background: var(--color-accent); border: 2px solid #fff;
  box-shadow: var(--shadow-sm); cursor: pointer;
}

/* Đề xuất */
.deXuat-group { display: flex; gap: var(--space-2); flex-wrap: wrap; }
.deXuat-option {
  flex: 1; min-width: 100px;
  padding: var(--space-2) var(--space-3); border-radius: var(--radius-md);
  border: 1px solid var(--color-border); text-align: center;
  cursor: pointer; font: var(--text-sm); transition: all var(--transition-fast);
}
.deXuat-option.active {
  border-color: var(--color-accent); background: color-mix(in srgb, var(--color-accent) 10%, transparent);
  color: var(--color-accent); font-weight: 600;
}

.form-actions {
  display: flex; justify-content: flex-end; gap: var(--space-3);
  padding-top: var(--space-5); border-top: 1px solid var(--color-border); margin-top: var(--space-5);
}
@media (max-width: 768px) { .pb-layout { grid-template-columns: 1fr; } }
</style>
