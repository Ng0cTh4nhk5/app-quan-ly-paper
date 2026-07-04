<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'
import { useToast } from '@/composables/useToast'
import { validateThuyetMinhFile } from '@/utils/uploadValidation'
import StatusBadge from '@/components/StatusBadge.vue'
import {
  AlertTriangle,
  CheckCircle,
  Clock,
  Edit3,
  FileSignature,
  FileText,
  FlaskConical,
  Paperclip,
  PartyPopper,
  Search,
  Send,
  Trash2,
  XCircle,
} from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const store = useDetaiStore()
const toast = useToast()
const { chiTiet, loading, error, canActions, actionLoading } = storeToRefs(store)

const uploading = ref(false)
const supportsDocumentDelete = !import.meta.env.VITE_API_URL

onMounted(async () => {
  try {
    await store.layChiTiet(route.params.id)
    await store.layCanActions(route.params.id)
  } catch {}
})

const canEditSubmission = computed(() =>
  ['DRAFT', 'CHO_CHINH_SUA_THUYET_MINH'].includes(chiTiet.value?.trangThai)
)
const canUploadThuyetMinh = canEditSubmission
const canGuiHoSo = computed(() => canActions.value.guiHoSo)
const canXoaTaiLieuPreview = computed(() => canActions.value.xoaTaiLieu)
const canBoSung = computed(() => canActions.value.boSungHoSo)
const canXemHopDong = computed(() => canActions.value.xemHopDong)
const taiLieu = computed(() => chiTiet.value?.taiLieu ?? [])
const auditItems = computed(() => chiTiet.value?.auditLog ?? [])
const hasThuyetMinh = computed(() => taiLieu.value.some(t => t.loai === 'THUYET_MINH'))

const STEPS = [
  { key: 'DRAFT', label: 'Bản nháp', icon: Edit3 },
  { key: 'CHO_PNCKH_XEM_XET', label: 'Chờ P.NCKH', icon: Send },
  { key: 'DANG_PHAN_BIEN', label: 'Phản biện', icon: Search },
  { key: 'DANG_LAP_HOP_DONG', label: 'Hợp đồng', icon: FileSignature },
  { key: 'DANG_THUC_HIEN', label: 'Thực hiện', icon: FlaskConical },
  { key: 'HOAN_TAT', label: 'Hoàn thành', icon: CheckCircle },
]
const STATUS_ORDER = STEPS.map(s => s.key)

function showToast(type, msg) {
  toast.add({
    severity: type === 'success' ? 'success' : 'error',
    summary: type === 'success' ? 'Thành công' : 'Lỗi',
    detail: msg,
  })
}

function fmt(iso, full = false) {
  if (!iso) return '—'
  const date = new Date(iso)
  return full
    ? date.toLocaleString('vi-VN')
    : date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function fmtM(n) {
  return typeof n === 'number' ? `${n.toLocaleString('vi-VN')} đ` : '—'
}

function getStepState(stepKey, currentStatus) {
  const stepIdx = STATUS_ORDER.indexOf(stepKey)
  const currentIdx = STATUS_ORDER.indexOf(currentStatus)
  if (currentIdx === -1 || stepIdx === -1) return ''
  if (stepIdx < currentIdx) return 'done'
  if (stepIdx === currentIdx) return 'active'
  return ''
}

async function guiHoSo() {
  if (!hasThuyetMinh.value) {
    showToast('error', 'Vui lòng tải lên bản thuyết minh trước khi gửi hồ sơ.')
    return
  }
  try {
    await store.guiHoSo(route.params.id)
    showToast('success', 'Hồ sơ đã được gửi tới P.NCKH để xét duyệt.')
  } catch (e) {
    showToast('error', e.response?.data?.message ?? 'Không thể gửi hồ sơ.')
  }
}

async function onUploadThuyetMinh(event) {
  const files = Array.from(event.target.files ?? [])
  if (!files.length) return

  for (const file of files) {
    const result = await validateThuyetMinhFile(file)
    if (!result.valid) {
      showToast('error', result.message)
      event.target.value = ''
      return
    }
  }

  try {
    uploading.value = true
    for (const file of files) {
      await store.uploadFile(route.params.id, file, 'THUYET_MINH')
    }
    showToast('success', 'Đã tải lên bản thuyết minh.')
  } catch (e) {
    showToast('error', e.response?.data?.message ?? 'Không thể tải lên tài liệu.')
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function xoaTaiLieu(taiLieuId) {
  try {
    await store.xoaTaiLieu(route.params.id, taiLieuId)
    showToast('success', 'Đã xóa tài liệu khỏi bản nháp.')
  } catch (e) {
    showToast('error', e.response?.data?.message ?? 'Không thể xóa tài liệu.')
  }
}
</script>

<template>
  <div>
    <button class="btn btn-ghost btn-sm mb-4" @click="router.back()">← Quay lại danh sách</button>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton" style="height: 48px; border-radius: 8px"></div>
      <div class="skeleton" style="height: 200px; border-radius: 8px"></div>
      <div class="skeleton" style="height: 120px; border-radius: 8px"></div>
    </div>

    <div v-else-if="error" class="alert alert-danger flex items-center gap-2">
      <XCircle :size="16" /> {{ error }}
    </div>

    <template v-else-if="chiTiet">
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
            v-if="canUploadThuyetMinh"
            class="btn btn-primary flex items-center gap-2"
            :disabled="actionLoading.guiHoSo || !canGuiHoSo"
            @click="guiHoSo"
          >
            <span v-if="actionLoading.guiHoSo" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <Send v-else :size="16" /> Gửi hồ sơ tới P.NCKH
          </button>
        </div>
      </div>

      <div v-if="canEditSubmission && !hasThuyetMinh" class="warning-banner mb-4 flex gap-3">
        <AlertTriangle class="shrink-0" :size="20" />
        <div>
          <strong>Cần bản thuyết minh:</strong> Tải lên file PDF hoặc DOCX trước khi gửi hồ sơ.
        </div>
      </div>

      <div v-if="canBoSung" class="warning-banner mb-4 flex gap-3">
        <AlertTriangle class="shrink-0" :size="20" />
        <div>
          <strong>Cần bổ sung hồ sơ:</strong> P.NCKH yêu cầu bổ sung tài liệu.
          <button class="btn-link ml-1" @click="router.push(`/gv/de-tai/${chiTiet.id}/bo-sung`)">
            Bổ sung ngay →
          </button>
        </div>
      </div>

      <div v-if="chiTiet.trangThai === 'DANG_LAP_HOP_DONG'" class="info-banner mb-4 flex gap-3">
        <PartyPopper class="shrink-0" :size="20" />
        <div>
          <strong>Hợp đồng đã sẵn sàng.</strong> Đề tài được chấp thuận, vui lòng xem và phản hồi hợp đồng.
          <button class="btn-link ml-1" @click="router.push(`/gv/de-tai/${chiTiet.id}/hop-dong`)">
            Xem hợp đồng →
          </button>
        </div>
      </div>

      <div class="status-stepper mb-6">
        <div
          v-for="step in STEPS"
          :key="step.key"
          class="stepper-step"
          :class="getStepState(step.key, chiTiet.trangThai)"
        >
          <div class="stepper-dot"><component :is="step.icon" :size="16" /></div>
          <div class="stepper-label">{{ step.label }}</div>
        </div>
      </div>

      <div class="detail-layout">
        <div class="detail-main">
          <div class="card mb-4">
            <div class="card-header">
              <h3 class="card-title">Mô tả & tóm tắt</h3>
            </div>
            <div class="card-body">
              <p v-if="chiTiet.moTa" class="detail-text">{{ chiTiet.moTa }}</p>
              <p v-else class="text-muted" style="font-style:italic">Chưa có mô tả.</p>
            </div>
          </div>

          <div class="card mb-4">
            <div class="card-header">
              <h3 class="card-title">Thuyết minh & tài liệu</h3>
            </div>
            <div class="card-body">
              <div v-if="canEditSubmission" class="upload-panel mb-4">
                <div>
                  <div class="upload-title">Bản thuyết minh đề tài</div>
                  <p class="upload-desc">Tải lên file PDF hoặc DOCX, tối đa 20MB/file trước khi gửi hồ sơ.</p>
                </div>
                <label class="btn btn-secondary flex items-center gap-2" :class="{ disabled: uploading }">
                  <span v-if="uploading" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
                  <Paperclip v-else :size="16" />
                  {{ uploading ? 'Đang tải...' : 'Chọn file' }}
                  <input type="file" accept=".pdf,.docx" multiple hidden :disabled="uploading" @change="onUploadThuyetMinh" />
                </label>
              </div>

              <div v-if="!taiLieu.length" class="empty-docs">
                <FileText :size="28" />
                <span>Chưa có tài liệu nào được tải lên.</span>
              </div>
              <div v-else class="document-list">
                <div v-for="tl in taiLieu" :key="tl.id" class="document-item">
                  <div class="document-icon"><FileText :size="16" /></div>
                  <div class="document-main">
                    <div class="document-name">{{ tl.tenFile }}</div>
                    <div class="document-meta">{{ tl.loai }} · {{ tl.size || '—' }} · {{ fmt(tl.uploadedAt, true) }}</div>
                  </div>
                  <div class="document-actions">
                    <a class="btn btn-ghost btn-sm" :href="tl.downloadUrl || '#'" target="_blank">Xem</a>
                    <button
                      v-if="canXoaTaiLieuPreview"
                      type="button"
                      class="btn btn-ghost btn-sm btn-danger-lite"
                      @click="xoaTaiLieu(tl.id)"
                    >
                      <Trash2 :size="14" /> Xóa
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <h3 class="card-title">Lịch sử xử lý</h3>
            </div>
            <div class="card-body">
              <div v-if="auditItems.length" class="timeline">
                <div v-for="(ev, i) in auditItems" :key="i" class="timeline-item">
                  <div class="timeline-dot active">{{ i === 0 ? '●' : '○' }}</div>
                  <div class="timeline-body">
                    <div class="timeline-title">{{ ev.action ?? ev.hanhDong }}</div>
                    <div class="timeline-time">{{ ev.actor ?? ev.nguoiThucHien }} · {{ fmt(ev.createdAt ?? ev.thoiGian, true) }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-docs">
                <Clock :size="24" />
                <span>Chưa có lịch sử xử lý.</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-sidebar">
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
                  <dd class="mono">{{ fmtM(chiTiet.kinhPhi) }}</dd>
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

          <div class="card">
            <div class="card-header">
              <h3 class="card-title">Thao tác</h3>
            </div>
            <div class="card-body sidebar-actions">
              <button
                v-if="canUploadThuyetMinh"
                class="btn btn-primary w-full flex items-center justify-center gap-2"
                :disabled="actionLoading.guiHoSo || !canGuiHoSo"
                @click="guiHoSo"
              >
                <Send :size="16" /> Gửi hồ sơ tới P.NCKH
              </button>
              <button
                v-if="canBoSung"
                class="btn btn-secondary w-full flex items-center justify-center gap-2"
                @click="router.push(`/gv/de-tai/${chiTiet.id}/bo-sung`)"
              >
                <Paperclip :size="16" /> Bổ sung hồ sơ
              </button>
              <button
                v-if="canXemHopDong"
                class="btn btn-secondary w-full flex items-center justify-center gap-2"
                @click="router.push(`/gv/de-tai/${chiTiet.id}/hop-dong`)"
              >
                <FileText :size="16" /> Xem hợp đồng
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.action-bar {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-main,
.detail-sidebar {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.detail-text {
  font: var(--text-body-lg);
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  line-height: 1.7;
}

.upload-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface-alt);
}

.upload-title {
  font: var(--text-h4);
  color: var(--color-text-primary);
}

.upload-desc {
  font: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: 2px;
}

.document-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.document-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.document-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--color-accent-light);
  color: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.document-main {
  flex: 1;
  min-width: 0;
}

.document-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.document-name {
  font: var(--text-body);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-meta {
  font: var(--text-caption);
  color: var(--color-text-muted);
  margin-top: 2px;
}

.empty-docs {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: 88px;
  color: var(--color-text-muted);
  font: var(--text-body);
  text-align: center;
}

.info-dl {
  display: flex;
  flex-direction: column;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  gap: var(--space-3);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row dt {
  font: var(--text-sm);
  color: var(--color-text-muted);
  white-space: nowrap;
}

.info-row dd {
  font: var(--text-body);
  color: var(--color-text-primary);
  text-align: right;
}

.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.btn-link {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-accent);
  font: inherit;
  font-weight: 600;
  padding: 0;
}

.btn-link:hover {
  color: var(--color-accent-hover);
}

.disabled {
  opacity: 0.7;
  pointer-events: none;
}

.btn-danger-lite {
  color: var(--color-danger);
}

.btn-danger-lite:hover {
  background: var(--color-danger-bg, #fff0f0);
}

@media (max-width: 768px) {
  .upload-panel,
  .document-item {
    align-items: stretch;
    flex-direction: column;
  }

  .action-bar {
    width: 100%;
  }

  .action-bar .btn {
    width: 100%;
    justify-content: center;
  }

  .document-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
