/**
 * API Layer — dùng mock data engine
 * Khi có backend thật: chỉ cần đổi BASE_URL và xóa mock interceptor
 */
import axios from 'axios'
import {
  getDeTaiById, getDeTaiByRole, createDeTai,
  updateTrangThai, addTaiLieu, getDB, PB_LIST, KY_NCKH_LIST
} from '@/mock/db.js'

const BASE_URL = import.meta.env.VITE_API_URL || '__MOCK__'

const api = axios.create({
  baseURL: BASE_URL === '__MOCK__' ? '' : BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// ── Request interceptor: gắn JWT ──────────────────
api.interceptors.request.use(config => {
  const token = localStorage.getItem('rgms_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ── MOCK INTERCEPTOR ──────────────────────────────
// Khi VITE_API_URL chưa set → intercept mọi request và trả mock data
if (BASE_URL === '__MOCK__') {
  api.interceptors.request.use(async config => {
    await new Promise(r => setTimeout(r, 250)) // simulate network delay

    const token = localStorage.getItem('rgms_token')
    let user = null
    if (token) {
      try {
        const raw = atob(token.split('.')[1])
        user = JSON.parse(decodeURIComponent(Array.from(raw, c =>
          '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join('')))
      } catch {}
    }
    const url = config.url
    const method = config.method

    let mockData = null

    // GET /de-tai
    if (method === 'get' && url === '/de-tai') {
      const trangThai = config.params?.trangThai
      let list = user ? getDeTaiByRole(user.role, user.id) : getDB()
      if (trangThai) list = list.filter(d => d.trangThai === trangThai)
      mockData = list
    }

    // GET /de-tai/:id
    if (method === 'get' && url.match(/^\/de-tai\/\d+$/)) {
      const id = url.split('/').pop()
      mockData = getDeTaiById(id)
      if (!mockData) return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy đề tài' } } })
    }

    // POST /de-tai
    if (method === 'post' && url === '/de-tai') {
      mockData = createDeTai(JSON.parse(config.data), user)
    }

    // POST /de-tai/:id/gui-ho-so
    if (method === 'post' && url.match(/\/de-tai\/\d+\/gui-ho-so/)) {
      const id = url.split('/')[2]
      mockData = updateTrangThai(id, 'CHO_PNCKH_XEM_XET', user?.hoTen ?? 'GV', 'Gửi hồ sơ đến P.NCKH')
    }

    // POST /de-tai/:id/tiep-nhan
    if (method === 'post' && url.match(/\/de-tai\/\d+\/tiep-nhan/)) {
      const id = url.split('/')[2]
      mockData = updateTrangThai(id, 'DANG_XEM_XET_BOI_PNCKH', user?.hoTen ?? 'NCKH', 'Tiếp nhận hồ sơ')
    }

    // POST /de-tai/:id/bo-sung (GV nộp bổ sung)
    if (method === 'post' && url.match(/\/de-tai\/\d+\/bo-sung$/)) {
      const id = url.split('/')[2]
      const body = JSON.parse(config.data)
      mockData = updateTrangThai(id, 'CHO_PNCKH_XEM_XET', user?.hoTen ?? 'GV',
        'Đã bổ sung hồ sơ, gửi lại cho P.NCKH', { moTaBoSung: body.moTaBoSung })
    }

    // POST /de-tai/:id/yeu-cau-bo-sung
    if (method === 'post' && url.match(/\/de-tai\/\d+\/yeu-cau-bo-sung/)) {
      const id = url.split('/')[2]
      const body = JSON.parse(config.data)
      mockData = updateTrangThai(id, 'CHO_BO_SUNG_HO_SO', user?.hoTen ?? 'NCKH', 'Yêu cầu bổ sung hồ sơ', { yeuCauBoSung: body })
    }

    // POST /de-tai/:id/lap-to-phan-bien
    if (method === 'post' && url.match(/\/de-tai\/\d+\/lap-to-phan-bien/)) {
      const id = url.split('/')[2]
      const { thanhVienIds } = JSON.parse(config.data)
      const members = PB_LIST.filter(u => thanhVienIds.includes(u.id))
        .map(u => ({ ...u, ketQua: null, nhanXet: null }))
      mockData = updateTrangThai(id, 'DANG_PHAN_BIEN', user?.hoTen ?? 'NCKH', 'Lập tổ phản biện', { toPhanBien: members })
    }

    // POST /de-tai/:id/nop-ket-qua-pb
    if (method === 'post' && url.match(/\/de-tai\/\d+\/nop-ket-qua-pb/)) {
      const id = url.split('/')[2]
      const body = JSON.parse(config.data)
      const dt = getDeTaiById(id)
      const pb = dt.toPhanBien?.find(p => p.id === user?.id)
      if (pb) { pb.ketQua = body.ketQua; pb.nhanXet = body.nhanXet }
      dt.updatedAt = new Date().toISOString()
      mockData = JSON.parse(JSON.stringify(dt))
    }

    // POST /de-tai/:id/xet-duyet-pb
    if (method === 'post' && url.match(/\/de-tai\/\d+\/xet-duyet-pb/)) {
      const id = url.split('/')[2]
      const body = JSON.parse(config.data)
      const ketQua = body.ketQua ?? body.quyetDinh
      const nextStatus = ketQua === 'CHAP_THUAN' ? 'DANG_LAP_HOP_DONG'
        : ketQua === 'TU_CHOI' ? 'BI_TU_CHOI' : 'CHO_CHINH_SUA_THUYET_MINH'
      mockData = updateTrangThai(id, nextStatus, user?.hoTen ?? 'NCKH',
        `Xét duyệt PB: ${ketQua}`, { ghiChuPB: body.ghiChu })
    }

    // POST /de-tai/:id/ky-hop-dong
    if (method === 'post' && url.match(/\/de-tai\/\d+\/ky-hop-dong/)) {
      const id = url.split('/')[2]
      const body = JSON.parse(config.data || '{}')
      mockData = updateTrangThai(id, 'DANG_THUC_HIEN', user?.hoTen ?? 'NCKH', 'Ký hợp đồng thành công', { soHopDong: body.soHopDong })
    }

    // GET /users (danh sách PB)
    if (method === 'get' && url === '/users') mockData = PB_LIST

    // GET /ky-nckh
    if (method === 'get' && url === '/ky-nckh') mockData = KY_NCKH_LIST

    if (mockData !== null) {
      return Promise.reject({ isMockSuccess: true, data: mockData })
    }

    return Promise.reject({ response: { status: 501, data: { message: `Mock chưa handle: ${method} ${url}` } } })
  })

  api.interceptors.response.use(
    res => res,
    err => {
      if (err?.isMockSuccess) return Promise.resolve({ data: err.data, status: 200 })
      return Promise.reject(err)
    }
  )
}

// ── Real backend response interceptor ─────────────
if (BASE_URL !== '__MOCK__') {
  api.interceptors.response.use(
    res => res,
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
