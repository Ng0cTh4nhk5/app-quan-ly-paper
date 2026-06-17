<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'
import StatusBadge from '@/components/StatusBadge.vue'
import { CheckCircle, XCircle, Edit3, Send, Search, FileSignature, FlaskConical, Paperclip, FileText, AlertTriangle, PartyPopper } from '@lucide/vue'

const route  = useRoute()
const router = useRouter()
const store  = useDetaiStore()
const { chiTiet, loading } = storeToRefs(store)

// Toast state
const toast = ref(null) // { type, msg }
function showToast(type, msg) {
  toast.value = { type, msg }
  setTimeout(() => { toast.value = null }, 4000)
}

onMounted(() => store.layChiTiet(route.params.id))

const canGuiHoSo   = computed(() =>
  ['DRAFT', 'CHO_BO_SUNG_HO_SO', 'CHO_CHINH_SUA_THUYET_MINH'].includes(chiTiet.value?.trangThai)
)
const canBoSung    = computed(() => chiTiet.value?.trangThai === 'CHO_BO_SUNG_HO_SO')
const canXemHopDong = computed(() =>
  ['DANG_LAP_HOP_DONG', 'DANG_THUC_HIEN', 'DA_HOAN_THANH'].includes(chiTiet.value?.trangThai)
)

const isLoading = ref(false)
async function guiHoSo() {
  try {
    isLoading.value = true
    await store.guiHoSo(route.params.id)
    showToast('success', 'Hồ sơ đã được gửi tới P.NCKH để xét duyệt.')
  } catch (e) {
    showToast('error', e.response?.data?.message ?? 'Không thể gửi hồ sơ.')
  } finally {
    isLoading.value = false
  }
}

function fmt(iso, full = false) {
  if (!iso) return '—'
  if (full) return new Date(iso).toLocaleString('vi-VN')
  return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
function fmtM(n) { return n?.toLocaleString('vi-VN') + ' đ' }

// Status stepper config
const STEPS = [
  { key: 'DRAFT',               label: 'Bản nháp',    icon: Edit3 },
  { key: 'CHO_PNCKH_XEM_XET',  label: 'Chờ P.NCKH', icon: Send },
  { key: 'DANG_PHAN_BIEN',      label: 'Phản biện',   icon: Search },
  { key: 'DANG_LAP_HOP_DONG',   label: 'Hợp đồng',   icon: FileSignature },
  { key: 'DANG_THUC_HIEN',      label: 'Thực hiện',   icon: FlaskConical },
  { key: 'DA_HOAN_THANH',       label: 'Hoàn thành',  icon: CheckCircle },
]

const STATUS_ORDER = STEPS.map(s => s.key)
function getStepState(stepKey, currentStatus) {
  const stepIdx    = STATUS_ORDER.indexOf(stepKey)
  const currentIdx = STATUS_ORDER.indexOf(currentStatus)
  if (stepIdx < currentIdx) return 'done'
  if (stepIdx === currentIdx) return 'active'
  return ''
}
</script>

<template>
  <div>
    <!-- Back -->
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại danh sách</button>

    <!-- Toast -->
    <div v-if="toast" class="inline-toast flex items-center gap-2" :class="toast.type">
      <CheckCircle v-if="toast.type === 'success'" :size="16" />
      <XCircle v-else :size="16" />
      <span>{{ toast.msg }}</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" style="display:flex;flex-direction:column;gap:12px">
      <div class="skeleton" style="height: 48px; border-radius: 8px"></div>
      <div class="skeleton" style="height: 200px; border-radius: 8px"></div>
      <div class="skeleton" style="height: 120px; border-radius: 8px"></div>
    </div>

    <template v-else-if="chiTiet">
      <!-- Title + Actions -->
      <div class="page-header mb-4">
        <div class="page-header-left">
          <div class="flex items-center gap-3 mb-2">
            <StatusBadge :status="chiTiet.trangThai" />
            <span class="mono text-sm text-muted">{{ chiTiet.maSo || 'Chưa có mã số' }}</span>
          </div>
          <h1 class="page-title">{{ chiTiet.tenDeTai }}</h1>
          <p v-if="chiTiet.kyNckh" class="page-subtitle">Kỳ NCKH: {{ chiTiet.kyNckh }}</p>
        </div>
        <div class="action-bar">
          <button
            v-if="canBoSung"
            class="btn btn-secondary flex items-center gap-2"
            @click="router.push(`/gv/de-tai/${chiTiet.id}/bo-sung`)"
          >
            <Paperclip :size="16" /> Bổ sung hồ sơ
          </button>
          <button
            v-if="canXemHopDong"
            class="btn btn-secondary flex items-center gap-2"
            @click="router.push(`/gv/de-tai/${chiTiet.id}/hop-dong`)"
          >
            <FileText :size="16" /> Xem hợp đồng
          </button>
          <button
            v-if="canGuiHoSo"
            class="btn btn-primary flex items-center gap-2"
            :disabled="isLoading"
            @click="guiHoSo"
          >
            <span v-if="isLoading" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <Send v-else :size="16" /> Gửi hồ sơ tới P.NCKH
          </button>
        </div>
      </div>

      <!-- Warning if needs supplement -->
      <div v-if="canBoSung" class="warning-banner mb-4 flex gap-3">
        <AlertTriangle class="shrink-0" :size="20" />
        <div>
          <strong>Cần bổ sung hồ sơ:</strong> P.NCKH yêu cầu bổ sung tài liệu.
          <button class="btn-link ml-1" @click="router.push(`/gv/de-tai/${chiTiet.id}/bo-sung`)">
            Bổ sung ngay →
          </button>
        </div>
      </div>

      <!-- Contract ready banner -->
      <div v-if="chiTiet.trangThai === 'DANG_LAP_HOP_DONG'" class="info-banner mb-4 flex gap-3">
        <PartyPopper class="shrink-0" :size="20" />
        <div>
          <strong>Hợp đồng đã sẵn sàng!</strong> Đề tài được chấp thuận. Vui lòng xem và ký xác nhận hợp đồng.
          <button class="btn-link ml-1" @click="router.push(`/gv/de-tai/${chiTiet.id}/hop-dong`)">
            Ký hợp đồng →
          </button>
        </div>
      </div>

      <!-- Status Stepper -->
      <div class="status-stepper mb-6">
        <div
          v-for="step in STEPS" :key="step.key"
          class="stepper-step"
          :class="getStepState(step.key, chiTiet.trangThai)"
        >
          <div class="stepper-dot"><component :is="step.icon" :size="16" /></div>
          <div class="stepper-label">{{ step.label }}</div>
        </div>
      </div>

      <!-- Content 2-col -->
      <div class="detail-layout">
        <!-- Left: Main content -->
        <div class="detail-main">
          <!-- Description -->
          <div class="card mb-4">
            <div class="card-header">
              <h3 class="card-title">Mô tả & Tóm tắt</h3>
            </div>
            <div class="card-body">
              <p v-if="chiTiet.moTa" class="detail-text">{{ chiTiet.moTa }}</p>
              <p v-else class="text-muted" style="font-style:italic">Chưa có mô tả.</p>

              <div v-if="chiTiet.tomTat" class="mt-4">
                <h4 class="field-label">Tóm tắt nghiên cứu</h4>
                <p class="detail-text">{{ chiTiet.tomTat }}</p>
              </div>
            </div>
          </div>

          <!-- Activity timeline -->
          <div v-if="chiTiet.lichSu?.length" class="card">
            <div class="card-header">
              <h3 class="card-title">Lịch sử xử lý</h3>
            </div>
            <div class="card-body">
              <div class="timeline">
                <div v-for="(ev, i) in chiTiet.lichSu" :key="i" class="timeline-item">
                  <div class="timeline-dot active">
                    {{ i === 0 ? '●' : '○' }}
                  </div>
                  <div class="timeline-body">
                    <div class="timeline-title">{{ ev.hanhDong }}</div>
                    <div class="timeline-time">{{ ev.nguoiThucHien }} · {{ fmt(ev.thoiGian, true) }}</div>
                    <div v-if="ev.ghiChu" class="timeline-desc"
                      style="background:var(--color-surface-alt);border-radius:var(--radius-md);padding:var(--space-2) var(--space-3);margin-top:var(--space-2)">
                      {{ ev.ghiChu }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Meta sidebar -->
        <div class="detail-sidebar">
          <!-- Info card -->
          <div class="card mb-4">
            <div class="card-header">
              <h3 class="card-title">Thông tin đề tài</h3>
            </div>
            <div class="card-body" style="padding: 0">
              <dl class="info-dl">
                <div class="info-row">
                  <dt>Mã số</dt>
                  <dd class="mono">{{ chiTiet.maSo || '—' }}</dd>
                </div>
                <div class="info-row">
                  <dt>Kỳ NCKH</dt>
                  <dd>{{ chiTiet.kyNckh || '—' }}</dd>
                </div>
                <div class="info-row">
                  <dt>Lĩnh vực</dt>
                  <dd>{{ chiTiet.linhVuc || '—' }}</dd>
                </div>
                <div class="info-row">
                  <dt>Thời gian</dt>
                  <dd>{{ chiTiet.thoiGianThucHien || '—' }} tháng</dd>
                </div>
                <div class="info-row">
                  <dt>Kinh phí</dt>
                  <dd class="mono">{{ chiTiet.kinhPhi ? fmtM(chiTiet.kinhPhi) : '—' }}</dd>
                </div>
                <div class="info-row">
                  <dt>Ngày tạo</dt>
                  <dd>{{ fmt(chiTiet.createdAt) }}</dd>
                </div>
                <div class="info-row">
                  <dt>Cập nhật</dt>
                  <dd>{{ fmt(chiTiet.updatedAt) }}</dd>
                </div>
              </dl>
            </div>
          </div>

          <!-- Thành viên -->
          <div v-if="chiTiet.thanhVien?.length" class="card mb-4">
            <div class="card-header"><h3 class="card-title">Thành viên</h3></div>
            <div class="card-body" style="padding:0">
              <div v-for="tv in chiTiet.thanhVien" :key="tv.id" class="member-row">
                <div class="member-avatar">{{ tv.hoTen?.charAt(0) }}</div>
                <div>
                  <div class="member-name">{{ tv.hoTen }}</div>
                  <div class="member-role">{{ tv.vaiTro || 'Thành viên' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Quick action buttons -->
          <div class="card">
            <div class="card-header"><h3 class="card-title">Thao tác</h3></div>
            <div class="card-body" style="display:flex;flex-direction:column;gap:var(--space-2)">
              <button
                v-if="canGuiHoSo" class="btn btn-primary w-full flex items-center justify-center gap-2"
                :disabled="isLoading" @click="guiHoSo"
              >
                <Send :size="16" /> Gửi hồ sơ tới P.NCKH
              </button>
              <button
                v-if="canBoSung" class="btn btn-secondary w-full flex items-center justify-center gap-2"
                @click="router.push(`/gv/de-tai/${chiTiet.id}/bo-sung`)"
              >
                <Paperclip :size="16" /> Bổ sung hồ sơ
              </button>
              <button
                v-if="canXemHopDong" class="btn btn-secondary w-full flex items-center justify-center gap-2"
                @click="router.push(`/gv/de-tai/${chiTiet.id}/hop-dong`)"
              >
                <FileText :size="16" /> Xem hợp đồng
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Not found -->
    <div v-else class="alert alert-danger flex items-center gap-2">
      <XCircle :size="16" /> Không tìm thấy đề tài hoặc bạn không có quyền truy cập.
    </div>
  </div>
</template>

<style scoped>
.action-bar { display: flex; gap: var(--space-2); flex-wrap: wrap; }

.inline-toast {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font: var(--text-body);
  margin-bottom: var(--space-4);
}
.inline-toast.success { background: var(--color-success-bg); color: var(--color-success-text); border: 1px solid var(--color-success-border); }
.inline-toast.error   { background: var(--color-error-bg);   color: var(--color-error-text);   border: 1px solid var(--color-error-border); }

.detail-main    { display: flex; flex-direction: column; min-width: 0; }
.detail-sidebar { display: flex; flex-direction: column; }

.detail-text { font: var(--text-body-lg); color: var(--color-text-secondary); white-space: pre-wrap; line-height: 1.7; }

.field-label { font: var(--text-h4); color: var(--color-text-primary); margin-bottom: var(--space-2); }

/* Info DL */
.info-dl { display: flex; flex-direction: column; }
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  gap: var(--space-3);
}
.info-row:last-child { border-bottom: none; }
.info-row dt { font: var(--text-sm); color: var(--color-text-muted); white-space: nowrap; }
.info-row dd { font: var(--text-body); color: var(--color-text-primary); text-align: right; }

/* Members */
.member-row {
  display: flex; align-items: center; gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid var(--color-border);
}
.member-row:last-child { border-bottom: none; }
.member-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--color-accent-light);
  border: 1px solid var(--color-accent);
  color: var(--color-accent);
  display: flex; align-items: center; justify-content: center;
  font: 600 13px/1 var(--font-sans);
  flex-shrink: 0;
}
.member-name { font: var(--text-h4); color: var(--color-text-primary); }
.member-role { font: var(--text-sm); color: var(--color-text-muted); }

.btn-link {
  background: none; border: none; cursor: pointer;
  color: var(--color-accent); font: inherit; font-weight: 600; padding: 0;
}
.btn-link:hover { color: var(--color-accent-hover); }
</style>
