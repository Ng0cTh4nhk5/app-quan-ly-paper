-- =============================================================================
-- V1_1__seed_data.sql
-- RGMS Initial Seed Data
-- Tạo tài khoản Admin, đơn vị mẫu, cấu hình hệ thống mặc định
-- Password hash = BCrypt(12) của "Admin@2026" (thay ngay sau lần đầu đăng nhập)
-- =============================================================================

-- [1] Đơn vị mẫu (P.NCKH)
INSERT INTO don_vi (id, ten, ma_don_vi, ngay_tao)
VALUES (
    '00000000-0000-7000-8000-000000000001',
    'Phòng Nghiên cứu Khoa học',
    'PNCKH',
    NOW()
);

-- [2] Tài khoản Admin hệ thống
-- PASSWORD: Admin@2026 — đổi ngay sau lần đầu đăng nhập!
INSERT INTO nguoi_dung (id, ho_ten, email, mat_khau_hash, role, loai_tai_khoan, don_vi_id, trang_thai, ngay_tao)
VALUES (
    '00000000-0000-7000-8000-000000000010',
    'Quản trị viên hệ thống',
    'admin@rgms.edu.vn',
    '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy2', -- Admin@2026
    'ADMIN',
    'CHINH_THUC',
    '00000000-0000-7000-8000-000000000001',
    'HOAT_DONG',
    NOW()
);

-- [3] Cấu hình hệ thống mặc định
INSERT INTO cau_hinh_he_thong (id, khoa, gia_tri, mo_ta, cap_nhat_boi_id, ngay_cap_nhat, ngay_tao)
VALUES
    (
        '00000000-0000-7000-8000-000000000100',
        'max_advance_rate',
        '0.5',
        'Tỷ lệ tạm ứng tối đa trên tổng kinh phí hợp đồng (0.0 - 1.0). Mặc định 50%.',
        '00000000-0000-7000-8000-000000000010',
        NOW(), NOW()
    ),
    (
        '00000000-0000-7000-8000-000000000101',
        'feedback_deadline_days',
        '7',
        'Số ngày giảng viên có để bổ sung hồ sơ sau khi P.NCKH tạo feedback.',
        '00000000-0000-7000-8000-000000000010',
        NOW(), NOW()
    ),
    (
        '00000000-0000-7000-8000-000000000102',
        'draft_expiry_days',
        '30',
        'Số ngày tối đa một đề tài ở trạng thái DRAFT trước khi hệ thống tự treo (E1).',
        '00000000-0000-7000-8000-000000000010',
        NOW(), NOW()
    ),
    (
        '00000000-0000-7000-8000-000000000103',
        'phan_bien_deadline_days',
        '14',
        'Số ngày mặc định để tổ phản biện nộp kết quả.',
        '00000000-0000-7000-8000-000000000010',
        NOW(), NOW()
    ),
    (
        '00000000-0000-7000-8000-000000000104',
        'thu_hoi_deadline_days',
        '30',
        'Số ngày giảng viên có để hoàn trả tạm ứng sau khi kế toán khởi động thu hồi.',
        '00000000-0000-7000-8000-000000000010',
        NOW(), NOW()
    );

-- [4] Kỳ NCKH mẫu (2025-2026)
INSERT INTO ky_nckh (id, ten, nam_hoc, ngay_bat_dau_dang_ky, ngay_ket_thuc_dang_ky, trang_thai, ngay_tao)
VALUES (
    '00000000-0000-7000-8000-000000000200',
    'Kỳ NCKH 2025–2026 (Học kỳ 1)',
    '2025-2026',
    '2025-09-01',
    '2025-10-31',
    'DA_DONG',
    NOW()
);
