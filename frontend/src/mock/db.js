/**
 * Mock Data Engine — RGMS
 * Mỗi lần F5 (import module mới) → fresh data được sinh lại
 */

const NOW = new Date()
const days = (n) => new Date(NOW - n * 86400000).toISOString()

// ── USERS ──────────────────────────────────────────
export const MOCK_USERS = {
  gv_a: { id: 1, username: 'nv.anh',   hoTen: 'TS. Nguyễn Văn Anh',   role: 'GIANG_VIEN', khoa: 'Công nghệ thông tin' },
  gv_b: { id: 2, username: 'tt.binh',  hoTen: 'PGS. Trần Thị Bình',  role: 'GIANG_VIEN', khoa: 'Toán học' },
  nckh: { id: 3, username: 'nckh.01',  hoTen: 'CN. Lê Văn Cường',    role: 'NCKH',        khoa: 'P.NCKH' },
  pb_1: { id: 4, username: 'pb.duc',   hoTen: 'TS. Phạm Quang Đức',  role: 'TO_PHAN_BIEN', khoa: 'Điện tử' },
  pb_2: { id: 5, username: 'pb.em',    hoTen: 'PGS. Vũ Thị Em',      role: 'TO_PHAN_BIEN', khoa: 'Cơ khí' },
  pb_3: { id: 6, username: 'pb.phong', hoTen: 'TS. Đỗ Minh Phong',   role: 'TO_PHAN_BIEN', khoa: 'Vật lý' },
}

export const PB_LIST = [MOCK_USERS.pb_1, MOCK_USERS.pb_2, MOCK_USERS.pb_3]

// ── KỲ NCKH ────────────────────────────────────────
export const KY_NCKH_LIST = [
  { id: 1, ten: 'Kỳ NCKH 2026-I',  batDau: '2026-01-01', ketThuc: '2026-06-30' },
  { id: 2, ten: 'Kỳ NCKH 2026-II', batDau: '2026-07-01', ketThuc: '2026-12-31' },
]

// ── AUDIT LOG FACTORY ───────────────────────────────
const makeLog = (actor, action, createdAt) => ({ actor, action, createdAt })

// ── ĐỀ TÀI DATA ────────────────────────────────────
export const MOCK_DE_TAI = [
  {
    id: 1,
    maSo: 'NCKH-2026-0001',
    tenDeTai: 'Nghiên cứu ứng dụng trí tuệ nhân tạo trong hỗ trợ giảng dạy đại học',
    trangThai: 'DRAFT',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Khoa học máy tính',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Đề tài nghiên cứu về ứng dụng AI trong giảng dạy đại học, tập trung vào mô hình học tập cá nhân hóa.',
    kinhPhi: 45000000,
    updatedAt: days(1),
    createdAt: days(3),
    taiLieu: [
      { id: 101, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-001.pdf', downloadUrl: '#', size: '2.4 MB', uploadedAt: days(2) }
    ],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(3)),
      makeLog('TS. Nguyễn Văn Anh', 'Upload thuyết minh', days(2)),
    ],
    toPhanBien: null,
  },
  {
    id: 2,
    maSo: 'NCKH-2026-0002',
    tenDeTai: 'Phát triển hệ thống quản lý thư viện số thế hệ mới tích hợp blockchain',
    trangThai: 'CHO_PNCKH_XEM_XET',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Hệ thống thông tin',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Hệ thống quản lý thư viện số tích hợp blockchain đảm bảo tính minh bạch và bảo mật.',
    kinhPhi: 60000000,
    updatedAt: days(2),
    createdAt: days(10),
    taiLieu: [
      { id: 102, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-002.pdf', downloadUrl: '#', size: '3.1 MB', uploadedAt: days(5) }
    ],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(10)),
      makeLog('TS. Nguyễn Văn Anh', 'Gửi hồ sơ đến P.NCKH', days(2)),
    ],
    toPhanBien: null,
  },
  {
    id: 3,
    maSo: 'NCKH-2026-0003',
    tenDeTai: 'Mô hình dự báo chất lượng không khí bằng học máy sâu',
    trangThai: 'DANG_XEM_XET_BOI_PNCKH',
    chuNhiemId: 2,
    chuNhiem: 'PGS. Trần Thị Bình',
    linhVuc: 'Khoa học máy tính',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Ứng dụng deep learning dự báo chất lượng không khí đô thị với độ chính xác cao.',
    kinhPhi: 80000000,
    updatedAt: days(1),
    createdAt: days(15),
    taiLieu: [
      { id: 103, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-003.pdf', downloadUrl: '#', size: '4.5 MB', uploadedAt: days(8) }
    ],
    auditLog: [
      makeLog('PGS. Trần Thị Bình', 'Tạo đề tài', days(15)),
      makeLog('PGS. Trần Thị Bình', 'Gửi hồ sơ đến P.NCKH', days(8)),
      makeLog('CN. Lê Văn Cường', 'Tiếp nhận hồ sơ', days(1)),
    ],
    toPhanBien: null,
  },
  {
    id: 4,
    maSo: 'NCKH-2026-0004',
    tenDeTai: 'Thiết kế robot tự hành phục vụ giao hàng trong khuôn viên trường đại học',
    trangThai: 'CHO_BO_SUNG_HO_SO',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Điện tử - Tự động hóa',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Robot tự hành tích hợp hệ thống định vị và tránh vật cản thông minh.',
    kinhPhi: 120000000,
    updatedAt: days(0),
    createdAt: days(20),
    yeuCauBoSung: {
      noiDung: 'Cần bổ sung: (1) Sơ đồ thiết kế phần cứng robot chi tiết; (2) Kế hoạch kinh phí phân kỳ theo từng giai đoạn; (3) Danh sách thành viên nhóm nghiên cứu với CV tóm tắt.',
      deadline: days(-7),
    },
    taiLieu: [],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(20)),
      makeLog('TS. Nguyễn Văn Anh', 'Gửi hồ sơ', days(12)),
      makeLog('CN. Lê Văn Cường', 'Tiếp nhận hồ sơ', days(10)),
      makeLog('CN. Lê Văn Cường', 'Yêu cầu bổ sung hồ sơ', days(0)),
    ],
    toPhanBien: null,
  },
  {
    id: 5,
    maSo: 'NCKH-2026-0005',
    tenDeTai: 'Nghiên cứu vật liệu nano ứng dụng trong lưu trữ năng lượng tái tạo',
    trangThai: 'DANG_PHAN_BIEN',
    chuNhiemId: 2,
    chuNhiem: 'PGS. Trần Thị Bình',
    linhVuc: 'Vật liệu học',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Phát triển vật liệu nano có khả năng lưu trữ năng lượng cao cho pin thế hệ mới.',
    kinhPhi: 150000000,
    updatedAt: days(3),
    createdAt: days(30),
    taiLieu: [
      { id: 105, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-005.pdf', downloadUrl: '#', size: '5.2 MB', uploadedAt: days(20) }
    ],
    auditLog: [
      makeLog('PGS. Trần Thị Bình', 'Tạo đề tài', days(30)),
      makeLog('PGS. Trần Thị Bình', 'Gửi hồ sơ', days(20)),
      makeLog('CN. Lê Văn Cường', 'Tiếp nhận & lập tổ phản biện', days(5)),
    ],
    toPhanBien: [
      { id: 4, hoTen: 'TS. Phạm Quang Đức',  ketQua: null, nhanXet: null },
      { id: 5, hoTen: 'PGS. Vũ Thị Em',     ketQua: 'CHAP_NHAN', nhanXet: 'Đề tài có tính khả thi cao, phương pháp nghiên cứu rõ ràng.' },
    ],
  },
  {
    id: 6,
    maSo: 'NCKH-2026-0006',
    tenDeTai: 'Xây dựng nền tảng học trực tuyến thích ứng cho sinh viên khuyết tật',
    trangThai: 'DANG_LAP_HOP_DONG',
    chuNhiemId: 2,
    chuNhiem: 'PGS. Trần Thị Bình',
    linhVuc: 'Giáo dục học',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Nền tảng e-learning với công nghệ hỗ trợ tiếp cận cho người khuyết tật.',
    kinhPhi: 75000000,
    updatedAt: days(1),
    createdAt: days(40),
    taiLieu: [
      { id: 106, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-006.pdf', downloadUrl: '#', size: '3.8 MB', uploadedAt: days(30) }
    ],
    auditLog: [
      makeLog('PGS. Trần Thị Bình', 'Tạo đề tài', days(40)),
      makeLog('CN. Lê Văn Cường', 'Phê duyệt — chấp nhận', days(5)),
      makeLog('CN. Lê Văn Cường', 'Đang soạn hợp đồng', days(1)),
    ],
    toPhanBien: [
      { id: 4, hoTen: 'TS. Phạm Quang Đức', ketQua: 'CHAP_NHAN', nhanXet: 'Đề tài phù hợp mục tiêu.' },
      { id: 6, hoTen: 'TS. Đỗ Minh Phong',  ketQua: 'CHAP_NHAN', nhanXet: 'Tính ứng dụng cao.' },
    ],
    soHopDong: null,
  },
  {
    id: 7,
    maSo: 'NCKH-2025-0087',
    tenDeTai: 'Phân tích dữ liệu lớn trong quản trị giáo dục đại học',
    trangThai: 'DANG_THUC_HIEN',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Khoa học máy tính',
    kyNckhId: 1,
    kyNckh: 'Kỳ NCKH 2026-I',
    moTa: 'Áp dụng big data analytics để cải thiện chất lượng quản trị đại học.',
    kinhPhi: 90000000,
    updatedAt: days(5),
    createdAt: days(90),
    taiLieu: [],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(90)),
      makeLog('CN. Lê Văn Cường', 'Ký hợp đồng', days(60)),
    ],
    toPhanBien: null,
  },
  {
    id: 8,
    maSo: 'NCKH-2025-0099',
    tenDeTai: 'Xay dung bo cong cu truc quan hoa du lieu nghien cuu khoa hoc',
    trangThai: 'HOAN_TAT',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyen Van Anh',
    linhVuc: 'He thong thong tin',
    kyNckhId: 1,
    kyNckh: 'Ky NCKH 2026-I',
    moTa: 'De tai da hoan tat de kiem thu nhan trang thai va giao dien danh sach.',
    kinhPhi: 55000000,
    updatedAt: days(12),
    createdAt: days(140),
    taiLieu: [
      { id: 108, loai: 'BAO_CAO_TONG_KET', tenFile: 'bao-cao-tong-ket-0099.pdf', downloadUrl: '#', size: '2.9 MB', uploadedAt: days(15) }
    ],
    auditLog: [
      makeLog('TS. Nguyen Van Anh', 'Tao de tai', days(140)),
      makeLog('CN. Le Van Cuong', 'Nghiem thu va hoan tat de tai', days(12)),
    ],
    toPhanBien: null,
  },
]

// ── MOCK STATE (mutated by actions) ────────────────
let _deTaiList = JSON.parse(JSON.stringify(MOCK_DE_TAI))
let _nextId = 9
let _nextMaSo = 9

export function getDB() { return _deTaiList }

export function getDeTaiById(id) {
  return _deTaiList.find(d => d.id === parseInt(id)) ?? null
}

export function getDeTaiByRole(role, userId) {
  if (role === 'GIANG_VIEN') return _deTaiList.filter(d => d.chuNhiemId === userId)
  if (role === 'NCKH') return _deTaiList
  if (role === 'TO_PHAN_BIEN') return _deTaiList.filter(d =>
    d.trangThai === 'DANG_PHAN_BIEN' &&
    d.toPhanBien?.some(pb => pb.id === userId)
  )
  return []
}

export function createDeTai(payload, user) {
  const id = _nextId++
  const maSo = `NCKH-2026-${String(_nextMaSo++).padStart(4, '0')}`
  const newItem = {
    id, maSo,
    tenDeTai: payload.tenDeTai,
    trangThai: 'DRAFT',
    chuNhiemId: user.id,
    chuNhiem: user.hoTen,
    linhVuc: payload.linhVuc ?? '',
    kyNckhId: payload.kyNckhId,
    kyNckh: KY_NCKH_LIST.find(k => k.id === payload.kyNckhId)?.ten ?? '',
    moTa: payload.moTa ?? '',
    kinhPhi: 0,
    thoiGianThucHien: 12,
    updatedAt: new Date().toISOString(),
    createdAt: new Date().toISOString(),
    taiLieu: [],
    auditLog: [makeLog(user.hoTen, 'Tạo đề tài', new Date().toISOString())],
    toPhanBien: null,
  }
  _deTaiList.unshift(newItem)
  return newItem
}

export function updateTrangThai(id, trangThai, actorName, action, extra = {}) {
  const dt = getDeTaiById(id)
  if (!dt) throw new Error('Không tìm thấy đề tài')
  dt.trangThai = trangThai
  dt.updatedAt = new Date().toISOString()
  dt.auditLog.push(makeLog(actorName, action, new Date().toISOString()))
  Object.assign(dt, extra)
  return JSON.parse(JSON.stringify(dt))
}

export function addTaiLieu(detaiId, file, actorName = 'He thong') {
  const dt = getDeTaiById(detaiId)
  if (!dt) return
  dt.taiLieu = dt.taiLieu ?? []
  const size = typeof file.size === 'number'
    ? `${(file.size / 1048576).toFixed(1)} MB`
    : (file.size ?? '1.2 MB')
  dt.taiLieu.push({
    id: Date.now(),
    loai: file.loai ?? 'THUYET_MINH',
    tenFile: file.name ?? file.fileName ?? `file-${Date.now()}.pdf`,
    downloadUrl: file.downloadUrl ?? '#',
    size,
    uploadedAt: new Date().toISOString(),
  })
  dt.updatedAt = new Date().toISOString()
  dt.auditLog.push(makeLog(actorName, `Upload tai lieu: ${file.name ?? file.fileName ?? 'file'}`, new Date().toISOString()))
  return JSON.parse(JSON.stringify(dt))
}

export function removeTaiLieu(detaiId, taiLieuId, actorName = 'He thong') {
  const dt = getDeTaiById(detaiId)
  if (!dt) throw new Error('Khong tim thay de tai.')
  if (dt.trangThai !== 'DRAFT') throw new Error('Chi duoc xoa tai lieu khi de tai dang o trang thai nhap.')

  const taiLieuIndex = (dt.taiLieu ?? []).findIndex(t => t.id === parseInt(taiLieuId))
  if (taiLieuIndex === -1) throw new Error('Khong tim thay tai lieu.')

  const [removedFile] = dt.taiLieu.splice(taiLieuIndex, 1)
  dt.updatedAt = new Date().toISOString()
  dt.auditLog.push(makeLog(actorName, `Xoa tai lieu: ${removedFile.tenFile}`, new Date().toISOString()))
  return JSON.parse(JSON.stringify(dt))
}
