<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useNckhStore } from '@/stores/nckh.store'
import { useToast } from '@/composables/useToast'
import api from '@/api/axios'
import StatusBadge from '@/components/StatusBadge.vue'
import EmptyState from '@/components/EmptyState.vue'
import {
  allPbSubmitted as areAllPbSubmitted,
  hasOpenContractFeedback,
  normalizeContractStatus,
} from '@/utils/sopDGuards'
import {
  AlertCircle,
  ArrowLeft,
  Calendar,
  CheckCircle,
  ClipboardList,
  FileText,
  MessageSquareText,
  Send,
  Upload,
  Users,
  X,
  XCircle,
} from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const store = useNckhStore()
const toast = useToast()
const { chiTiet, loading, error, canActions, actionLoading } = storeToRefs(store)

const showYeuCauDialog = ref(false)
const showTuChoiDialog = ref(false)
const showPBDialog = ref(false)
const showXetDuyetDialog = ref(false)
const showKyDialog = ref(false)

const yeuCauForm = ref({ noiDung: '', deadlinePhanHoi: '' })
const tuChoiForm = ref({ lyDo: '' })
const pbForm = ref({ thanhVienIds: [], deadlineNop: '' })
const xetDuyetForm = ref({ quyetDinh: 'CHAP_NHAN', ghiChu: '', noiDung: '', deadlinePhanHoi: '' })
const kyForm = ref({ fileScan: null, ngayKy: new Date().toISOString().slice(0, 10) })
const todayIso = new Date().toISOString().slice(0, 10)

onMounted(async () => {
  try {
    await store.layChiTiet(route.params.id)
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Không thể tải chi tiết', detail: e.response?.data?.message ?? error.value })
  }
})

const pbMembers = ref([])

// Lazy-load danh sách phản biện từ API khi mở dialog lập tổ PB
watch(showPBDialog, async (v) => {
  if (v && !pbMembers.value.length) {
    try {
      const res = await api.get('/users')
      const list = Array.isArray(res.data) ? res.data : (res.data?.content ?? [])
      pbMembers.value = list.filter(u => u.role === 'TO_PHAN_BIEN')
    } catch {
      pbMembers.value = []
    }
  }
})
const terminalState = computed(() => ['BI_TU_CHOI', 'DANG_THUC_HIEN', 'HOAN_TAT', 'DA_HUY', 'DA_RUT'].includes(chiTiet.value?.trangThai))
const chuNhiemName = computed(() => chiTiet.value?.giangVien?.hoTen ?? chiTiet.value?.chuNhiem ?? '---')
const pbResults = computed(() => chiTiet.value?.toPhanBien ?? [])
const pbSubmittedCount = computed(() => pbResults.value.filter(pb => pb.ketQua && String(pb.nhanXet ?? '').trim()).length)
const pbTotalCount = computed(() => pbResults.value.length)
const allPbSubmitted = computed(() => areAllPbSubmitted(pbResults.value))
const pbAcceptBlocked = computed(() => xetDuyetForm.value.quyetDinh === 'CHAP_NHAN' && !allPbSubmitted.value)
const contractStatus = computed(() => normalizeContractStatus(chiTiet.value))
const contractHasFeedback = computed(() => hasOpenContractFeedback(chiTiet.value))
const contractNeedsRevision = computed(() => ['CAN_SUA', 'YEU_CAU_SUA'].includes(contractStatus.value))
const contractStatusLabel = computed(() => {
  const labels = {
    CHUA_SOAN: 'Chưa soạn hợp đồng',
    CHO_GV_XEM: 'Chờ GV phản hồi',
    CHO_PHAN_HOI: 'Chờ GV phản hồi',
    CAN_SUA: 'GV yêu cầu chỉnh sửa',
    YEU_CAU_SUA: 'GV yêu cầu chỉnh sửa',
    CHO_KY: 'GV đã đồng ý, chờ ký',
    DA_KY: 'Đã ký hợp đồng',
  }
  return labels[contractStatus.value] ?? 'Chưa có trạng thái hợp đồng'
})
const contractNextStep = computed(() => {
  const steps = {
    CHUA_SOAN: 'Kết quả phản biện đã được chấp nhận. P.NCKH cần soạn dự thảo hợp đồng để gửi giảng viên xem xét.',
    CHO_GV_XEM: 'Dự thảo đã gửi sang giảng viên. Đang chờ giảng viên đồng ý hoặc gửi yêu cầu chỉnh sửa.',
    CHO_PHAN_HOI: 'Dự thảo đã gửi sang giảng viên. Đang chờ giảng viên đồng ý hoặc gửi yêu cầu chỉnh sửa.',
    CAN_SUA: 'Giảng viên đã gửi yêu cầu chỉnh sửa. P.NCKH cần cập nhật hợp đồng và gửi lại cho giảng viên xem.',
    YEU_CAU_SUA: 'Giảng viên đã gửi yêu cầu chỉnh sửa. P.NCKH cần cập nhật hợp đồng và gửi lại cho giảng viên xem.',
    CHO_KY: 'Giảng viên đã đồng ý nội dung hợp đồng. P.NCKH tải file scan đã ký và xác nhận để chuyển đề tài sang thực hiện.',
    DA_KY: 'Hợp đồng đã hoàn tất, đề tài đã chuyển sang giai đoạn thực hiện.',
  }
  return steps[contractStatus.value] ?? ''
})
const pbReviewHint = computed(() => {
  if (chiTiet.value?.trangThai !== 'DANG_PHAN_BIEN') return ''
  if (!pbResults.value.length) return 'Chưa lập tổ phản biện cho đề tài này.'
  if (!allPbSubmitted.value) return 'Còn thành viên chưa nộp đủ điểm và nhận xét, nên chưa thể chấp nhận để lập hợp đồng.'
  return 'Tổ phản biện đã nộp đủ kết quả. P.NCKH có thể chấp nhận kết quả để chuyển đề tài sang bước lập hợp đồng.'
})
const hasAnyAction = computed(() =>
  !terminalState.value && Object.entries(canActions.value ?? {}).some(([key, val]) => key !== 'dongYHopDong' && val)
)

const yeuCauErrors = computed(() => ({
  noiDung: yeuCauForm.value.noiDung.trim().length > 0 && yeuCauForm.value.noiDung.trim().length < 20 ? 'Nội dung cần ít nhất 20 ký tự.' : '',
  deadlinePhanHoi: '',
}))
const canSubmitYeuCau = computed(() => yeuCauForm.value.noiDung.trim().length >= 20 && Boolean(yeuCauForm.value.deadlinePhanHoi))
const canSubmitPB = computed(() => pbForm.value.thanhVienIds.length >= 2 && Boolean(pbForm.value.deadlineNop))
const canSubmitTuChoi = computed(() => tuChoiForm.value.lyDo.trim().length >= 10)
const canSubmitXetDuyet = computed(() => {
  if (pbAcceptBlocked.value) return false
  if (xetDuyetForm.value.quyetDinh === 'CAN_SUA') {
    return xetDuyetForm.value.noiDung.trim().length >= 20 && Boolean(xetDuyetForm.value.deadlinePhanHoi)
  }
  if (xetDuyetForm.value.quyetDinh === 'TU_CHOI') return xetDuyetForm.value.ghiChu.trim().length >= 10
  return true
})
const canSubmitKy = computed(() => Boolean(kyForm.value.fileScan) && Boolean(kyForm.value.ngayKy) && kyForm.value.ngayKy <= todayIso)

function apiMessage(e, fallback) {
  return e.response?.data?.message ?? e.message ?? fallback
}

async function doAction(successSummary, fallbackError, fn, after) {
  try {
    await fn()
    toast.add({ severity: 'success', summary: successSummary })
    after?.()
  } catch (e) {
    toast.add({ severity: 'error', summary: fallbackError, detail: apiMessage(e, fallbackError) })
  }
}

function tiepNhan() {
  doAction('Đã tiếp nhận hồ sơ', 'Tiếp nhận thất bại', () => store.tiepNhan(route.params.id))
}


function submitYeuCau() {
  if (!canSubmitYeuCau.value) return
  doAction(
    'Đã gửi yêu cầu bổ sung',
    'Gửi yêu cầu thất bại',
    () => store.yeuCauBoSung(route.params.id, { ...yeuCauForm.value, noiDung: yeuCauForm.value.noiDung.trim() }),
    () => {
      showYeuCauDialog.value = false
      yeuCauForm.value = { noiDung: '', deadlinePhanHoi: '' }
    },
  )
}

function submitTuChoi() {
  if (!canSubmitTuChoi.value) return
  doAction(
    'Đã từ chối hồ sơ',
    'Từ chối hồ sơ thất bại',
    () => store.tuChoiHoSo(route.params.id, { lyDo: tuChoiForm.value.lyDo.trim() }),
    () => {
      showTuChoiDialog.value = false
      tuChoiForm.value = { lyDo: '' }
    },
  )
}

function submitLapPB() {
  if (!canSubmitPB.value) return
  doAction(
    'Đã lập tổ phản biện',
    'Lập tổ phản biện thất bại',
    () => store.lapToPhanBien(route.params.id, { ...pbForm.value }),
    () => {
      showPBDialog.value = false
      pbForm.value = { thanhVienIds: [], deadlineNop: '' }
    },
  )
}

function submitXetDuyetPB() {
  if (!canSubmitXetDuyet.value) return
  const decision = xetDuyetForm.value.quyetDinh
  const successSummary = decision === 'CHAP_NHAN'
    ? 'Đã duyệt phản biện, chuyển sang lập hợp đồng'
    : decision === 'CAN_SUA'
      ? 'Đã yêu cầu chỉnh sửa thuyết minh'
      : 'Đã từ chối đề tài sau phản biện'
  doAction(
    successSummary,
    'Xét duyệt phản biện thất bại',
    () => store.xetDuyetPB(route.params.id, {
      ...xetDuyetForm.value,
      ghiChu: xetDuyetForm.value.ghiChu.trim(),
      noiDung: xetDuyetForm.value.noiDung.trim(),
    }),
    () => {
      showXetDuyetDialog.value = false
      xetDuyetForm.value = { quyetDinh: 'CHAP_NHAN', ghiChu: '', noiDung: '', deadlinePhanHoi: '' }
    },
  )
}

function onFileScanChange(event) {
  kyForm.value.fileScan = event.target.files?.[0] ?? null
}

function submitKyHopDong() {
  if (!canSubmitKy.value) return
  doAction(
    'Đã xác nhận ký hợp đồng',
    'Ký hợp đồng thất bại',
    () => store.kyHopDong(route.params.id, kyForm.value),
    () => {
      showKyDialog.value = false
      kyForm.value = { fileScan: null, ngayKy: new Date().toISOString().slice(0, 10) }
    },
  )
}

function fmt(iso) {
  return iso ? new Date(iso).toLocaleString('vi-VN') : '---'
}

function fmtDate(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '---'
}

function fmtM(n) {
  return n ? `${Number(n).toLocaleString('vi-VN')} đ` : '---'
}
</script>

<template>
  <div class="page-container">
    <button class="btn btn-ghost btn-sm mb-4 flex items-center gap-2" @click="router.back()">
      <ArrowLeft :size="16" /> Quay lại
    </button>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton" style="height:76px;border-radius:8px"></div>
      <div class="skeleton" style="height:220px;border-radius:8px"></div>
    </div>

    <div v-else-if="error" class="alert alert-danger">
      <AlertCircle :size="18" /> {{ error }}
    </div>

    <template v-else-if="chiTiet">
      <div class="page-header">
        <div class="page-header-left">
          <div class="flex items-center gap-3 mb-2">
            <StatusBadge :status="chiTiet.trangThai" />
            <span class="mono text-muted text-sm">{{ chiTiet.maSo }}</span>
          </div>
          <h1 class="page-title">{{ chiTiet.tenDeTai }}</h1>
          <p class="page-subtitle">Chủ nhiệm: {{ chuNhiemName }}</p>
        </div>

        <div v-if="hasAnyAction" class="action-bar">
          <button
            v-if="canActions.tiepNhan"
            class="btn btn-primary"
            :disabled="actionLoading.tiepNhan"
            @click="tiepNhan"
          >
            <span v-if="actionLoading.tiepNhan" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <CheckCircle v-else :size="16" /> Tiếp nhận
          </button>
          <button v-if="canActions.yeuCauBoSung" class="btn btn-warning" @click="showYeuCauDialog = true">
            <ClipboardList :size="16" /> Yêu cầu bổ sung
          </button>
          <button v-if="canActions.tuChoiHoSo" class="btn btn-danger" @click="showTuChoiDialog = true">
            <XCircle :size="16" /> Từ chối
          </button>
          <button v-if="canActions.lapToPhanBien" class="btn btn-secondary" @click="showPBDialog = true">
            <Users :size="16" /> Lập tổ PB
          </button>
          <button v-if="canActions.xetDuyetPB" class="btn btn-primary" @click="showXetDuyetDialog = true">
            <ClipboardList :size="16" /> Xét duyệt PB
          </button>
          <button v-if="canActions.soanHopDong" class="btn btn-secondary" @click="router.push(`/nckh/de-tai/${chiTiet.id}/hop-dong`)">
            <FileText :size="16" /> {{ contractNeedsRevision ? 'Chỉnh sửa HĐ' : 'Soạn HĐ' }}
          </button>
          <button v-if="canActions.kyHopDong" class="btn btn-success" @click="showKyDialog = true">
            <Upload :size="16" /> Xác nhận ký HĐ
          </button>
        </div>
      </div>

      <div v-if="terminalState" class="alert alert-info mb-4">
        <AlertCircle :size="18" /> Đề tài đang ở trạng thái kết thúc. Màn hình chỉ hiển thị thông tin và lịch sử xử lý.
      </div>

      <!-- FIXED(TL): Bổ sung HOAN_TAT để hiển thị thập hợp đồng cả khi đề tài đã hoàn tất -->
      <section v-if="['DANG_LAP_HOP_DONG', 'DANG_THUC_HIEN', 'HOAN_TAT'].includes(chiTiet.trangThai)" class="contract-status-card mb-4">
        <div class="contract-status-main">
          <span class="contract-status-icon"><FileText :size="18" /></span>
          <div>
            <h3 class="detail-card-title">Trạng thái hợp đồng</h3>
            <p class="detail-desc">{{ contractStatusLabel }}</p>
            <p v-if="contractNextStep" class="contract-next-step">{{ contractNextStep }}</p>
          </div>
        </div>
        <div class="contract-status-actions">
          <span class="badge" :class="contractStatus === 'DA_KY' ? 'badge-success' : contractNeedsRevision ? 'badge-warning' : 'badge-info'">
            {{ contractStatusLabel }}
          </span>
        </div>
      </section>

      <section v-if="contractHasFeedback" class="detail-card feedback-panel mb-4">
        <h3 class="detail-card-title flex items-center gap-2">
          <MessageSquareText :size="18" /> Phản hồi hợp đồng từ GV
        </h3>
        <p class="detail-desc">{{ chiTiet.hopDongFeedback?.noiDung || 'GV đã yêu cầu chỉnh sửa hợp đồng.' }}</p>
        <p class="text-muted text-sm mt-2">
          {{ chiTiet.hopDongFeedback?.actor || 'Giảng viên' }} - {{ fmt(chiTiet.hopDongFeedback?.createdAt) }}
        </p>
      </section>

      <div class="detail-grid">
        <section class="detail-card">
          <h3 class="detail-card-title">Thông tin đề tài</h3>
          <dl class="info-list">
            <dt>Chủ nhiệm</dt><dd>{{ chuNhiemName }}</dd>
            <dt>Kỳ NCKH</dt><dd>{{ chiTiet.kyNckh || '---' }}</dd>
            <dt>Lĩnh vực</dt><dd>{{ chiTiet.linhVuc || '---' }}</dd>
            <dt>Kinh phí</dt><dd>{{ fmtM(chiTiet.kinhPhi) }}</dd>
            <dt>Cập nhật</dt><dd>{{ fmt(chiTiet.updatedAt) }}</dd>
          </dl>
        </section>

        <section class="detail-card">
          <h3 class="detail-card-title">Mô tả</h3>
          <p class="detail-desc">{{ chiTiet.moTa || 'Chưa có mô tả.' }}</p>
        </section>
      </div>

      <section class="detail-card mt-4">
        <h3 class="detail-card-title">Hồ sơ và tài liệu</h3>
        <div v-if="chiTiet.taiLieu?.length" class="doc-list">
          <a v-for="doc in chiTiet.taiLieu" :key="doc.id" class="doc-item" :href="doc.downloadUrl || '#'" target="_blank">
            <FileText :size="16" />
            <span>{{ doc.tenFile || doc.name }}</span>
            <small>{{ doc.loai }} - {{ doc.size || '---' }}</small>
          </a>
        </div>
        <EmptyState v-else :icon="FileText" title="Chưa có tài liệu" />
      </section>

      <section v-if="chiTiet.yeuCauBoSung" class="detail-card mt-4">
        <h3 class="detail-card-title">Yêu cầu bổ sung đang chờ xử lý</h3>
        <p class="detail-desc">{{ chiTiet.yeuCauBoSung.noiDung }}</p>
        <p class="text-muted text-sm mt-2">
          <Calendar :size="14" class="inline-icon" /> Deadline: {{ fmtDate(chiTiet.yeuCauBoSung.deadlinePhanHoi ?? chiTiet.yeuCauBoSung.deadline) }}
        </p>
      </section>

      <section v-if="pbResults.length" class="detail-card mt-4">
        <h3 class="detail-card-title">Tổ phản biện và kết quả</h3>
        <div class="pb-review-status mb-4" :class="{ warning: !allPbSubmitted }">
          <span class="badge" :class="allPbSubmitted ? 'badge-success' : 'badge-warning'">
            Đã nộp: {{ pbSubmittedCount }}/{{ pbTotalCount }}
          </span>
          <span>{{ pbReviewHint }}</span>
        </div>
        <div class="table-wrapper compact-table">
          <table class="data-table">
            <thead>
              <tr>
                <th>Thành viên</th>
                <th>Kết quả</th>
                <th>Điểm tổng</th>
                <th>Nhận xét</th>
                <th>Ngày nộp</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pb in pbResults" :key="pb.id">
                <td>{{ pb.hoTen }}</td>
                <td>
                  <span class="badge" :class="pb.ketQua ? 'badge-success' : 'badge-warning'">
                    {{ pb.ketQua || 'Chưa nộp' }}
                  </span>
                </td>
                <td>{{ pb.diemTong ?? '---' }}</td>
                <td class="review-text">{{ pb.nhanXet || '---' }}</td>
                <td>{{ fmtDate(pb.submittedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="detail-card mt-4">
        <h3 class="detail-card-title">Lịch sử xử lý</h3>
        <div v-if="chiTiet.auditLog?.length" class="timeline">
          <div v-for="(log, index) in chiTiet.auditLog" :key="`${log.createdAt}-${index}`" class="timeline-item">
            <div class="timeline-dot active">{{ index + 1 }}</div>
            <div class="timeline-body">
              <div class="timeline-title">{{ log.action }}</div>
              <div class="timeline-time">{{ log.actor }} - {{ fmt(log.createdAt) }}</div>
            </div>
          </div>
        </div>
        <EmptyState v-else :icon="ClipboardList" title="Chưa có lịch sử xử lý" />
      </section>
    </template>

    <Teleport to="body">
      <div v-if="showYeuCauDialog" class="dialog-overlay" @click.self="!actionLoading.yeuCauBoSung && (showYeuCauDialog = false)">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Yêu cầu bổ sung hồ sơ</h3>
            <button class="dialog-close btn btn-ghost btn-sm" :disabled="actionLoading.yeuCauBoSung" @click="showYeuCauDialog = false"><X :size="18" /></button>
          </div>
          <div class="dialog-body">
            <div class="form-group">
              <label class="form-label">Nội dung yêu cầu <span class="required">*</span></label>
              <textarea v-model="yeuCauForm.noiDung" class="form-textarea" :class="{ 'is-error': yeuCauErrors.noiDung }" rows="5" placeholder="Mô tả rõ tài liệu hoặc thông tin cần bổ sung..."></textarea>
              <span v-if="yeuCauErrors.noiDung" class="form-error">{{ yeuCauErrors.noiDung }}</span>
            </div>
            <div class="form-group">
              <label class="form-label">Hạn phản hồi <span class="required">*</span></label>
              <input v-model="yeuCauForm.deadlinePhanHoi" type="date" class="form-input" />
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" :disabled="actionLoading.yeuCauBoSung" @click="showYeuCauDialog = false">Hủy</button>
            <button class="btn btn-warning" :disabled="!canSubmitYeuCau || actionLoading.yeuCauBoSung" @click="submitYeuCau">
              <span v-if="actionLoading.yeuCauBoSung" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              <Send v-else :size="16" /> Gửi yêu cầu
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showTuChoiDialog" class="dialog-overlay" @click.self="!actionLoading.tuChoiHoSo && (showTuChoiDialog = false)">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Từ chối hồ sơ thẩm</h3>
            <button class="dialog-close btn btn-ghost btn-sm" :disabled="actionLoading.tuChoiHoSo" @click="showTuChoiDialog = false"><X :size="18" /></button>
          </div>
          <div class="dialog-body">
            <div class="form-group">
              <label class="form-label">Lý do từ chối <span class="required">*</span></label>
              <textarea v-model="tuChoiForm.lyDo" class="form-textarea" rows="4" placeholder="Nhập lý do rõ ràng để giảng viên nắm được kết quả sơ thẩm..."></textarea>
              <span v-if="tuChoiForm.lyDo && !canSubmitTuChoi" class="form-error">Lý do cần ít nhất 10 ký tự.</span>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" :disabled="actionLoading.tuChoiHoSo" @click="showTuChoiDialog = false">Hủy</button>
            <button class="btn btn-danger" :disabled="!canSubmitTuChoi || actionLoading.tuChoiHoSo" @click="submitTuChoi">
              <span v-if="actionLoading.tuChoiHoSo" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              <XCircle v-else :size="16" />
              {{ actionLoading.tuChoiHoSo ? 'Đang gửi...' : 'Xác nhận từ chối' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showPBDialog" class="dialog-overlay" @click.self="!actionLoading.lapToPhanBien && (showPBDialog = false)">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Lập tổ phản biện</h3>
            <button class="dialog-close btn btn-ghost btn-sm" :disabled="actionLoading.lapToPhanBien" @click="showPBDialog = false"><X :size="18" /></button>
          </div>
          <div class="dialog-body">
            <p class="text-muted text-sm mb-4">Chọn ít nhất 2 thành viên và thiết lập hạn nộp kết quả.</p>
            <div class="pb-member-list">
              <label v-for="pb in pbMembers" :key="pb.id" class="pb-member-item">
                <input type="checkbox" :value="pb.id" v-model="pbForm.thanhVienIds" />
                <span class="pb-member-name">{{ pb.hoTen }}</span>
                <span class="text-muted text-sm">{{ pb.khoa }}</span>
              </label>
            </div>
            <span v-if="pbForm.thanhVienIds.length > 0 && pbForm.thanhVienIds.length < 2" class="form-error">Cần chọn ít nhất 2 người.</span>
            <div class="form-group mt-4">
              <label class="form-label">Hạn nộp kết quả <span class="required">*</span></label>
              <input v-model="pbForm.deadlineNop" type="date" class="form-input" />
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" :disabled="actionLoading.lapToPhanBien" @click="showPBDialog = false">Hủy</button>
            <button class="btn btn-primary" :disabled="!canSubmitPB || actionLoading.lapToPhanBien" @click="submitLapPB">
              <span v-if="actionLoading.lapToPhanBien" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showXetDuyetDialog" class="dialog-overlay" @click.self="!actionLoading.xetDuyetPB && (showXetDuyetDialog = false)">
        <div class="dialog wide-dialog">
          <div class="dialog-header">
            <h3>Xét duyệt kết quả phản biện</h3>
            <button class="dialog-close btn btn-ghost btn-sm" :disabled="actionLoading.xetDuyetPB" @click="showXetDuyetDialog = false"><X :size="18" /></button>
          </div>
          <div class="dialog-body">
            <div class="pb-review-status mb-4" :class="{ warning: !allPbSubmitted }">
              <span class="badge" :class="allPbSubmitted ? 'badge-success' : 'badge-warning'">
                Đã nộp: {{ pbSubmittedCount }}/{{ pbTotalCount }}
              </span>
              <span>{{ pbReviewHint }}</span>
            </div>

            <div class="table-wrapper compact-table mb-4">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Thành viên PB</th>
                    <th>Trạng thái</th>
                    <th>Điểm tổng</th>
                    <th>Nhận xét chi tiết</th>
                    <th>Ngày nộp</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="pb in pbResults" :key="pb.id">
                    <td>{{ pb.hoTen }}</td>
                    <td>
                      <span class="badge" :class="pb.ketQua ? 'badge-success' : 'badge-warning'">
                        {{ pb.ketQua || 'Chưa nộp' }}
                      </span>
                    </td>
                    <td>{{ pb.diemTong ?? '---' }}</td>
                    <td class="review-text">{{ pb.nhanXet || '---' }}</td>
                    <td>{{ fmtDate(pb.submittedAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="form-group">
              <label class="form-label">Quyết định <span class="required">*</span></label>
              <select v-model="xetDuyetForm.quyetDinh" class="form-select">
                <option value="CHAP_NHAN">Chấp nhận - chuyển sang lập hợp đồng</option>
                <option value="CAN_SUA">Yêu cầu chỉnh sửa thuyết minh</option>
                <option value="TU_CHOI">Từ chối đề tài</option>
              </select>
            </div>

            <div v-if="xetDuyetForm.quyetDinh === 'CAN_SUA'" class="form-group">
              <label class="form-label">Nội dung cần chỉnh sửa <span class="required">*</span></label>
              <textarea v-model="xetDuyetForm.noiDung" class="form-textarea" rows="4" placeholder="Nhập nội dung chỉnh sửa thuyết minh..."></textarea>
              <span v-if="xetDuyetForm.noiDung && xetDuyetForm.noiDung.trim().length < 20" class="form-error">Nội dung cần ít nhất 20 ký tự.</span>
            </div>
            <div v-if="xetDuyetForm.quyetDinh === 'CAN_SUA'" class="form-group">
              <label class="form-label">Hạn phản hồi <span class="required">*</span></label>
              <input v-model="xetDuyetForm.deadlinePhanHoi" type="date" class="form-input" />
            </div>

            <div class="form-group">
              <label class="form-label">{{ xetDuyetForm.quyetDinh === 'TU_CHOI' ? 'Lý do từ chối' : 'Ghi chú' }}</label>
              <textarea v-model="xetDuyetForm.ghiChu" class="form-textarea" rows="3" placeholder="Ghi chú tổng hợp..."></textarea>
              <span v-if="xetDuyetForm.quyetDinh === 'TU_CHOI' && xetDuyetForm.ghiChu && xetDuyetForm.ghiChu.trim().length < 10" class="form-error">Lý do cần ít nhất 10 ký tự.</span>
              <span v-if="pbAcceptBlocked" class="form-error">Không thể chấp nhận khi còn thành viên phản biện chưa nộp đủ kết quả.</span>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" :disabled="actionLoading.xetDuyetPB" @click="showXetDuyetDialog = false">Hủy</button>
            <button class="btn btn-primary" :disabled="!canSubmitXetDuyet || actionLoading.xetDuyetPB" @click="submitXetDuyetPB">
              <span v-if="actionLoading.xetDuyetPB" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showKyDialog" class="dialog-overlay" @click.self="!actionLoading.kyHopDong && (showKyDialog = false)">
        <div class="dialog">
          <div class="dialog-header">
            <h3>Xác nhận ký hợp đồng</h3>
            <button class="dialog-close btn btn-ghost btn-sm" :disabled="actionLoading.kyHopDong" @click="showKyDialog = false"><X :size="18" /></button>
          </div>
          <div class="dialog-body">
            <div class="form-group">
              <label class="form-label">File scan hợp đồng đã ký (PDF) <span class="required">*</span></label>
              <input type="file" class="form-input" accept="application/pdf,.pdf" @change="onFileScanChange" />
              <span v-if="kyForm.fileScan" class="text-muted text-sm">{{ kyForm.fileScan.name }}</span>
            </div>
            <div class="form-group">
              <label class="form-label">Ngày ký <span class="required">*</span></label>
              <input v-model="kyForm.ngayKy" type="date" class="form-input" :max="todayIso" />
              <span v-if="kyForm.ngayKy && kyForm.ngayKy > todayIso" class="form-error">Ngày ký không được nằm trong tương lai.</span>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-secondary" :disabled="actionLoading.kyHopDong" @click="showKyDialog = false">Hủy</button>
            <button class="btn btn-success" :disabled="!canSubmitKy || actionLoading.kyHopDong" @click="submitKyHopDong">
              <span v-if="actionLoading.kyHopDong" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
              Xác nhận ký
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.detail-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.action-bar {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: flex-end;
}

.detail-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: var(--space-4);
}

.detail-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}

.detail-card-title {
  font: var(--text-h4);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
}

.contract-status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
}

.contract-status-main {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.contract-status-icon {
  width: 36px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  color: var(--color-accent);
  background: color-mix(in srgb, var(--color-accent) 10%, transparent);
  flex-shrink: 0;
}

.contract-next-step {
  margin-top: var(--space-1);
  font: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
  max-width: 720px;
}

.contract-status-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
  flex-wrap: wrap;
  flex-shrink: 0;
}

.feedback-panel {
  border-color: color-mix(in srgb, var(--color-warning) 35%, var(--color-border));
  background: color-mix(in srgb, var(--color-warning) 7%, var(--color-surface));
}

.info-list {
  display: grid;
  grid-template-columns: auto 1fr;
  row-gap: var(--space-3);
  column-gap: var(--space-4);
  align-items: start;
}

.info-list dt {
  font: var(--text-sm);
  color: var(--color-text-muted);
}

.info-list dd {
  font: var(--text-body);
  color: var(--color-text-primary);
  min-width: 0;
}

.detail-desc {
  font: var(--text-body);
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  line-height: 1.6;
}

.doc-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.doc-item {
  display: grid;
  grid-template-columns: 20px 1fr auto;
  gap: var(--space-2);
  align-items: center;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
}

.doc-item span,
.review-text {
  min-width: 0;
  overflow-wrap: anywhere;
}

.doc-item small {
  color: var(--color-text-muted);
}

.compact-table {
  overflow-x: auto;
}

.pb-review-status {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface-alt);
  color: var(--color-text-secondary);
  font: var(--text-sm);
}

.pb-review-status.warning {
  border-color: color-mix(in srgb, var(--color-warning) 40%, var(--color-border));
  background: color-mix(in srgb, var(--color-warning) 8%, var(--color-surface));
}

.pb-member-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.pb-member-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.pb-member-item:hover {
  background: var(--color-surface-alt);
}

.pb-member-name {
  flex: 1;
  color: var(--color-text-primary);
}

.pb-summary-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.pb-summary-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.wide-dialog {
  max-width: 720px;
}

.form-textarea.is-error {
  border-color: #EF4444;
  background: var(--color-error-bg);
}

.inline-icon {
  vertical-align: text-bottom;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .action-bar,
  .action-bar .btn,
  .contract-status-card,
  .dialog-footer,
  .dialog-footer .btn {
    width: 100%;
    justify-content: center;
  }

  .contract-status-card,
  .pb-review-status {
    align-items: flex-start;
    flex-direction: column;
  }

  .contract-status-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .doc-item,
  .pb-summary-item {
    grid-template-columns: 1fr;
  }
}
</style>
