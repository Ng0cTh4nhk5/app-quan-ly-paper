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

    if (method === 'get' && url === '/de-tai') {
      const trangThai = config.params?.trangThai
      let list = user ? getDeTaiByRole(user.role, user.id) : getDB()
      if (trangThai) list = list.filter(d => d.trangThai === trangThai)
      mockData = list
    }

    if (method === 'get' && url.match(/^\/de-tai\/\d+$/)) {
      const id = url.split('/').pop()
      mockData = getDeTaiById(id)
      if (!mockData) {
        return Promise.reject({ response: { status: 404, data: { message: 'Khong tim thay de tai.' } } })
      }
    }

    if (method === 'post' && url === '/de-tai') {
      mockData = createDeTai(getPayload(config.data), user)
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/(upload|tai-lieu)$/)) {
      const id = url.split('/')[2]
      const dt = getDeTaiById(id)
      if (!dt) {
        return Promise.reject({ response: { status: 404, data: { message: 'Khong tim thay de tai.' } } })
      }
      const body = getPayload(config.data)
      const file = body.file
      mockData = addTaiLieu(id, {
        name: file?.name ?? body.fileName ?? `file-${Date.now()}.pdf`,
        loai: body.loai ?? 'THUYET_MINH',
        size: file?.size ?? body.size,
        downloadUrl: file ? URL.createObjectURL(file) : undefined,
      }, user?.hoTen ?? 'GV')
    }

    if (method === 'delete' && url.match(/^\/de-tai\/\d+\/tai-lieu\/\d+$/)) {
      const [, , id, , taiLieuId] = url.split('/')
      const dt = getDeTaiById(id)
      if (!dt) {
        return Promise.reject({ response: { status: 404, data: { message: 'Khong tim thay de tai.' } } })
      }
      if (dt.trangThai !== 'DRAFT') {
        return Promise.reject({ response: { status: 422, data: { message: 'Chi duoc xoa tai lieu khi de tai dang o trang thai nhap.' } } })
      }
      try {
        mockData = removeTaiLieu(id, taiLieuId, user?.hoTen ?? 'GV')
      } catch (error) {
        return Promise.reject({ response: { status: 422, data: { message: error.message } } })
      }
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/gui-ho-so$/)) {
      const id = url.split('/')[2]
      const dt = getDeTaiById(id)
      const hasThuyetMinh = dt?.taiLieu?.some(t => t.loai === 'THUYET_MINH')
      if (!hasThuyetMinh) {
        return Promise.reject({ response: { status: 422, data: { message: 'Can tai len ban thuyet minh truoc khi gui ho so.' } } })
      }
      mockData = updateTrangThai(id, 'CHO_PNCKH_XEM_XET', user?.hoTen ?? 'GV', 'Gui ho so den P.NCKH')
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/tiep-nhan$/)) {
      const id = url.split('/')[2]
      mockData = updateTrangThai(id, 'DANG_XEM_XET_BOI_PNCKH', user?.hoTen ?? 'NCKH', 'Tiep nhan ho so')
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/bo-sung$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      mockData = updateTrangThai(id, 'CHO_PNCKH_XEM_XET', user?.hoTen ?? 'GV', 'Nop bo sung ho so', {
        moTaBoSung: body.moTaBoSung,
        yeuCauBoSung: null,
      })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/yeu-cau-bo-sung$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      mockData = updateTrangThai(id, 'CHO_BO_SUNG_HO_SO', user?.hoTen ?? 'NCKH', 'Yeu cau bo sung ho so', {
        yeuCauBoSung: {
          noiDung: body.noiDung ?? body.lyDo ?? '',
          deadline: body.deadline,
        },
      })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/lap-to-phan-bien$/)) {
      const id = url.split('/')[2]
      const { thanhVienIds = [] } = getPayload(config.data)
      const members = PB_LIST.filter(u => thanhVienIds.includes(u.id))
        .map(u => ({ ...u, ketQua: null, nhanXet: null }))
      mockData = updateTrangThai(id, 'DANG_PHAN_BIEN', user?.hoTen ?? 'NCKH', 'Lap to phan bien', { toPhanBien: members })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/nop-ket-qua-pb$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      const dt = getDeTaiById(id)
      const pb = dt?.toPhanBien?.find(p => p.id === user?.id)
      if (pb) {
        pb.ketQua = body.ketQua ?? body.deXuat
        pb.nhanXet = body.nhanXet
      }
      dt.updatedAt = new Date().toISOString()
      mockData = JSON.parse(JSON.stringify(dt))
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/xet-duyet-pb$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      const ketQua = body.ketQua ?? body.quyetDinh
      const nextStatus = ketQua === 'CHAP_THUAN'
        ? 'DANG_LAP_HOP_DONG'
        : ketQua === 'TU_CHOI' ? 'BI_TU_CHOI' : 'CHO_CHINH_SUA_THUYET_MINH'
      mockData = updateTrangThai(id, nextStatus, user?.hoTen ?? 'NCKH', `Xet duyet PB: ${ketQua}`, { ghiChuPB: body.ghiChu })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/hop-dong\/soan$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      mockData = updateTrangThai(id, 'DANG_LAP_HOP_DONG', user?.hoTen ?? 'NCKH', 'Soan va gui hop dong cho GV xem xet', {
        kinhPhi: body.kinhPhi,
        thoiGianThucHien: body.thoiGian,
        ghiChuHopDong: body.ghiChu,
        hopDongStatus: 'DANG_SOAN',
        gvDaDongYHopDong: false,
        hopDongFeedback: null,
        soHopDong: body.soHopDong,
      })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/gv-dong-y-hop-dong$/)) {
      const id = url.split('/')[2]
      const dt = getDeTaiById(id)
      if (dt?.hopDongFeedback) {
        return Promise.reject({ response: { status: 422, data: { message: 'Hop dong dang co phan hoi chinh sua, can P.NCKH soan lai truoc khi dong y.' } } })
      }
      mockData = updateTrangThai(id, 'DANG_LAP_HOP_DONG', user?.hoTen ?? 'GV', 'GV dong y hop dong', {
        gvDaDongYHopDong: true,
        hopDongStatus: 'CHO_KY',
        ngayGvDongYHopDong: new Date().toISOString(),
      })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/ky-hop-dong$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      const dt = getDeTaiById(id)
      if (!dt?.gvDaDongYHopDong) {
        return Promise.reject({ response: { status: 422, data: { message: 'Can GV dong y hop dong truoc khi xac nhan ky.' } } })
      }
      if (dt?.hopDongFeedback) {
        return Promise.reject({ response: { status: 422, data: { message: 'Hop dong dang co phan hoi chinh sua, can soan lai truoc khi ky.' } } })
      }
      mockData = updateTrangThai(id, 'DANG_THUC_HIEN', user?.hoTen ?? 'NCKH', 'Ky hop dong thanh cong', {
        soHopDong: body.soHopDong ?? `HD-${new Date().getFullYear()}-${String(id).padStart(4, '0')}`,
        ngayKyHopDong: new Date().toISOString(),
        hopDongStatus: 'DA_KY',
      })
    }

    if (method === 'post' && url.match(/\/de-tai\/\d+\/hop-dong\/yeu-cau-chinh-sua$/)) {
      const id = url.split('/')[2]
      const body = getPayload(config.data)
      const noiDung = String(body.noiDung ?? '').trim()
      if (noiDung.length < 10) {
        return Promise.reject({ response: { status: 422, data: { message: 'Vui long nhap noi dung chinh sua it nhat 10 ky tu.' } } })
      }
      mockData = updateTrangThai(id, 'DANG_LAP_HOP_DONG', user?.hoTen ?? 'GV', 'GV phan hoi hop dong', {
        gvDaDongYHopDong: false,
        hopDongStatus: 'CAN_SUA',
        hopDongFeedback: {
          noiDung,
          createdAt: new Date().toISOString(),
          actor: user?.hoTen ?? 'GV',
        },
      })
    }

    if (method === 'get' && url === '/users') mockData = PB_LIST
    if (method === 'get' && url === '/ky-nckh') mockData = KY_NCKH_LIST

    if (mockData !== null) {
      return Promise.reject({ isMockSuccess: true, data: mockData })
    }

    return Promise.reject({ response: { status: 501, data: { message: `Mock chua handle: ${method} ${url}` } } })
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
