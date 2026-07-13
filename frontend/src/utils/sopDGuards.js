export const EMPTY_SOP_D_ACTIONS = {
  guiHoSo: false,
  boSungHoSo: false,
  xoaTaiLieu: false,
  xemHopDong: false,
  tiepNhan: false,
  yeuCauBoSung: false,
  tuChoiHoSo: false,
  lapToPhanBien: false,
  nopKetQuaPB: false,
  xetDuyetPB: false,
  soanHopDong: false,
  kyHopDong: false,
  dongYHopDong: false,
}

export function normalizeReviewDecision(value) {
  // FIXED(TL): Không default sang CHAP_NHAN khi value là null/undefined.
  // Trả về null để caller tự handle, tránh bypass guard validateXetDuyetPB.
  if (value == null) return null
  const raw = String(value).trim()
  if (raw === 'CHAP_THUAN') return 'CHAP_NHAN'
  if (raw === 'CHO_CHINH_SUA' || raw === 'YEU_CAU_CHINH_SUA' || raw === 'YEU_CAU_SUA') return 'CAN_SUA'
  return raw
}

export const CONTRACT_DRAFTABLE_STATUSES = ['CHUA_SOAN', 'CAN_SUA', 'YEU_CAU_SUA']
export const CONTRACT_FEEDBACK_STATUSES = ['CAN_SUA', 'YEU_CAU_SUA']
export const CONTRACT_REVIEWABLE_STATUSES = ['CHO_GV_XEM', 'CHO_PHAN_HOI']
export const MAX_ADVANCE_RATE_PERCENT = 50

export function normalizeContractStatus(dt) {
  if (!dt) return 'CHUA_SOAN'
  if (dt.hopDongStatus) return dt.hopDongStatus
  // FIXED(TL): Bổ sung case HOAN_TAT - trước đây trả về null gây lỗi guard.
  if (dt.trangThai === 'DANG_THUC_HIEN' || dt.trangThai === 'HOAN_TAT') return 'DA_KY'
  if (dt.trangThai !== 'DANG_LAP_HOP_DONG') return null
  if (dt.gvDaDongYHopDong) return 'CHO_KY'
  if (dt.hopDongFeedback) return 'CAN_SUA'
  return 'CHUA_SOAN'
}

export function hasOpenContractFeedback(dt) {
  return Boolean(dt?.hopDongFeedback) || CONTRACT_FEEDBACK_STATUSES.includes(normalizeContractStatus(dt))
}

export function allPbSubmitted(pbResults = []) {
  return Array.isArray(pbResults) &&
    pbResults.length > 0 &&
    pbResults.every(pb => Boolean(pb?.ketQua) && Boolean(String(pb?.nhanXet ?? '').trim()))
}

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

function toIsoDate(value) {
  if (!value) return ''
  return String(value).slice(0, 10)
}

function toPercentRate(value) {
  if (value === undefined || value === null || value === '') return 0
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) return NaN
  return numeric <= 1 ? numeric * 100 : numeric
}

export function canActionsFor(dt, user) {
  const role = user?.role
  const pb = dt?.toPhanBien?.find(member => member.id === user?.id)
  const hasThuyetMinh = dt?.taiLieu?.some(t => t.loai === 'THUYET_MINH')
  const isOwner = dt?.chuNhiemId === user?.id || dt?.giangVien?.id === user?.id
  const contractStatus = normalizeContractStatus(dt)
  const hasContractFeedback = hasOpenContractFeedback(dt)
  return {
    ...EMPTY_SOP_D_ACTIONS,
    guiHoSo: role === 'GIANG_VIEN' && isOwner && ['DRAFT', 'CHO_CHINH_SUA_THUYET_MINH'].includes(dt?.trangThai) && hasThuyetMinh,
    boSungHoSo: role === 'GIANG_VIEN' && isOwner && dt?.trangThai === 'CHO_BO_SUNG_HO_SO',
    xoaTaiLieu: role === 'GIANG_VIEN' && isOwner && dt?.trangThai === 'DRAFT',
    xemHopDong: role === 'GIANG_VIEN' && isOwner && ['DANG_LAP_HOP_DONG', 'DANG_THUC_HIEN', 'HOAN_TAT'].includes(dt?.trangThai),
    tiepNhan: role === 'NCKH' && dt?.trangThai === 'CHO_PNCKH_XEM_XET',
    yeuCauBoSung: role === 'NCKH' && dt?.trangThai === 'DANG_XEM_XET_BOI_PNCKH',
    tuChoiHoSo: role === 'NCKH' && dt?.trangThai === 'DANG_XEM_XET_BOI_PNCKH',
    lapToPhanBien: role === 'NCKH' && dt?.trangThai === 'DANG_XEM_XET_BOI_PNCKH',
    nopKetQuaPB: role === 'TO_PHAN_BIEN' && dt?.trangThai === 'DANG_PHAN_BIEN' && Boolean(pb) && !pb.ketQua,
    // FIXED(TL): Thêm check dt.toPhanBien tồn tại - NCKH chỉ có thể xét duyệt
    // khi tổ phản biện đã được lập và có ít nhất 1 thành viên.
    xetDuyetPB: role === 'NCKH' && dt?.trangThai === 'DANG_PHAN_BIEN' &&
      Array.isArray(dt?.toPhanBien) && dt.toPhanBien.length > 0,
    soanHopDong: role === 'NCKH' && dt?.trangThai === 'DANG_LAP_HOP_DONG' && CONTRACT_DRAFTABLE_STATUSES.includes(contractStatus),
    kyHopDong: role === 'NCKH' && dt?.trangThai === 'DANG_LAP_HOP_DONG' && contractStatus === 'CHO_KY' && dt?.gvDaDongYHopDong === true && !hasContractFeedback,
    dongYHopDong: role === 'GIANG_VIEN' && isOwner && dt?.trangThai === 'DANG_LAP_HOP_DONG' && CONTRACT_REVIEWABLE_STATUSES.includes(contractStatus) && !dt?.gvDaDongYHopDong && !hasContractFeedback,
  }
}

export function enforceContractActionGuards(actions = {}, dt, role) {
  const contractStatus = normalizeContractStatus(dt)
  const hasContractFeedback = hasOpenContractFeedback(dt)
  const next = { ...actions }

  if (role === 'NCKH') {
    next.soanHopDong = Boolean(actions.soanHopDong) &&
      dt?.trangThai === 'DANG_LAP_HOP_DONG' &&
      CONTRACT_DRAFTABLE_STATUSES.includes(contractStatus)
    next.kyHopDong = Boolean(actions.kyHopDong) &&
      dt?.trangThai === 'DANG_LAP_HOP_DONG' &&
      contractStatus === 'CHO_KY' &&
      dt?.gvDaDongYHopDong === true &&
      !hasContractFeedback
  }

  if (role === 'GIANG_VIEN') {
    next.dongYHopDong = Boolean(actions.dongYHopDong) &&
      dt?.trangThai === 'DANG_LAP_HOP_DONG' &&
      CONTRACT_REVIEWABLE_STATUSES.includes(contractStatus) &&
      dt?.gvDaDongYHopDong !== true &&
      !hasContractFeedback
  }

  return next
}

export function validateYeuCauBoSung(payload) {
  const noiDung = String(payload?.noiDung ?? payload?.lyDo ?? '').trim()
  const deadlinePhanHoi = payload?.deadlinePhanHoi ?? payload?.deadline
  if (noiDung.length < 20) return 'Nội dung yêu cầu bổ sung phải có ít nhất 20 ký tự.'
  if (!deadlinePhanHoi) return 'Vui lòng chọn hạn phản hồi.'
  return ''
}

export function validateTuChoiHoSo(payload) {
  const lyDo = String(payload?.lyDo ?? payload?.noiDung ?? '').trim()
  if (lyDo.length < 10) return 'Lý do từ chối phải có ít nhất 10 ký tự.'
  return ''
}

export function validateLapToPhanBien(payload) {
  const thanhVienIds = payload?.thanhVienIds ?? []
  if (thanhVienIds.length < 2) return 'Cần chọn ít nhất 2 thành viên tổ phản biện.'
  if (!payload?.deadlineNop) return 'Vui lòng chọn hạn nộp kết quả phản biện.'
  return ''
}

export function validatePbKetQua(payload) {
  const nhanXet = String(payload?.nhanXet ?? '').trim()
  if (nhanXet.length < 20) return 'Nhận xét phải có ít nhất 20 ký tự.'
  return ''
}

export function validateXetDuyetPB(payload, pbResults = null) {
  const quyetDinh = normalizeReviewDecision(payload?.quyetDinh ?? payload?.ketQua)
  const ghiChu = String(payload?.ghiChu ?? '').trim()
  const noiDung = String(payload?.noiDung ?? '').trim()
  const deadlinePhanHoi = payload?.deadlinePhanHoi ?? payload?.deadline
  if (quyetDinh === 'CHAP_NHAN' && Array.isArray(pbResults) && !allPbSubmitted(pbResults)) {
    return 'Chỉ có thể chấp nhận khi tất cả thành viên phản biện đã nộp điểm và nhận xét.'
  }
  if (quyetDinh === 'CAN_SUA' && (noiDung.length < 20 || !deadlinePhanHoi)) {
    return 'Yêu cầu chỉnh sửa cần nội dung ít nhất 20 ký tự và hạn phản hồi.'
  }
  if (quyetDinh === 'TU_CHOI' && ghiChu.length < 10) {
    return 'Từ chối sau phản biện cần ghi chú lý do ít nhất 10 ký tự.'
  }
  return ''
}

export function validateSoanHopDong(payload, currentTopic = null) {
  if (!payload?.kinhPhi || Number(payload.kinhPhi) <= 0) return 'Kinh phí hợp đồng phải lớn hơn 0.'
  if (!payload?.ngayBatDau || !payload?.ngayKetThuc) return 'Vui lòng nhập ngày bắt đầu và ngày kết thúc.'
  const ngayBatDau = toIsoDate(payload.ngayBatDau)
  const originalNgayBatDau = toIsoDate(currentTopic?.ngayBatDau)
  const canKeepExistingPastStart = CONTRACT_FEEDBACK_STATUSES.includes(normalizeContractStatus(currentTopic)) &&
    originalNgayBatDau &&
    ngayBatDau === originalNgayBatDau &&
    ngayBatDau < todayIso()
  if (ngayBatDau < todayIso() && !canKeepExistingPastStart) return 'Ngày bắt đầu không được trước ngày hiện tại.'
  if (new Date(payload.ngayKetThuc) <= new Date(payload.ngayBatDau)) return 'Ngày kết thúc phải sau ngày bắt đầu.'
  const advanceRate = toPercentRate(payload.tyLeTamUng)
  if (!Number.isFinite(advanceRate) || advanceRate < 0 || advanceRate > MAX_ADVANCE_RATE_PERCENT) {
    return `Tỷ lệ tạm ứng tối đa theo hợp đồng phải từ 0 đến ${MAX_ADVANCE_RATE_PERCENT}%.`
  }
  return ''
}

export function validateKyHopDong(payload) {
  const fileScan = payload?.fileScan ?? payload?.file
  if (!fileScan) return 'Vui lòng tải lên file scan hợp đồng đã ký.'
  if (!payload?.ngayKy) return 'Vui lòng nhập ngày ký hợp đồng.'
  if (toIsoDate(payload.ngayKy) > todayIso()) return 'Ngày ký không được nằm trong tương lai.'
  return ''
}
