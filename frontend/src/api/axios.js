import axios from 'axios'
import {
  getDeTaiById,
  getDeTaiByRole,
  createDeTai,
  updateTrangThai,
  addTaiLieu,
  removeTaiLieu,
  getDB,
  PB_LIST,
  KY_NCKH_LIST,
} from '@/mock/db.js'
import {
  CONTRACT_DRAFTABLE_STATUSES,
  CONTRACT_REVIEWABLE_STATUSES,
  canActionsFor,
  hasOpenContractFeedback,
  normalizeContractStatus,
  normalizeReviewDecision,
  validateKyHopDong,
  validateLapToPhanBien,
  validatePbKetQua,
  validateSoanHopDong,
  validateTuChoiHoSo,
  validateXetDuyetPB,
  validateYeuCauBoSung,
} from '@/mock/sopDGuards.js'

const BASE_URL = import.meta.env.VITE_API_URL || '__MOCK__'

const api = axios.create({
  baseURL: BASE_URL === '__MOCK__' ? '' : BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

function getPayload(data, fallback = {}) {
  if (!data) return fallback
  if (typeof FormData !== 'undefined' && data instanceof FormData) {
    return Object.fromEntries(data.entries())
  }
  if (typeof data === 'string') return JSON.parse(data || '{}')
  return data
}

function asPercentRate(value) {
  const numeric = Number(value ?? 0)
  return numeric <= 1 ? numeric * 100 : numeric
}

function clone(data) {
  return JSON.parse(JSON.stringify(data))
}

function mockReject(status, message) {
  return Promise.reject({ response: { status, data: { message } } })
}

function getMockUser() {
  const token = localStorage.getItem('rgms_token')
  if (!token) return null
  try {
    const raw = atob(token.split('.')[1])
    return JSON.parse(decodeURIComponent(Array.from(raw, c =>
      '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join('')))
  } catch {
    return null
  }
}

function actor(user, fallback) {
  return user?.hoTen ?? fallback
}

function requireState(dt, state, message) {
  if (dt?.trangThai !== state) throw new Error(message)
}

function canReadTopic(dt, user) {
  if (!dt || !user) return false
  if (user.role === 'NCKH') return true
  if (user.role === 'GIANG_VIEN') return dt.chuNhiemId === user.id || dt.giangVien?.id === user.id
  if (user.role === 'TO_PHAN_BIEN') return dt.toPhanBien?.some(member => member.id === user.id)
  return false
}

function canMutateGvTopic(dt, user) {
  return user?.role === 'GIANG_VIEN' && (dt?.chuNhiemId === user.id || dt?.giangVien?.id === user.id)
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('rgms_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
    if (typeof config.headers?.delete === 'function') {
      config.headers.delete('Content-Type')
    } else {
      delete config.headers['Content-Type']
    }
  }
  return config
})

if (BASE_URL === '__MOCK__') {
  api.interceptors.request.use(async config => {
    await new Promise(resolve => setTimeout(resolve, 250))

    const user = getMockUser()
    const url = config.url
    const method = config.method
    let mockData = null

    try {
      if (method === 'get' && url === '/de-tai') {
        const trangThai = config.params?.trangThai
        let list = user ? getDeTaiByRole(user.role, user.id) : getDB()
        if (trangThai) list = list.filter(d => d.trangThai === trangThai)
        mockData = clone(list)
      }

      if (method === 'get' && url.match(/^\/de-tai\/\d+$/)) {
        const id = url.split('/').pop()
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canReadTopic(dt, user)) return mockReject(403, 'Bạn không có quyền xem đề tài này.')
        mockData = clone(dt)
      }

      if (method === 'get' && url.match(/^\/de-tai\/\d+\/can-actions$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canReadTopic(dt, user)) return mockReject(403, 'Bạn không có quyền xem đề tài này.')
        mockData = canActionsFor(dt, user)
      }

      if (method === 'post' && url === '/de-tai') {
        mockData = createDeTai(getPayload(config.data), user)
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/(upload|tai-lieu)$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canMutateGvTopic(dt, user)) return mockReject(403, 'Bạn không có quyền cập nhật đề tài này.')
        const body = getPayload(config.data)
        const file = body.file
        mockData = addTaiLieu(id, {
          name: file?.name ?? body.fileName ?? `file-${Date.now()}.pdf`,
          loai: body.loai ?? 'THUYET_MINH',
          size: file?.size ?? body.size,
          downloadUrl: file ? URL.createObjectURL(file) : undefined,
        }, actor(user, 'GV'))
      }

      if (method === 'delete' && url.match(/^\/de-tai\/\d+\/tai-lieu\/\d+$/)) {
        const [, , id, , taiLieuId] = url.split('/')
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canMutateGvTopic(dt, user)) return mockReject(403, 'Bạn không có quyền cập nhật đề tài này.')
        if (dt.trangThai !== 'DRAFT') return mockReject(422, 'Chỉ được xóa tài liệu khi đề tài đang ở trạng thái nháp.')
        mockData = removeTaiLieu(id, taiLieuId, actor(user, 'GV'))
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/gui-ho-so$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canMutateGvTopic(dt, user)) return mockReject(403, 'Bạn không có quyền cập nhật đề tài này.')
        const hasThuyetMinh = dt?.taiLieu?.some(t => t.loai === 'THUYET_MINH')
        if (!hasThuyetMinh) return mockReject(422, 'Cần tải lên bản thuyết minh trước khi gửi hồ sơ.')
        mockData = updateTrangThai(id, 'CHO_PNCKH_XEM_XET', actor(user, 'GV'), 'Gửi hồ sơ đến P.NCKH')
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/tiep-nhan$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        requireState(dt, 'CHO_PNCKH_XEM_XET', 'Chỉ tiếp nhận hồ sơ đang chờ P.NCKH xem xét.')
        mockData = updateTrangThai(id, 'DANG_XEM_XET_BOI_PNCKH', actor(user, 'NCKH'), 'Tiếp nhận hồ sơ')
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/bo-sung$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canMutateGvTopic(dt, user)) return mockReject(403, 'Bạn không có quyền cập nhật đề tài này.')
        const body = getPayload(config.data)
        mockData = updateTrangThai(id, 'CHO_PNCKH_XEM_XET', actor(user, 'GV'), 'Nộp bổ sung hồ sơ', {
          moTaBoSung: body.moTaBoSung,
          yeuCauBoSung: null,
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/yeu-cau-bo-sung$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        const body = getPayload(config.data)
        const noiDung = String(body.noiDung ?? body.lyDo ?? '').trim()
        const deadlinePhanHoi = body.deadlinePhanHoi ?? body.deadline
        requireState(dt, 'DANG_XEM_XET_BOI_PNCKH', 'Chỉ yêu cầu bổ sung khi hồ sơ đang được P.NCKH xem xét.')
        const validationError = validateYeuCauBoSung(body)
        if (validationError) return mockReject(422, validationError)
        mockData = updateTrangThai(id, 'CHO_BO_SUNG_HO_SO', actor(user, 'NCKH'), 'Yêu cầu bổ sung hồ sơ', {
          yeuCauBoSung: { noiDung, deadline: deadlinePhanHoi, deadlinePhanHoi },
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/tu-choi-ho-so$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        const body = getPayload(config.data)
        const lyDo = String(body.lyDo ?? body.noiDung ?? '').trim()
        requireState(dt, 'DANG_XEM_XET_BOI_PNCKH', 'Chỉ từ chối hồ sơ trong bước sơ thẩm.')
        const validationError = validateTuChoiHoSo(body)
        if (validationError) return mockReject(422, validationError)
        mockData = updateTrangThai(id, 'BI_TU_CHOI', actor(user, 'NCKH'), 'Từ chối hồ sơ sơ thẩm', { lyDoTuChoi: lyDo })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/lap-to-phan-bien$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        const { thanhVienIds = [], deadlineNop } = getPayload(config.data)
        requireState(dt, 'DANG_XEM_XET_BOI_PNCKH', 'Chỉ lập tổ phản biện khi hồ sơ đang xem xét sơ thẩm.')
        const validationError = validateLapToPhanBien({ thanhVienIds, deadlineNop })
        if (validationError) return mockReject(422, validationError)
        const members = PB_LIST.filter(u => thanhVienIds.includes(u.id))
          .map(u => ({ id: u.id, hoTen: u.hoTen, khoa: u.khoa, ketQua: null, nhanXet: null, diemTong: null, submittedAt: null }))
        mockData = updateTrangThai(id, 'DANG_PHAN_BIEN', actor(user, 'NCKH'), 'Lập tổ phản biện', {
          toPhanBien: members,
          deadlineNopPhanBien: deadlineNop,
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/nop-ket-qua-pb$/)) {
        const id = url.split('/')[2]
        const body = getPayload(config.data)
        const dt = getDeTaiById(id)
        const pb = dt?.toPhanBien?.find(p => p.id === user?.id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (dt.trangThai !== 'DANG_PHAN_BIEN') return mockReject(422, 'Đề tài không ở trạng thái phản biện.')
        if (!pb) return mockReject(403, 'Bạn không thuộc tổ phản biện của đề tài này.')
        if (pb.ketQua) return mockReject(422, 'Bạn đã nộp kết quả phản biện cho đề tài này.')
        const validationError = validatePbKetQua(body)
        if (validationError) return mockReject(422, validationError)
        const nhanXet = String(body.nhanXet ?? '').trim()
        pb.ketQua = normalizeReviewDecision(body.ketQua ?? body.deXuat)
        pb.nhanXet = nhanXet
        pb.diemTong = Number(body.diemTong ?? 0)
        pb.diemKhoaHoc = Number(body.diemKhoaHoc ?? 0)
        pb.diemCongNghe = Number(body.diemCongNghe ?? 0)
        pb.diemTinhKhaDung = Number(body.diemTinhKhaDung ?? 0)
        pb.submittedAt = new Date().toISOString()
        dt.updatedAt = new Date().toISOString()
        dt.auditLog.push({ actor: actor(user, 'PB'), action: 'Nộp kết quả phản biện', createdAt: new Date().toISOString() })
        mockData = clone(dt)
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/xet-duyet-pb$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        const body = getPayload(config.data)
        const quyetDinh = normalizeReviewDecision(body.quyetDinh ?? body.ketQua)
        const ghiChu = String(body.ghiChu ?? '').trim()
        const noiDung = String(body.noiDung ?? '').trim()
        const deadlinePhanHoi = body.deadlinePhanHoi ?? body.deadline
        requireState(dt, 'DANG_PHAN_BIEN', 'Chỉ xét duyệt kết quả khi đề tài đang phản biện.')
        const validationError = validateXetDuyetPB(body, dt?.toPhanBien)
        if (validationError) return mockReject(422, validationError)
        const nextStatus = quyetDinh === 'CHAP_NHAN'
          ? 'DANG_LAP_HOP_DONG'
          : quyetDinh === 'TU_CHOI' ? 'BI_TU_CHOI' : 'CHO_CHINH_SUA_THUYET_MINH'
        mockData = updateTrangThai(id, nextStatus, actor(user, 'NCKH'), `Xét duyệt PB: ${quyetDinh}`, {
          ketQuaXetDuyetPB: quyetDinh,
          ghiChuPB: ghiChu,
          yeuCauChinhSuaThuyetMinh: quyetDinh === 'CAN_SUA' ? { noiDung, deadlinePhanHoi } : null,
          hopDongStatus: quyetDinh === 'CHAP_NHAN' ? 'CHUA_SOAN' : dt.hopDongStatus,
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/(hop-dong\/soan|soan-hop-dong)$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        const body = getPayload(config.data)
        requireState(dt, 'DANG_LAP_HOP_DONG', 'Chỉ soạn hợp đồng khi đề tài đang lập hợp đồng.')
        const contractStatus = normalizeContractStatus(dt)
        if (!CONTRACT_DRAFTABLE_STATUSES.includes(contractStatus)) {
          return mockReject(409, 'Hợp đồng đã được gửi hoặc đã qua bước soạn, không thể soạn lại.')
        }
        if (Number(body.kinhPhi) !== Number(dt.kinhPhi)) {
          return mockReject(422, 'Kinh phí hợp đồng phải theo kinh phí đã phê duyệt của đề tài.')
        }
        const validationError = validateSoanHopDong(body, dt)
        if (validationError) return mockReject(422, validationError)
        const tyLeTamUngPercent = asPercentRate(body.tyLeTamUng)
        mockData = updateTrangThai(id, 'DANG_LAP_HOP_DONG', actor(user, 'NCKH'), 'Soạn và gửi hợp đồng cho GV xem xét', {
          kinhPhi: Number(dt.kinhPhi),
          ngayBatDau: body.ngayBatDau,
          ngayKetThuc: body.ngayKetThuc,
          tyLeTamUng: tyLeTamUngPercent,
          ghiChuHopDong: body.ghiChu,
          hopDongStatus: 'CHO_GV_XEM',
          gvDaDongYHopDong: false,
          hopDongFeedback: null,
          soHopDong: body.soHopDong ?? dt.soHopDong ?? `HD-${new Date().getFullYear()}-${String(id).padStart(4, '0')}`,
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/gv-dong-y-hop-dong$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canMutateGvTopic(dt, user)) return mockReject(403, 'Bạn không có quyền cập nhật đề tài này.')
        const contractStatus = normalizeContractStatus(dt)
        if (!CONTRACT_REVIEWABLE_STATUSES.includes(contractStatus)) return mockReject(409, 'Hợp đồng chưa ở trạng thái chờ GV phản hồi.')
        if (hasOpenContractFeedback(dt)) return mockReject(422, 'Hợp đồng đang có phản hồi chỉnh sửa, cần P.NCKH soạn lại trước khi đồng ý.')
        mockData = updateTrangThai(id, 'DANG_LAP_HOP_DONG', actor(user, 'GV'), 'GV đồng ý hợp đồng', {
          gvDaDongYHopDong: true,
          hopDongStatus: 'CHO_KY',
          ngayGvDongYHopDong: new Date().toISOString(),
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/ky-hop-dong$/)) {
        const id = url.split('/')[2]
        const body = getPayload(config.data)
        const dt = getDeTaiById(id)
        requireState(dt, 'DANG_LAP_HOP_DONG', 'Chỉ ký hợp đồng khi đề tài đang lập hợp đồng.')
        const contractStatus = normalizeContractStatus(dt)
        if (contractStatus !== 'CHO_KY') return mockReject(409, 'Chỉ xác nhận ký khi GV đã đồng ý nội dung hợp đồng.')
        if (!dt?.gvDaDongYHopDong) return mockReject(422, 'Cần GV đồng ý hợp đồng trước khi xác nhận ký.')
        if (hasOpenContractFeedback(dt)) return mockReject(422, 'Hợp đồng đang có phản hồi chỉnh sửa, cần soạn lại trước khi ký.')
        const validationError = validateKyHopDong(body)
        if (validationError) return mockReject(422, validationError)
        const fileScan = body.fileScan ?? body.file
        mockData = updateTrangThai(id, 'DANG_THUC_HIEN', actor(user, 'NCKH'), 'Ký hợp đồng thành công', {
          soHopDong: body.soHopDong ?? dt.soHopDong ?? `HD-${new Date().getFullYear()}-${String(id).padStart(4, '0')}`,
          ngayKyHopDong: body.ngayKy,
          hopDongStatus: 'DA_KY',
          fileScanHopDong: fileScan?.name ?? body.fileName ?? 'hop-dong-da-ky.pdf',
        })
      }

      if (method === 'post' && url.match(/\/de-tai\/\d+\/hop-dong\/yeu-cau-chinh-sua$/)) {
        const id = url.split('/')[2]
        const dt = getDeTaiById(id)
        if (!dt) return mockReject(404, 'Không tìm thấy đề tài.')
        if (!canMutateGvTopic(dt, user)) return mockReject(403, 'Bạn không có quyền cập nhật đề tài này.')
        const contractStatus = normalizeContractStatus(dt)
        if (!CONTRACT_REVIEWABLE_STATUSES.includes(contractStatus)) return mockReject(409, 'Hợp đồng chưa ở trạng thái chờ GV phản hồi.')
        if (hasOpenContractFeedback(dt)) return mockReject(409, 'Bạn đã gửi yêu cầu chỉnh sửa, vui lòng chờ P.NCKH cập nhật hợp đồng.')
        if (dt?.gvDaDongYHopDong) return mockReject(409, 'GV đã đồng ý hợp đồng, không thể gửi yêu cầu chỉnh sửa.')
        const body = getPayload(config.data)
        const noiDung = String(body.noiDung ?? '').trim()
        if (noiDung.length < 10) return mockReject(422, 'Vui lòng nhập nội dung chỉnh sửa ít nhất 10 ký tự.')
        mockData = updateTrangThai(id, 'DANG_LAP_HOP_DONG', actor(user, 'GV'), 'GV phản hồi hợp đồng', {
          gvDaDongYHopDong: false,
          hopDongStatus: 'CAN_SUA',
          hopDongFeedback: {
            noiDung,
            createdAt: new Date().toISOString(),
            actor: actor(user, 'GV'),
          },
        })
      }

      if (method === 'get' && url === '/users') mockData = clone(PB_LIST)
      if (method === 'get' && url === '/ky-nckh') mockData = clone(KY_NCKH_LIST)
    } catch (error) {
      return mockReject(422, error.message)
    }

    if (mockData !== null) {
      return Promise.reject({ isMockSuccess: true, data: mockData })
    }

    return mockReject(501, `Mock chưa xử lý: ${method} ${url}`)
  })

  api.interceptors.response.use(
    res => res,
    err => err?.isMockSuccess ? Promise.resolve({ data: err.data, status: 200 }) : Promise.reject(err)
  )
}

if (BASE_URL !== '__MOCK__') {
  api.interceptors.response.use(
    res => {
      if (res.data?.success === true && Object.prototype.hasOwnProperty.call(res.data, 'data')) {
        return { ...res, data: res.data.data }
      }
      return res
    },
    err => {
      if (err.response?.status === 401) {
        localStorage.removeItem('rgms_token')
        window.location.href = '/login'
      }
      if (err.response?.status === 403) window.location.href = '/forbidden'
      return Promise.reject(err)
    }
  )
}

export default api
