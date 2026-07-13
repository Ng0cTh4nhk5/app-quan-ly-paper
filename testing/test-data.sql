-- ============================================================================
-- Kịch bản Dữ liệu Mẫu Kiểm thử RGMS
-- Mục đích: Nạp cơ sở dữ liệu với dữ liệu thực tế phục vụ kiểm thử khắc phục lỗi
-- Ngày thực hiện: 2026-07-09
-- ============================================================================

-- Dọn dẹp dữ liệu cũ (chú ý - thao tác này sẽ xóa sạch toàn bộ!)
DELETE FROM thanh_vien_to_phan_bien;
DELETE FROM to_phan_bien;
DELETE FROM feedback;
DELETE FROM hop_dong;
DELETE FROM audit_log;
DELETE FROM phan_bien_de_xuat;
DELETE FROM tai_lieu;
DELETE FROM de_tai;
DELETE FROM ky_nckh;
DELETE FROM nguoi_dung WHERE id > 4;

-- Khởi tạo lại các sequence tự tăng
ALTER SEQUENCE nguoi_dung_id_seq RESTART WITH 5;
ALTER SEQUENCE ky_nckh_id_seq RESTART WITH 1;
ALTER SEQUENCE de_tai_id_seq RESTART WITH 1;
ALTER SEQUENCE hop_dong_id_seq RESTART WITH 1;
ALTER SEQUENCE feedback_id_seq RESTART WITH 1;
ALTER SEQUENCE to_phan_bien_id_seq RESTART WITH 1;
ALTER SEQUENCE thanh_vien_to_phan_bien_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS audit_log_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS phan_bien_de_xuat_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS tai_lieu_id_seq RESTART WITH 1;

-- ============================================================================
-- 1. TÀI KHOẢN THỬ NGHIỆM (GV, NCKH, PB)
-- ============================================================================

-- GV01: Giảng viên dùng để kiểm thử
INSERT INTO nguoi_dung (id, username, password_hash, ho_ten, email, vai_tro, ma_giang_vien, trang_thai, don_vi_id, created_at, updated_at)
VALUES (5, 'gv01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeQ6J6TqJ8Y9n8KGI6MZvxHJKL8JZz7.K',
        'Nguyễn Văn Anh', 'gv01@rgms.edu.vn', 'GIANG_VIEN', 'GV001', 'HOAT_DONG', 1,
        NOW(), NOW());
-- Mật khẩu giải mã: test123

-- GV02: Giảng viên khác (dùng để kiểm tra quyền sở hữu đề tài)
INSERT INTO nguoi_dung (id, username, password_hash, ho_ten, email, vai_tro, ma_giang_vien, trang_thai, don_vi_id, created_at, updated_at)
VALUES (6, 'gv02', '$2a$10$N9qo8uLOickgx2ZMRZoMyeQ6J6TqJ8Y9n8KGI6MZvxHJKL8JZz7.K',
        'Trần Thị Bình', 'gv02@rgms.edu.vn', 'GIANG_VIEN', 'GV002', 'HOAT_DONG', 1,
        NOW(), NOW());

-- NCKH01: Phòng NCKH dùng để kiểm thử
INSERT INTO nguoi_dung (id, username, password_hash, ho_ten, email, vai_tro, trang_thai, don_vi_id, created_at, updated_at)
VALUES (7, 'nckh01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeQ6J6TqJ8Y9n8KGI6MZvxHJKL8JZz7.K',
        'Lê Minh Châu', 'nckh01@rgms.edu.vn', 'NCKH', 'HOAT_DONG', 2,
        NOW(), NOW());

-- PB01: Phản biện viên 1
INSERT INTO nguoi_dung (id, username, password_hash, ho_ten, email, vai_tro, trang_thai, don_vi_id, created_at, updated_at)
VALUES (8, 'pb01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeQ6J6TqJ8Y9n8KGI6MZvxHJKL8JZz7.K',
        'Phạm Đức Dũng', 'pb01@rgms.edu.vn', 'TO_PHAN_BIEN', 'HOAT_DONG', 3,
        NOW(), NOW());

-- PB02: Phản biện viên 2
INSERT INTO nguoi_dung (id, username, password_hash, ho_ten, email, vai_tro, trang_thai, don_vi_id, created_at, updated_at)
VALUES (9, 'pb02', '$2a$10$N9qo8uLOickgx2ZMRZoMyeQ6J6TqJ8Y9n8KGI6MZvxHJKL8JZz7.K',
        'Hoàng Thị Oanh', 'pb02@rgms.edu.vn', 'TO_PHAN_BIEN', 'HOAT_DONG', 3,
        NOW(), NOW());

SELECT setval('nguoi_dung_id_seq', 9, true);

-- ============================================================================
-- 2. KỲ NCKH (1 hoạt động, 1 đã đóng)
-- ============================================================================

-- Kỳ NCKH 2026-01 (ĐANG MỞ - active)
INSERT INTO ky_nckh (id, ten_ky, trang_thai, created_at)
VALUES (1, 'Kỳ NCKH 2026-01', 'DANG_MO', NOW() - INTERVAL '30 days');

-- Kỳ NCKH 2025-02 (ĐÃ ĐÓNG - closed)
INSERT INTO ky_nckh (id, ten_ky, trang_thai, created_at)
VALUES (2, 'Kỳ NCKH 2025-02', 'DA_DONG', NOW() - INTERVAL '180 days');

SELECT setval('ky_nckh_id_seq', 2, true);

-- ============================================================================
-- 3. ĐỀ TÀI (15 đề tài bao phủ tất cả các trạng thái)
-- ============================================================================

-- Đề tài 1: CHO_PNCKH_XEM_XET (thuộc sở hữu của gv01)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (1, 'NCKH-2026-001',
        'Nghiên cứu ứng dụng AI trong chẩn đoán bệnh',
        'Phát triển hệ thống AI hỗ trợ bác sĩ chẩn đoán bệnh qua hình ảnh X-quang',
        'Trí tuệ nhân tạo', 'CHO_PNCKH_XEM_XET', 5, 1,
        NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 1);

-- Đề tài 2: CHO_BO_SUNG_HO_SO (thuộc sở hữu của gv01)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (2, 'NCKH-2026-002',
        'Phát triển hệ thống quản lý năng lượng thông minh',
        'Xây dựng giải pháp IoT giám sát và tối ưu hóa tiêu thụ điện năng',
        'IoT & Năng lượng', 'CHO_BO_SUNG_HO_SO', 5, 1,
        NOW() - INTERVAL '10 days', NOW() - INTERVAL '3 days', 1);

-- Đề tài 3: DANG_PHAN_BIEN (thuộc sở hữu của gv01)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (3, 'NCKH-2026-003',
        'Nghiên cứu blockchain trong bảo mật dữ liệu y tế',
        'Ứng dụng công nghệ blockchain để đảm bảo tính toàn vẹn hồ sơ bệnh án điện tử',
        'Blockchain & Bảo mật', 'DANG_PHAN_BIEN', 5, 1,
        NOW() - INTERVAL '20 days', NOW() - INTERVAL '8 days', 1);

-- Đề tài 4: DANG_PHAN_BIEN (thuộc sở hữu của gv02)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (4, 'NCKH-2026-004',
        'Tối ưu hóa thuật toán định tuyến mạng 5G',
        'Nghiên cứu cải thiện hiệu suất định tuyến trong mạng 5G đô thị',
        'Mạng và Truyền thông', 'DANG_PHAN_BIEN', 6, 1,
        NOW() - INTERVAL '18 days', NOW() - INTERVAL '7 days', 1);

-- Đề tài 5: CHO_CHINH_SUA_THUYET_MINH (thuộc sở hữu của gv01)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (5, 'NCKH-2026-005',
        'Phát triển robot tự hành phục vụ nông nghiệp',
        'Xây dựng robot thu hoạch cà chua tự động sử dụng computer vision',
        'Robotics & Nông nghiệp', 'CHO_CHINH_SUA_THUYET_MINH', 5, 1,
        NOW() - INTERVAL '25 days', NOW() - INTERVAL '5 days', 1);

-- Đề tài 6: DANG_LAP_HOP_DONG (thuộc sở hữu của gv02)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, gv_da_dong_y_hop_dong, created_at, updated_at, don_vi_id)
VALUES (6, 'NCKH-2026-006',
        'Nghiên cứu vật liệu nano cho pin lithium',
        'Tổng hợp và đánh giá vật liệu điện cực nano cấu trúc cho pin Li-ion',
        'Vật liệu & Năng lượng', 'DANG_LAP_HOP_DONG', 6, 1, true,
        NOW() - INTERVAL '30 days', NOW() - INTERVAL '2 days', 1);

-- Đề tài 7: DANG_THUC_HIEN (thuộc sở hữu của gv01, đã ký hợp đồng)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id,
                    kinh_phi, ngay_bat_dau, ngay_ket_thuc, gv_da_dong_y_hop_dong, created_at, updated_at, don_vi_id)
VALUES (7, 'NCKH-2026-007',
        'Xây dựng hệ thống dự báo thiên tai bằng ML',
        'Phát triển mô hình machine learning dự báo lũ lụt và hạn hán',
        'Machine Learning', 'DANG_THUC_HIEN', 5, 1,
        150000000.00, '2026-06-01', '2027-05-31', true,
        NOW() - INTERVAL '40 days', NOW() - INTERVAL '10 days', 1);

-- Đề tài 8: DANG_THUC_HIEN (thuộc sở hữu của gv02, đã ký hợp đồng)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id,
                    kinh_phi, ngay_bat_dau, ngay_ket_thuc, gv_da_dong_y_hop_dong, created_at, updated_at, don_vi_id)
VALUES (8, 'NCKH-2026-008',
        'Nghiên cứu phương pháp xử lý nước thải công nghiệp',
        'Phát triển công nghệ màng lọc nano xử lý kim loại nặng trong nước thải',
        'Môi trường', 'DANG_THUC_HIEN', 6, 1,
        200000000.00, '2026-05-15', '2027-05-14', true,
        NOW() - INTERVAL '50 days', NOW() - INTERVAL '15 days', 1);

-- Đề tài 9: BI_TU_CHOI (thuộc sở hữu của gv02, trạng thái cuối)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (9, 'NCKH-2026-009',
        'Nghiên cứu không khả thi về động cơ vĩnh cửu',
        'Đề tài không đạt yêu cầu về tính khả thi khoa học',
        'Vật lý', 'BI_TU_CHOI', 6, 1,
        NOW() - INTERVAL '60 days', NOW() - INTERVAL '50 days', 1);

-- Đề tài 10: DA_RUT (thuộc sở hữu của gv01, trạng thái cuối)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (10, 'NCKH-2026-010',
        'Nghiên cứu đã rút do lý do cá nhân',
        'Chủ nhiệm đề tài rút do chuyển công tác',
        'Khác', 'DA_RUT', 5, 1,
        NOW() - INTERVAL '35 days', NOW() - INTERVAL '20 days', 1);

-- Đề tài 11: CHO_PNCKH_XEM_XET (thuộc sở hữu của gv02)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (11, 'NCKH-2026-011',
        'Ứng dụng thực tế ảo trong giáo dục',
        'Phát triển môi trường VR/AR cho đào tạo y khoa',
        'VR/AR & Giáo dục', 'CHO_PNCKH_XEM_XET', 6, 1,
        NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 1);

-- Đề tài 12: DRAFT (thuộc sở hữu của gv01)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (12, 'NCKH-2026-012',
        'Nghiên cứu công nghệ in 3D trong y học',
        'In 3D mô sinh học và khung xương nhân tạo',
        'Y sinh học', 'DRAFT', 5, 1,
        NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 1);

-- Đề tài 13: BI_TREO (thuộc sở hữu của gv02, trạng thái cuối)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (13, 'NCKH-2026-013',
        'Đề tài bị treo do vi phạm quy định',
        'Vi phạm quy định về đạo đức nghiên cứu',
        'Khác', 'BI_TREO', 6, 1,
        NOW() - INTERVAL '70 days', NOW() - INTERVAL '60 days', 1);

-- Đề tài 14: DA_HUY (thuộc sở hữu của gv01, trạng thái cuối)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (14, 'NCKH-2026-014',
        'Đề tài đã hủy do thiếu kinh phí',
        'Hủy do không đủ nguồn kinh phí hỗ trợ',
        'Khác', 'DA_HUY', 5, 1,
        NOW() - INTERVAL '80 days', NOW() - INTERVAL '70 days', 1);

-- Đề tài 15: DANG_XEM_XET_BOI_PNCKH (thuộc sở hữu của gv01)
INSERT INTO de_tai (id, ma_so, ten_de_tai, mo_ta, linh_vuc, trang_thai, chu_nhiem_id, ky_nckh_id, created_at, updated_at, don_vi_id)
VALUES (15, 'NCKH-2026-015',
        'Nghiên cứu cảm biến sinh học đeo được',
        'Phát triển cảm biến theo dõi sức khỏe liên tục',
        'IoT & Y tế', 'DANG_XEM_XET_BOI_PNCKH', 5, 1,
        NOW() - INTERVAL '6 days', NOW() - INTERVAL '2 days', 1);

SELECT setval('de_tai_id_seq', 15, true);

-- ============================================================================
-- 4. HỢP ĐỒNG (cho các đề tài ở trạng thái DANG_THUC_HIEN)
-- ============================================================================

-- Hợp đồng cho đề tài 7
INSERT INTO hop_dong (id, de_tai_id, kinh_phi, ngay_bat_dau, ngay_ket_thuc, ty_le_tam_ung,
                      trang_thai_hop_dong, ngay_ky, created_at)
VALUES (1, 7, 150000000.00, '2026-06-01', '2027-05-31', 0.30,
        'DA_KY', '2026-05-25', NOW() - INTERVAL '10 days');

-- Hợp đồng cho đề tài 8
INSERT INTO hop_dong (id, de_tai_id, kinh_phi, ngay_bat_dau, ngay_ket_thuc, ty_le_tam_ung,
                      trang_thai_hop_dong, ngay_ky, created_at)
VALUES (2, 8, 200000000.00, '2026-05-15', '2027-05-14', 0.30,
        'DA_KY', '2026-05-10', NOW() - INTERVAL '15 days');

SELECT setval('hop_dong_id_seq', 2, true);

-- ============================================================================
-- 5. TỔ PHẢN BIỆN (cho các đề tài ở trạng thái DANG_PHAN_BIEN)
-- ============================================================================

-- Tổ phản biện cho đề tài 3 (DANG_PHAN_BIEN)
INSERT INTO to_phan_bien (id, de_tai_id, deadline_nop, ket_qua_tong_hop, created_at, updated_at)
VALUES (1, 3, CURRENT_DATE + INTERVAL '14 days', 'CHUA_CO',
        NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days');

-- Tổ phản biện cho đề tài 4 (DANG_PHAN_BIEN)
INSERT INTO to_phan_bien (id, de_tai_id, deadline_nop, ket_qua_tong_hop, created_at, updated_at)
VALUES (2, 4, CURRENT_DATE + INTERVAL '10 days', 'CHUA_CO',
        NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

SELECT setval('to_phan_bien_id_seq', 2, true);

-- ============================================================================
-- 6. THÀNH VIÊN TỔ PHẢN BIỆN
-- ============================================================================

-- Hội đồng 1 (đề tài 3): pb01 và pb02
INSERT INTO thanh_vien_to_phan_bien (id, to_phan_bien_id, nguoi_dung_id, ket_qua, created_at, updated_at)
VALUES (1, 1, 8, 'CHUA_NOP', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days');

INSERT INTO thanh_vien_to_phan_bien (id, to_phan_bien_id, nguoi_dung_id, ket_qua, created_at, updated_at)
VALUES (2, 1, 9, 'CHUA_NOP', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days');

-- Hội đồng 2 (đề tài 4): pb01 (đã nộp) và pb02 (chưa nộp)
INSERT INTO thanh_vien_to_phan_bien (id, to_phan_bien_id, nguoi_dung_id, ket_qua, nhan_xet, ngay_nop, created_at, updated_at)
VALUES (3, 2, 8, 'CHAP_NHAN',
        'Đề tài có tính khả thi cao, phương pháp nghiên cứu hợp lý và rõ ràng.',
        NOW() - INTERVAL '2 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '2 days');

INSERT INTO thanh_vien_to_phan_bien (id, to_phan_bien_id, nguoi_dung_id, ket_qua, created_at, updated_at)
VALUES (4, 2, 9, 'CHUA_NOP', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days');

SELECT setval('thanh_vien_to_phan_bien_id_seq', 4, true);

-- ============================================================================
-- 7. Ý KIẾN PHẢN HỒI (cho đề tài ở trạng thái CHO_BO_SUNG_HO_SO)
-- ============================================================================

-- Phản hồi cho đề tài 2 (CHO_BO_SUNG_HO_SO)
INSERT INTO feedback (id, de_tai_id, loai, noi_dung, deadline_phan_hoi, trang_thai, nguoi_tao_id, created_at)
VALUES (1, 2, 'BO_SUNG_HO_SO',
        'Hồ sơ thiếu các tài liệu sau:\n1. Thuyết minh chi tiết về phương pháp nghiên cứu\n2. Kinh nghiệm và năng lực của nhóm thực hiện\n3. Dự toán kinh phí chi tiết theo từng hạng mục\n\nVui lòng bổ sung đầy đủ trong vòng 7 ngày.',
        CURRENT_DATE + INTERVAL '4 days', 'CHO_XU_LY', 7,
        NOW() - INTERVAL '3 days');

SELECT setval('feedback_id_seq', 1, true);

-- ============================================================================
-- Hoàn tất khởi tạo dữ liệu thử nghiệm
-- ============================================================================

-- Kiểm tra số lượng bản ghi đã nạp
SELECT 'Tài khoản' AS danh_muc, COUNT(*) AS so_luong FROM nguoi_dung WHERE id >= 5
UNION ALL
SELECT 'Kỳ NCKH', COUNT(*) FROM ky_nckh
UNION ALL
SELECT 'Đề tài', COUNT(*) FROM de_tai
UNION ALL
SELECT 'Hợp đồng', COUNT(*) FROM hop_dong
UNION ALL
SELECT 'Tổ Phản biện', COUNT(*) FROM to_phan_bien
UNION ALL
SELECT 'Thành viên TPB', COUNT(*) FROM thanh_vien_to_phan_bien
UNION ALL
SELECT 'Phản hồi', COUNT(*) FROM feedback;

-- Hiển thị phân bổ đề tài theo từng trạng thái
SELECT trang_thai, COUNT(*) AS so_luong
FROM de_tai
GROUP BY trang_thai
ORDER BY so_luong DESC;
