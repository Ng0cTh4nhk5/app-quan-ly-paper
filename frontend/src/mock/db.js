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
    hopDongStatus: 'CHO_GV_XEM',
    gvDaDongYHopDong: false,
    hopDongFeedback: null,
    soHopDong: null,
  },
  {
    id: 10,
    maSo: 'NCKH-2026-0010',
    tenDeTai: 'Ứng dụng thị giác máy tính trong kiểm kê thiết bị phòng thí nghiệm',
    trangThai: 'DANG_PHAN_BIEN',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Trí tuệ nhân tạo',
    kyNckhId: 2,
    kyNckh: 'Kỳ NCKH 2026-II',
    moTa: 'Đề tài đã có đủ kết quả phản biện để P.NCKH demo bước chấp nhận và chuyển sang lập hợp đồng.',
    kinhPhi: 110000000,
    updatedAt: days(0),
    createdAt: days(28),
    taiLieu: [
      { id: 110, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-0010.pdf', downloadUrl: '#', size: '4.1 MB', uploadedAt: days(18) }
    ],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(28)),
      makeLog('TS. Nguyễn Văn Anh', 'Gửi hồ sơ', days(18)),
      makeLog('CN. Lê Văn Cường', 'Tiếp nhận và lập tổ phản biện', days(8)),
      makeLog('Tổ phản biện', 'Đã nộp đủ kết quả phản biện', days(0)),
    ],
    toPhanBien: [
      { id: 4, hoTen: 'TS. Phạm Quang Đức', ketQua: 'CHAP_NHAN', diemTong: 91, nhanXet: 'Đề tài có mục tiêu rõ, dữ liệu đầu vào khả thi và kế hoạch triển khai phù hợp.', submittedAt: days(1) },
      { id: 6, hoTen: 'TS. Đỗ Minh Phong', ketQua: 'CHAP_NHAN', diemTong: 87, nhanXet: 'Phương pháp nhận dạng phù hợp, cần chú ý quản trị dữ liệu ảnh khi triển khai.', submittedAt: days(1) },
    ],
  },
  {
    id: 11,
    maSo: 'NCKH-2026-0011',
    tenDeTai: 'Nền tảng theo dõi tiến độ đề tài bằng phân tích dữ liệu thời gian thực',
    trangThai: 'DANG_LAP_HOP_DONG',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Hệ thống thông tin',
    kyNckhId: 2,
    kyNckh: 'Kỳ NCKH 2026-II',
    moTa: 'Đề tài demo trạng thái P.NCKH đã soạn hợp đồng và đang chờ GV xem, đồng ý hoặc yêu cầu chỉnh sửa.',
    kinhPhi: 88000000,
    ngayBatDau: '2026-08-15',
    ngayKetThuc: '2027-02-15',
    tyLeTamUng: 40,
    updatedAt: days(0),
    createdAt: days(26),
    taiLieu: [
      { id: 111, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-0011.pdf', downloadUrl: '#', size: '3.7 MB', uploadedAt: days(17) }
    ],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(26)),
      makeLog('CN. Lê Văn Cường', 'Chấp nhận kết quả phản biện', days(3)),
      makeLog('CN. Lê Văn Cường', 'Soạn và gửi hợp đồng cho GV xem xét', days(0)),
    ],
    toPhanBien: [
      { id: 4, hoTen: 'TS. Phạm Quang Đức', ketQua: 'CHAP_NHAN', diemTong: 86, nhanXet: 'Nội dung phù hợp nhu cầu quản trị tiến độ đề tài.', submittedAt: days(4) },
      { id: 5, hoTen: 'PGS. Vũ Thị Em', ketQua: 'CHAP_NHAN', diemTong: 83, nhanXet: 'Kế hoạch triển khai khả thi, nên làm rõ phạm vi dashboard thời gian thực.', submittedAt: days(4) },
    ],
    hopDongStatus: 'CHO_GV_XEM',
    gvDaDongYHopDong: false,
    hopDongFeedback: null,
    soHopDong: 'HD-2026-0011',
    ghiChuHopDong: 'Dự thảo lần 1 gửi GV xem xét.',
  },
  {
    id: 12,
    maSo: 'NCKH-2026-0012',
    tenDeTai: 'Mô hình gợi ý tài liệu nghiên cứu cho giảng viên trẻ',
    trangThai: 'DANG_LAP_HOP_DONG',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyễn Văn Anh',
    linhVuc: 'Khoa học dữ liệu',
    kyNckhId: 2,
    kyNckh: 'Kỳ NCKH 2026-II',
    moTa: 'Đề tài demo nhánh GV yêu cầu chỉnh sửa hợp đồng để P.NCKH mở màn chỉnh sửa và gửi lại.',
    kinhPhi: 72000000,
    ngayBatDau: '2026-09-01',
    ngayKetThuc: '2027-03-01',
    tyLeTamUng: 30,
    updatedAt: days(0),
    createdAt: days(24),
    taiLieu: [
      { id: 112, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-0012.pdf', downloadUrl: '#', size: '2.9 MB', uploadedAt: days(16) }
    ],
    auditLog: [
      makeLog('TS. Nguyễn Văn Anh', 'Tạo đề tài', days(24)),
      makeLog('CN. Lê Văn Cường', 'Chấp nhận kết quả phản biện', days(4)),
      makeLog('CN. Lê Văn Cường', 'Soạn và gửi hợp đồng cho GV xem xét', days(2)),
      makeLog('TS. Nguyễn Văn Anh', 'GV phản hồi hợp đồng', days(0)),
    ],
    toPhanBien: [
      { id: 5, hoTen: 'PGS. Vũ Thị Em', ketQua: 'CHAP_NHAN', diemTong: 85, nhanXet: 'Đề tài có hướng ứng dụng rõ trong hỗ trợ nghiên cứu.', submittedAt: days(5) },
      { id: 6, hoTen: 'TS. Đỗ Minh Phong', ketQua: 'CHAP_NHAN', diemTong: 82, nhanXet: 'Cần quản lý rủi ro bản quyền dữ liệu nhưng tổng thể khả thi.', submittedAt: days(5) },
    ],
    hopDongStatus: 'CAN_SUA',
    gvDaDongYHopDong: false,
    hopDongFeedback: {
      noiDung: 'Đề nghị P.NCKH điều chỉnh ngày kết thúc sang 31/03/2027 và bổ sung ghi chú về phạm vi dữ liệu thử nghiệm.',
      createdAt: days(0),
      actor: 'TS. Nguyễn Văn Anh',
    },
    soHopDong: 'HD-2026-0012',
    ghiChuHopDong: 'Dự thảo cần rà soát lại theo phản hồi của GV.',
  },
  {
    id: 9,
    maSo: 'NCKH-2026-0009',
    tenDeTai: 'He thong canh bao som ngap do thi dua tren cam bien IoT',
    trangThai: 'DANG_LAP_HOP_DONG',
    chuNhiemId: 1,
    chuNhiem: 'TS. Nguyen Van Anh',
    linhVuc: 'IoT',
    kyNckhId: 2,
    kyNckh: 'Ky NCKH 2026-II',
    moTa: 'De tai da duoc phe duyet phan bien va giang vien da dong y hop dong de demo buoc ky.',
    kinhPhi: 98000000,
    ngayBatDau: '2026-08-01',
    ngayKetThuc: '2027-01-31',
    tyLeTamUng: 50,
    updatedAt: days(0),
    createdAt: days(22),
    taiLieu: [
      { id: 109, loai: 'THUYET_MINH', tenFile: 'thuyet-minh-de-tai-009.pdf', downloadUrl: '#', size: '3.4 MB', uploadedAt: days(20) }
    ],
    auditLog: [
      makeLog('TS. Nguyen Van Anh', 'Tao de tai', days(22)),
      makeLog('CN. Le Van Cuong', 'Chap nhan ket qua phan bien va soan hop dong', days(2)),
      makeLog('TS. Nguyen Van Anh', 'GV dong y hop dong', days(0)),
    ],
    toPhanBien: [
      { id: 4, hoTen: 'TS. Pham Quang Duc', ketQua: 'CHAP_NHAN', diemTong: 88, nhanXet: 'Noi dung tot, co kha nang ung dung trong do thi thong minh.', submittedAt: days(3) },
      { id: 5, hoTen: 'PGS. Vu Thi Em', ketQua: 'CHAP_NHAN', diemTong: 84, nhanXet: 'Phuong phap kha ro, can quan ly rui ro trien khai cam bien.', submittedAt: days(3) },
    ],
    hopDongStatus: 'CHO_KY',
    gvDaDongYHopDong: true,
    hopDongFeedback: null,
    soHopDong: 'HD-2026-0009',
    ngayGvDongYHopDong: days(0),
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
let _nextId = 13
let _nextMaSo = 13

export function getDB() { return _deTaiList }

export function getDeTaiById(id) {
  return _deTaiList.find(d => d.id === parseInt(id)) ?? null
}

export function getDeTaiByRole(role, userId) {
  if (role === 'GIANG_VIEN') return _deTaiList.filter(d => d.chuNhiemId === userId)
  if (role === 'NCKH') return _deTaiList
  if (role === 'TO_PHAN_BIEN') return _deTaiList.filter(d =>
    d.trangThai === 'DANG_PHAN_BIEN' &&
    d.toPhanBien?.some(pb => pb.id === userId && !pb.ketQua)
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
