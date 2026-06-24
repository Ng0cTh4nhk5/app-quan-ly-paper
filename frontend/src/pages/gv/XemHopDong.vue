<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDetaiStore } from '@/stores/detai.store'
import { useToast } from '@/composables/useToast'
import { AlertTriangle, ArrowLeft, Check, CheckCircle2, Hourglass, MessageSquareText, PenTool, Printer, X } from '@lucide/vue'

const route = useRoute()
const router = useRouter()
const store = useDetaiStore()
const { add: toastAdd } = useToast()
const { chiTiet, loading, error } = storeToRefs(store)
const agreeing = ref(false)
const agreed = ref(false)
const showSignDialog = ref(false)
const feedbackSubmitting = ref(false)
const showFeedbackDialog = ref(false)
const feedbackNoiDung = ref('')
const feedbackError = ref('')

onMounted(() => store.layChiTiet(route.params.id).catch(() => {}))

const hasPendingFeedback = computed(() => Boolean(chiTiet.value?.hopDongFeedback))

const canDongY = computed(() =>
  chiTiet.value?.trangThai === 'DANG_LAP_HOP_DONG' &&
  !agreed.value &&
  !chiTiet.value?.gvDaDongYHopDong &&
  !hasPendingFeedback.value
)

const canYeuCauChinhSua = computed(() =>
  chiTiet.value?.trangThai === 'DANG_LAP_HOP_DONG' &&
  !agreed.value &&
  !chiTiet.value?.gvDaDongYHopDong &&
  !hasPendingFeedback.value
)

async function dongYHopDong() {
  if (!showSignDialog.value) {
    showSignDialog.value = true
    return
  }
  agreeing.value = true
  try {
    await store.dongYHopDong(route.params.id, {
      soHopDong: chiTiet.value?.soHopDong,
    })
    agreed.value = true
    showSignDialog.value = false
    toastAdd({
      severity: 'success',
      summary: 'Đã đồng ý hợp đồng',
      detail: 'P.NCKH có thể tiến hành in, ký và xác nhận hợp đồng.',
    })
  } catch (e) {
    toastAdd({
      severity: 'error',
      summary: 'Đồng ý thất bại',
      detail: e.response?.data?.message ?? 'Không thể đồng ý hợp đồng.',
    })
  } finally {
    agreeing.value = false
  }
}

async function yeuCauChinhSuaHopDong() {
  const noiDung = feedbackNoiDung.value.trim()
  feedbackError.value = ''

  if (noiDung.length < 10) {
    feedbackError.value = 'Vui lòng nhập nội dung góp ý ít nhất 10 ký tự.'
    return
  }

  feedbackSubmitting.value = true
  try {
    await store.yeuCauChinhSuaHopDong(route.params.id, { noiDung })
    showFeedbackDialog.value = false
    feedbackNoiDung.value = ''
    toastAdd({
      severity: 'success',
      summary: 'Đã gửi yêu cầu chỉnh sửa',
      detail: 'Phản hồi của bạn đã được gửi đến P.NCKH.',
    })
  } catch (e) {
    toastAdd({
      severity: 'error',
      summary: 'Gửi yêu cầu thất bại',
      detail: e.response?.data?.message ?? 'Không thể gửi yêu cầu chỉnh sửa hợp đồng.',
    })
  } finally {
    feedbackSubmitting.value = false
  }
}

function fmt(iso) {
  return iso ? new Date(iso).toLocaleDateString('vi-VN') : '—'
}

function fmtM(n) {
  return n ? `${n.toLocaleString('vi-VN')} đ` : '—'
}
</script>

<template>
  <div>
    <button class="btn btn-ghost btn-sm mb-4 flex items-center gap-2" @click="router.back()">
      <ArrowLeft size="16" /> Quay lại
    </button>

    <div v-if="agreed" class="alert-success mb-4 flex items-start gap-2">
      <CheckCircle2 size="20" class="shrink-0 mt-0.5" />
      <div>
        Bạn đã đồng ý với nội dung hợp đồng. P.NCKH sẽ tiến hành in, ký và xác nhận hợp đồng để chuyển đề tài sang giai đoạn thực hiện.
        <button class="btn btn-ghost btn-sm ml-4" @click="router.push('/gv/dashboard')">Về dashboard</button>
      </div>
    </div>

    <div v-if="loading" class="contract-loading">
      <div class="skeleton" style="height:60px;border-radius:8px"></div>
      <div class="skeleton" style="height:300px;border-radius:8px"></div>
    </div>

    <div v-else-if="error" class="alert alert-danger">
      {{ error }}
    </div>

    <div v-else-if="chiTiet" class="hop-dong-wrapper">
      <div class="page-header mb-4">
        <div class="page-header-left">
          <h1 class="page-title">Hợp đồng nghiên cứu khoa học</h1>
          <p class="page-subtitle mono">{{ chiTiet.soHopDong || chiTiet.maSo }}</p>
        </div>
        <div class="contract-actions">
          <button class="btn btn-secondary flex items-center gap-2" onclick="window.print()">
            <Printer size="16" /> In hợp đồng
          </button>
          <button
            v-if="canYeuCauChinhSua"
            class="btn btn-secondary flex items-center gap-2"
            :disabled="feedbackSubmitting"
            @click="showFeedbackDialog = true"
          >
            <span v-if="feedbackSubmitting" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <MessageSquareText v-else size="16" />
            Yêu cầu chỉnh sửa
          </button>
          <button
            v-if="canDongY"
            class="btn btn-primary flex items-center gap-2"
            :disabled="agreeing"
            @click="dongYHopDong"
          >
            <span v-if="agreeing" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <PenTool v-else size="16" />
            {{ agreeing ? 'Đang gửi...' : 'Đồng ý hợp đồng' }}
          </button>
          <span v-else-if="chiTiet.trangThai === 'DANG_THUC_HIEN'" class="badge badge-success flex items-center gap-1">
            <CheckCircle2 size="14" /> Đã ký
          </span>
          <span v-else-if="chiTiet.gvDaDongYHopDong" class="badge badge-info flex items-center gap-1">
            <CheckCircle2 size="14" /> Đã đồng ý
          </span>
        </div>
      </div>

      <div class="hop-dong-doc">
        <div class="hop-dong-head">
          <div class="seal-ring">HĐ</div>
          <div>
            <p class="hop-dong-school">TRƯỜNG ĐẠI HỌC - PHÒNG NGHIÊN CỨU KHOA HỌC</p>
            <h2 class="hop-dong-main-title">HỢP ĐỒNG NGHIÊN CỨU KHOA HỌC</h2>
            <p class="hop-dong-sub-title">Kỳ NCKH: {{ chiTiet.kyNckh || '—' }}</p>
          </div>
        </div>

        <div
          class="status-banner flex items-center justify-center gap-2"
          :class="chiTiet.trangThai === 'DANG_THUC_HIEN' ? 'signed' : 'pending'"
        >
          <template v-if="chiTiet.trangThai === 'DANG_THUC_HIEN'">
            <CheckCircle2 size="18" /> Hợp đồng đã có hiệu lực
          </template>
          <template v-else-if="hasPendingFeedback">
            <MessageSquareText size="18" /> Đã gửi phản hồi, đang chờ P.NCKH chỉnh sửa hợp đồng
          </template>
          <template v-else-if="chiTiet.gvDaDongYHopDong">
            <CheckCircle2 size="18" /> Chủ nhiệm đề tài đã đồng ý, chờ P.NCKH xác nhận ký
          </template>
          <template v-else>
            <Hourglass size="18" /> Đang chờ chủ nhiệm đề tài xem xét và phản hồi hợp đồng
          </template>
        </div>

        <div v-if="chiTiet.hopDongFeedback" class="feedback-banner">
          <div class="feedback-title">
            <MessageSquareText size="16" />
            Phản hồi chỉnh sửa đã gửi
          </div>
          <p>{{ chiTiet.hopDongFeedback.noiDung }}</p>
          <span>{{ fmt(chiTiet.hopDongFeedback.createdAt) }}</span>
        </div>

        <div class="contract-body">
          <section class="contract-section">
            <h3 class="section-title">I. THÔNG TIN ĐỀ TÀI</h3>
            <dl class="contract-dl">
              <div class="contract-row">
                <dt>Tên đề tài</dt>
                <dd>{{ chiTiet.tenDeTai }}</dd>
              </div>
              <div class="contract-row">
                <dt>Mã số</dt>
                <dd class="mono">{{ chiTiet.maSo }}</dd>
              </div>
              <div class="contract-row">
                <dt>Lĩnh vực</dt>
                <dd>{{ chiTiet.linhVuc || '—' }}</dd>
              </div>
              <div class="contract-row">
                <dt>Kỳ NCKH</dt>
                <dd>{{ chiTiet.kyNckh || '—' }}</dd>
              </div>
              <div class="contract-row">
                <dt>Kinh phí</dt>
                <dd class="money">{{ fmtM(chiTiet.kinhPhi) }}</dd>
              </div>
              <div class="contract-row">
                <dt>Thời gian</dt>
                <dd>{{ chiTiet.thoiGianThucHien ? `${chiTiet.thoiGianThucHien} tháng` : '—' }}</dd>
              </div>
            </dl>
          </section>

          <section class="contract-section">
            <h3 class="section-title">II. CHỦ NHIỆM ĐỀ TÀI</h3>
            <dl class="contract-dl">
              <div class="contract-row">
                <dt>Họ và tên</dt>
                <dd>{{ chiTiet.giangVien?.hoTen ?? chiTiet.chuNhiem }}</dd>
              </div>
              <div class="contract-row">
                <dt>Đơn vị</dt>
                <dd>{{ chiTiet.giangVien?.khoa ?? '—' }}</dd>
              </div>
            </dl>
          </section>

          <div class="sig-row">
            <div class="sig-block">
              <div class="sig-label">Chủ nhiệm đề tài</div>
              <div class="sig-area" :class="{ done: chiTiet.trangThai === 'DANG_THUC_HIEN' || chiTiet.gvDaDongYHopDong }">
                <Check v-if="chiTiet.trangThai === 'DANG_THUC_HIEN' || chiTiet.gvDaDongYHopDong" size="32" class="text-accent" />
              </div>
              <div class="sig-name">{{ chiTiet.giangVien?.hoTen ?? chiTiet.chuNhiem }}</div>
              <div class="sig-date">
                <template v-if="chiTiet.trangThai === 'DANG_THUC_HIEN'">
                  {{ fmt(chiTiet.ngayKyHopDong ?? chiTiet.updatedAt) }}
                </template>
                <template v-else-if="chiTiet.gvDaDongYHopDong">
                  Đã đồng ý {{ fmt(chiTiet.ngayGvDongYHopDong ?? chiTiet.updatedAt) }}
                </template>
              </div>
            </div>
            <div class="sig-block">
              <div class="sig-label">Trưởng P.NCKH</div>
              <div class="sig-area" :class="{ done: chiTiet.trangThai === 'DANG_THUC_HIEN' }">
                <Check v-if="chiTiet.trangThai === 'DANG_THUC_HIEN'" size="32" class="text-accent" />
              </div>
              <div class="sig-name">P.NCKH</div>
              <div class="sig-date">{{ chiTiet.trangThai === 'DANG_THUC_HIEN' ? fmt(chiTiet.ngayKyHopDong ?? chiTiet.updatedAt) : '' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showFeedbackDialog && chiTiet"
      class="dialog-overlay"
      @click.self="!feedbackSubmitting && (showFeedbackDialog = false)"
    >
      <div class="dialog sign-dialog">
        <div class="dialog-header">
          <div class="sign-dialog-title">
            <span class="sign-dialog-icon"><MessageSquareText size="18" /></span>
            <h3>Yêu cầu chỉnh sửa hợp đồng</h3>
          </div>
          <button
            type="button"
            class="btn btn-ghost btn-sm"
            :disabled="feedbackSubmitting"
            aria-label="Đóng"
            @click="showFeedbackDialog = false"
          >
            <X size="18" />
          </button>
        </div>

        <div class="dialog-body">
          <p class="sign-dialog-message">
            Ghi rõ điều khoản hoặc thông tin cần P.NCKH rà soát lại trước khi bạn đồng ý hợp đồng.
          </p>

          <div class="form-group">
            <label class="form-label">Nội dung phản hồi <span class="required">*</span></label>
            <textarea
              v-model="feedbackNoiDung"
              class="form-textarea"
              :class="{ 'is-error': feedbackError }"
              rows="5"
              placeholder="Ví dụ: Đề nghị điều chỉnh thời gian thực hiện, kinh phí hoặc thông tin thành viên..."
              :disabled="feedbackSubmitting"
              @input="feedbackError = ''"
            ></textarea>
            <span v-if="feedbackError" class="form-error">{{ feedbackError }}</span>
          </div>
        </div>

        <div class="dialog-footer">
          <button
            type="button"
            class="btn btn-secondary"
            :disabled="feedbackSubmitting"
            @click="showFeedbackDialog = false"
          >
            Hủy
          </button>
          <button
            type="button"
            class="btn btn-primary flex items-center gap-2"
            :disabled="feedbackSubmitting"
            @click="yeuCauChinhSuaHopDong"
          >
            <span v-if="feedbackSubmitting" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <MessageSquareText v-else size="16" />
            {{ feedbackSubmitting ? 'Đang gửi...' : 'Gửi phản hồi' }}
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="showSignDialog && chiTiet"
      class="dialog-overlay"
      @click.self="!agreeing && (showSignDialog = false)"
    >
      <div class="dialog sign-dialog">
        <div class="dialog-header">
          <div class="sign-dialog-title">
            <span class="sign-dialog-icon"><PenTool size="18" /></span>
            <h3>Xác nhận đồng ý hợp đồng</h3>
          </div>
          <button
            type="button"
            class="btn btn-ghost btn-sm"
            :disabled="agreeing"
            aria-label="Đóng"
            @click="showSignDialog = false"
          >
            <X size="18" />
          </button>
        </div>

        <div class="dialog-body">
          <p class="sign-dialog-message">
            Bạn xác nhận đã đọc và đồng ý với nội dung hợp đồng nghiên cứu khoa học. Sau bước này, P.NCKH sẽ tiến hành in, ký, đóng dấu và cập nhật file scan lên hệ thống.
          </p>

          <dl class="sign-summary">
            <div>
              <dt>Mã số</dt>
              <dd class="mono">{{ chiTiet.maSo }}</dd>
            </div>
            <div>
              <dt>Tên đề tài</dt>
              <dd>{{ chiTiet.tenDeTai }}</dd>
            </div>
            <div>
              <dt>Kinh phí</dt>
              <dd class="money">{{ fmtM(chiTiet.kinhPhi) }}</dd>
            </div>
            <div>
              <dt>Kỳ NCKH</dt>
              <dd>{{ chiTiet.kyNckh || '—' }}</dd>
            </div>
          </dl>

          <div class="sign-warning">
            <AlertTriangle size="18" />
            <span>Hành động này chỉ ghi nhận đồng ý của chủ nhiệm đề tài; hợp đồng chỉ có hiệu lực sau khi P.NCKH xác nhận ký.</span>
          </div>
        </div>

        <div class="dialog-footer">
          <button
            type="button"
            class="btn btn-secondary"
            :disabled="agreeing"
            @click="showSignDialog = false"
          >
            Hủy
          </button>
          <button
            type="button"
            class="btn btn-primary flex items-center gap-2"
            :disabled="agreeing"
            @click="dongYHopDong"
          >
            <span v-if="agreeing" class="spinner" style="width:14px;height:14px;border-width:2px"></span>
            <PenTool v-else size="16" />
            {{ agreeing ? 'Đang gửi...' : 'Đồng ý hợp đồng' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.contract-loading {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hop-dong-wrapper {
  max-width: 820px;
}

.contract-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.alert-success {
  display: flex;
  align-items: center;
  background: color-mix(in srgb, var(--color-success) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-success) 30%, transparent);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-5);
  color: var(--color-success);
  font: var(--text-body);
}

.hop-dong-doc {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.hop-dong-head {
  background: linear-gradient(135deg, var(--color-accent) 0%, color-mix(in srgb, var(--color-accent) 70%, #000) 100%);
  padding: var(--space-6);
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.seal-ring {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.6);
  background: rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font: 700 20px/1 var(--font-sans);
  color: #fff;
  flex-shrink: 0;
}

.hop-dong-school {
  font: var(--text-sm);
  color: rgba(255,255,255,0.8);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.hop-dong-main-title {
  font: 700 20px/1.2 var(--font-sans);
  color: #fff;
  margin: 4px 0;
}

.hop-dong-sub-title {
  font: var(--text-sm);
  color: rgba(255,255,255,0.7);
}

.status-banner {
  padding: var(--space-3) var(--space-6);
  font: var(--text-sm);
  font-weight: 600;
  text-align: center;
}

.status-banner.signed {
  background: color-mix(in srgb, var(--color-success) 10%, transparent);
  color: var(--color-success);
}

.status-banner.pending {
  background: color-mix(in srgb, var(--color-warning) 10%, transparent);
  color: var(--color-warning);
}

.feedback-banner {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid color-mix(in srgb, var(--color-info, #0284c7) 22%, transparent);
  border-bottom: 1px solid color-mix(in srgb, var(--color-info, #0284c7) 22%, transparent);
  background: color-mix(in srgb, var(--color-info, #0284c7) 8%, transparent);
}

.feedback-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font: var(--text-sm);
  font-weight: 700;
  color: var(--color-accent);
  margin-bottom: var(--space-2);
}

.feedback-banner p {
  font: var(--text-body);
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  line-height: 1.6;
  margin-bottom: var(--space-1);
}

.feedback-banner span {
  font: var(--text-caption);
  color: var(--color-text-muted);
}

.contract-body {
  padding: var(--space-6);
}

.contract-section {
  margin-bottom: var(--space-6);
}

.section-title {
  font: var(--text-h4);
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--space-2);
  margin-bottom: var(--space-4);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 13px;
}

.contract-dl {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.contract-row {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: var(--space-4);
  align-items: baseline;
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-border);
}

.contract-row:last-child {
  border-bottom: none;
}

.contract-row dt {
  font: var(--text-sm);
  color: var(--color-text-muted);
}

.contract-row dd {
  font: var(--text-body);
  color: var(--color-text-primary);
}

.contract-row dd.money {
  font-weight: 600;
  color: var(--color-accent);
  font-size: 15px;
}

.sig-row {
  display: flex;
  justify-content: space-around;
  border-top: 1px solid var(--color-border);
  padding-top: var(--space-6);
  margin-top: var(--space-6);
}

.sig-block {
  text-align: center;
}

.sig-label {
  font: var(--text-sm);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--space-3);
}

.sig-area {
  width: 160px;
  height: 60px;
  margin: 0 auto var(--space-2);
  border-bottom: 2px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sig-area.done {
  border-color: var(--color-accent);
}

.sig-name {
  font: var(--text-body);
  color: var(--color-text-primary);
  font-weight: 600;
}

.sig-date {
  font: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: 2px;
}

.sign-dialog {
  max-width: 560px;
}

.sign-dialog-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.sign-dialog-title h3 {
  font: var(--text-h3);
  color: var(--color-text-primary);
}

.sign-dialog-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--color-accent) 12%, transparent);
  color: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
}

.sign-dialog-message {
  font: var(--text-body);
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--space-4);
}

.sign-summary {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: var(--space-4);
}

.sign-summary div {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}

.sign-summary div:last-child {
  border-bottom: none;
}

.sign-summary dt {
  font: var(--text-sm);
  color: var(--color-text-muted);
}

.sign-summary dd {
  font: var(--text-body);
  color: var(--color-text-primary);
  min-width: 0;
}

.sign-summary dd.money {
  font-weight: 700;
  color: var(--color-accent);
}

.sign-warning {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border: 1px solid color-mix(in srgb, var(--color-warning) 35%, transparent);
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--color-warning) 10%, transparent);
  color: var(--color-warning-text);
  font: var(--text-sm);
  line-height: 1.5;
}

.sign-warning svg {
  flex-shrink: 0;
  margin-top: 1px;
}

@media (max-width: 640px) {
  .hop-dong-head,
  .sig-row,
  .contract-actions {
    flex-direction: column;
  }

  .contract-row {
    grid-template-columns: 1fr;
    gap: 2px;
  }

  .contract-actions .btn {
    width: 100%;
    justify-content: center;
  }

  .sign-summary div {
    grid-template-columns: 1fr;
    gap: 2px;
  }

  .dialog-footer .btn {
    width: 100%;
    justify-content: center;
  }
}

@media print {
  .btn,
  .page-header {
    display: none !important;
  }
}
</style>
