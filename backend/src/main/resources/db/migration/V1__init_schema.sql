-- =============================================================================
-- V1__init_schema.sql
-- RGMS Database Schema — Flyway Migration V1
-- Tham chiếu: docs/reality/analysis/er-diagram.md
-- Tất cả PK dạng UUID, quan hệ theo Business Rules BR-01 đến BR-15
-- =============================================================================

-- Extension for UUID support (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- MASTER DATA TABLES
-- =============================================================================

-- [Entity 3] DonVi — Khoa/Phòng ban
CREATE TABLE don_vi (
    id              UUID PRIMARY KEY,
    ten             VARCHAR(200) NOT NULL,
    ma_don_vi       VARCHAR(50)  UNIQUE NOT NULL,
    truong_don_vi_id UUID        REFERENCES nguoi_dung(id) ON DELETE SET NULL, -- FK added after nguoi_dung
    ngay_tao        TIMESTAMP    NOT NULL,
    ngay_cap_nhat   TIMESTAMP
);

-- [Entity 2] NguoiDung — Người dùng hệ thống (7 roles + tài khoản tạm)
CREATE TABLE nguoi_dung (
    id              UUID PRIMARY KEY,
    ho_ten          VARCHAR(150)  NOT NULL,
    email           VARCHAR(150)  UNIQUE NOT NULL,
    mat_khau_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(30)   NOT NULL,       -- Enum: GIANG_VIEN, PNCKH, KE_TOAN, ADMIN, LANH_DAO, HOI_DONG
    loai_tai_khoan  VARCHAR(20)   NOT NULL DEFAULT 'CHINH_THUC', -- CHINH_THUC | TAM_THOI
    hieu_luc_den    TIMESTAMP     ,               -- Non-null chỉ với tài khoản tạm
    ma_giang_vien   VARCHAR(50)   ,               -- Nullable với tài khoản tạm
    don_vi_id       UUID          REFERENCES don_vi(id) ON DELETE SET NULL,
    trang_thai      VARCHAR(20)   NOT NULL DEFAULT 'HOAT_DONG', -- HOAT_DONG | DA_KHOA
    ngay_tao        TIMESTAMP     NOT NULL,
    ngay_cap_nhat   TIMESTAMP
);

-- Add FK on don_vi.truong_don_vi_id now that nguoi_dung exists
ALTER TABLE don_vi
    ADD CONSTRAINT fk_don_vi_truong FOREIGN KEY (truong_don_vi_id) REFERENCES nguoi_dung(id) ON DELETE SET NULL;

-- [Entity 4] KyNCKH — Kỳ Nghiên Cứu Khoa Học
CREATE TABLE ky_nckh (
    id                      UUID PRIMARY KEY,
    ten                     VARCHAR(200)  NOT NULL, -- "Kỳ NCKH 2025–2026 (HK1)"
    nam_hoc                 VARCHAR(20)   NOT NULL, -- "2025-2026"
    ngay_bat_dau_dang_ky    DATE          NOT NULL,
    ngay_ket_thuc_dang_ky   DATE          NOT NULL,
    trang_thai              VARCHAR(20)   NOT NULL DEFAULT 'SAP_MO', -- SAP_MO | DANG_MO | DA_DONG
    ngay_tao                TIMESTAMP     NOT NULL,
    ngay_cap_nhat           TIMESTAMP
);

-- [Entity 5] CauHinhHeThong — Cấu hình hệ thống (Admin quản lý)
CREATE TABLE cau_hinh_he_thong (
    id              UUID PRIMARY KEY,
    khoa            VARCHAR(100)  UNIQUE NOT NULL,   -- "max_advance_rate", "feedback_deadline_days"
    gia_tri         VARCHAR(500)  NOT NULL,
    mo_ta           TEXT,
    cap_nhat_boi_id UUID          NOT NULL REFERENCES nguoi_dung(id),
    ngay_cap_nhat   TIMESTAMP     NOT NULL,
    ngay_tao        TIMESTAMP     NOT NULL
);

-- [Entity 17] BieuMauTemplate — Template biểu mẫu (hợp đồng, nghiệm thu, thanh lý)
CREATE TABLE bieu_mau_template (
    id                  UUID PRIMARY KEY,
    ten                 VARCHAR(300)  NOT NULL,
    giai_doan           VARCHAR(30)   NOT NULL, -- DANG_KY | HOP_DONG | NGHIEM_THU | QUYET_TOAN | KHAC
    file_path           VARCHAR(500)  NOT NULL,
    cac_truong_tu_dong  JSONB,                  -- {"{{TEN_GV}}": "NguoiDung.ho_ten", ...}
    phien_ban           VARCHAR(20)   NOT NULL DEFAULT '1.0',
    ngay_cap_nhat       TIMESTAMP     NOT NULL,
    ngay_tao            TIMESTAMP     NOT NULL
);

-- =============================================================================
-- CORE — ĐỀ TÀI (Central entity of the system)
-- =============================================================================

-- [Entity 1] DeTai
CREATE TABLE de_tai (
    id                          UUID PRIMARY KEY,
    ma_so                       VARCHAR(50)     UNIQUE,   -- Auto-generated: NCKH-{YEAR}-{SEQ}
    ten_de_tai                  VARCHAR(500)    NOT NULL,
    status                      VARCHAR(50)     NOT NULL DEFAULT 'DRAFT',
    ngay_tao                    TIMESTAMP       NOT NULL,
    ngay_bat_dau                DATE,                     -- Set khi ký hợp đồng
    ngay_ket_thuc               DATE,                     -- Deadline thực hiện (cập nhật khi gia hạn)
    kinh_phi                    DECIMAL(15, 2),            -- Tổng kinh phí theo hợp đồng
    tong_tam_ung_da_giai_ngan   DECIMAL(15, 2)  NOT NULL DEFAULT 0, -- Denormalized check
    chu_nhiem_id                UUID            NOT NULL REFERENCES nguoi_dung(id) ON DELETE RESTRICT,
    don_vi_id                   UUID            NOT NULL REFERENCES don_vi(id),
    ky_nckh_id                  UUID            NOT NULL REFERENCES ky_nckh(id),
    nguon_cho_hoan_tra          VARCHAR(30),              -- HUY | RUT | KHONG_NGHIEM_THU | DU_TAM_UNG
    ngay_cap_nhat               TIMESTAMP
);

CREATE INDEX idx_de_tai_status ON de_tai(status);
CREATE INDEX idx_de_tai_chu_nhiem ON de_tai(chu_nhiem_id);
CREATE INDEX idx_de_tai_ky_nckh ON de_tai(ky_nckh_id);

-- =============================================================================
-- HỢP ĐỒNG & TÀI CHÍNH
-- =============================================================================

-- [Entity 6] HopDong — Hợp đồng thực hiện đề tài (1-1 với DeTai)
CREATE TABLE hop_dong (
    id                      UUID PRIMARY KEY,
    de_tai_id               UUID         UNIQUE NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    so_hieu                 VARCHAR(100) NOT NULL,
    ngay_ky                 DATE,
    ngay_bat_dau            DATE         NOT NULL,
    ngay_ket_thuc           DATE         NOT NULL,
    max_advance_rate        DECIMAL(5,4) NOT NULL DEFAULT 0.5, -- 0.0–1.0; overrides system config
    file_scan_path          VARCHAR(500),                      -- File HĐ đã ký, scan
    bien_ban_thanh_ly_path  VARCHAR(500),
    status                  VARCHAR(20)  NOT NULL DEFAULT 'DANG_SOAN', -- DANG_SOAN | CHO_KY | DA_KY | DA_THANH_LY
    ngay_tao                TIMESTAMP    NOT NULL,
    ngay_cap_nhat           TIMESTAMP
);

-- [Entity 11] TamUng — Đề nghị tạm ứng (nhiều đợt per DeTai)
CREATE TABLE tam_ung (
    id              UUID         PRIMARY KEY,
    de_tai_id       UUID         NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    dot_thu         INTEGER      NOT NULL,      -- Đợt 1, 2, 3... (sequence per de_tai)
    so_tien         DECIMAL(15,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'CHO_DUYET', -- CHO_DUYET | DA_GIAI_NGAN | DA_THU_HOI | BI_TU_CHOI
    ngay_tao        TIMESTAMP    NOT NULL,
    ngay_giai_ngan  TIMESTAMP,
    nguoi_duyet_id  UUID         REFERENCES nguoi_dung(id),   -- Kế toán xử lý
    ghi_chu         TEXT,
    UNIQUE (de_tai_id, dot_thu)   -- dot_thu must be sequential per project (BR-03)
);

-- [Entity 12] ThuHoiTamUng — Thu hồi khi đề tài thất bại (1-1 với DeTai)
CREATE TABLE thu_hoi_tam_ung (
    id              UUID         PRIMARY KEY,
    de_tai_id       UUID         UNIQUE NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    nguon           VARCHAR(30)  NOT NULL,      -- HUY | RUT | KHONG_NGHIEM_THU
    tong_can_hoan   DECIMAL(15,2) NOT NULL,
    so_tien_da_thu  DECIMAL(15,2) NOT NULL DEFAULT 0,
    han_hoan_tra    DATE         NOT NULL,
    trang_thai      VARCHAR(20)  NOT NULL DEFAULT 'CHO_HOAN_TRA', -- CHO_HOAN_TRA | DA_HOAN_TRA | GHI_NO
    ngay_tao        TIMESTAMP    NOT NULL,
    nguoi_xu_ly_id  UUID         NOT NULL REFERENCES nguoi_dung(id),
    ghi_chu         TEXT
);

-- [Entity 13] GiaHanThucHien — Gia hạn thực hiện (nhiều lần per DeTai)
CREATE TABLE gia_han_thuc_hien (
    id                      UUID         PRIMARY KEY,
    de_tai_id               UUID         NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    ly_do                   TEXT         NOT NULL,
    deadline_cu             DATE         NOT NULL,
    deadline_moi_de_nghi    DATE         NOT NULL,
    deadline_duoc_duyet     DATE,                 -- Set sau khi PNCKH phê duyệt
    trang_thai              VARCHAR(20)  NOT NULL DEFAULT 'CHO_DUYET', -- CHO_DUYET | CHAP_THUAN | TU_CHOI
    nguoi_tao_id            UUID         NOT NULL REFERENCES nguoi_dung(id),
    nguoi_duyet_id          UUID         REFERENCES nguoi_dung(id),
    ngay_tao                TIMESTAMP    NOT NULL,
    ngay_duyet              TIMESTAMP
);

-- [Entity 14] QuyetToan — Quyết toán (1-1 với DeTai)
CREATE TABLE quyet_toan (
    id                  UUID         PRIMARY KEY,
    de_tai_id           UUID         UNIQUE NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    tong_chi_phi        DECIMAL(15,2) NOT NULL,
    so_du_can_hoan_tra  DECIMAL(15,2) NOT NULL DEFAULT 0,  -- = tam_ung_giai_ngan - tong_chi_phi nếu > 0
    status              VARCHAR(20)  NOT NULL DEFAULT 'CHO_DUYET', -- CHO_DUYET | CHO_PHAN_DU | DA_THANH_TOAN | TU_CHOI
    ngay_tao            TIMESTAMP    NOT NULL,
    ngay_xac_nhan       TIMESTAMP,
    nguoi_xu_ly_id      UUID         REFERENCES nguoi_dung(id),
    ngay_cap_nhat       TIMESTAMP
);

-- =============================================================================
-- PHẢN BIỆN
-- =============================================================================

-- [Entity 7] ToPhanBien — Tổ phản biện (1-1 với DeTai tại giai đoạn phản biện)
CREATE TABLE to_phan_bien (
    id                  UUID         PRIMARY KEY,
    de_tai_id           UUID         UNIQUE NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    ngay_tao            TIMESTAMP    NOT NULL,
    deadline_nop        DATE         NOT NULL,
    ket_qua_tong_hop    VARCHAR(20)  NOT NULL DEFAULT 'CHUA_CO', -- CHUA_CO | CHAP_NHAN | CAN_SUA | TU_CHOI
    ghi_chu_pnckh       TEXT
);

-- [Entity 8] ThanhVienToPhanBien — Thành viên tổ phản biện
CREATE TABLE thanh_vien_to_phan_bien (
    id              UUID         PRIMARY KEY,
    to_phan_bien_id UUID         NOT NULL REFERENCES to_phan_bien(id) ON DELETE CASCADE,
    nguoi_dung_id   UUID         NOT NULL REFERENCES nguoi_dung(id) ON DELETE RESTRICT,
    ket_qua         VARCHAR(20)  NOT NULL DEFAULT 'CHUA_NOP', -- CHUA_NOP | CHAP_NHAN | CAN_SUA | TU_CHOI
    nhan_xet        TEXT,
    ngay_nop        TIMESTAMP,
    UNIQUE (to_phan_bien_id, nguoi_dung_id)
);

-- =============================================================================
-- NGHIỆM THU
-- =============================================================================

-- [Entity 9] HoiDongNghiemThu — Hội đồng nghiệm thu (1-1 với DeTai)
CREATE TABLE hoi_dong_nghiem_thu (
    id              UUID         PRIMARY KEY,
    de_tai_id       UUID         UNIQUE NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    ngay_hop        DATE         NOT NULL,
    ngay_ket_thuc   TIMESTAMP,              -- Trigger đóng tài khoản tạm sau khi session kết thúc
    ket_qua         VARCHAR(30)  NOT NULL DEFAULT 'CHUA_CO', -- CHUA_CO | DAT_KHONG_SUA | DAT_SUA_NHO | CAN_BO_SUNG | KHONG_DAT
    bien_ban_path   VARCHAR(500),
    ngay_tao        TIMESTAMP    NOT NULL,
    ngay_cap_nhat   TIMESTAMP
);

-- [Entity 10] ThanhVienHoiDong — Thành viên hội đồng nghiệm thu
CREATE TABLE thanh_vien_hoi_dong (
    id              UUID         PRIMARY KEY,
    hoi_dong_id     UUID         NOT NULL REFERENCES hoi_dong_nghiem_thu(id) ON DELETE CASCADE,
    nguoi_dung_id   UUID         NOT NULL REFERENCES nguoi_dung(id) ON DELETE RESTRICT,
    vai_tro         VARCHAR(20)  NOT NULL,  -- CHU_TICH | THU_KY | UY_VIEN | PHAN_BIEN
    da_chap_thuan   BOOLEAN,                -- NULL for THU_KY/UY_VIEN; used for CHU_TICH & PHAN_BIEN (BR-11)
    nhan_xet        TEXT,
    ngay_tao        TIMESTAMP    NOT NULL,
    UNIQUE (hoi_dong_id, nguoi_dung_id)
);

-- =============================================================================
-- HỖ TRỢ
-- =============================================================================

-- [Entity 15] TaiLieu — File đính kèm
CREATE TABLE tai_lieu (
    id              UUID         PRIMARY KEY,
    de_tai_id       UUID         NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    loai            VARCHAR(30)  NOT NULL, -- THUYET_MINH | BAO_CAO | MINH_CHUNG_NT | MINH_CHUNG_QT | HOP_DONG_SCAN | BIEN_BAN_NT | BIEN_BAN_THANH_LY | BIEU_MAU_KHAC
    ten_file        VARCHAR(300) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    dinh_dang       VARCHAR(20)  NOT NULL, -- pdf | docx | xlsx | jpg | png
    kich_thuoc_byte BIGINT       NOT NULL,
    nguoi_upload_id UUID         NOT NULL REFERENCES nguoi_dung(id),
    ngay_upload     TIMESTAMP    NOT NULL,
    phien_ban       INTEGER      NOT NULL DEFAULT 1  -- Auto-increments per (de_tai_id, loai) (BR-14)
);

CREATE INDEX idx_tai_lieu_de_tai ON tai_lieu(de_tai_id, loai);

-- [Entity 16] Feedback — Phản hồi yêu cầu bổ sung/chỉnh sửa
CREATE TABLE feedback (
    id                  UUID         PRIMARY KEY,
    de_tai_id           UUID         NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    noi_dung            TEXT         NOT NULL,
    loai                VARCHAR(30)  NOT NULL, -- BO_SUNG_HO_SO | PHAN_BIEN | HOP_DONG | NGHIEM_THU | QUYET_TOAN
    nguoi_tao_id        UUID         NOT NULL REFERENCES nguoi_dung(id),
    deadline_phan_hoi   DATE,
    trang_thai          VARCHAR(20)  NOT NULL DEFAULT 'CHO_XU_LY', -- CHO_XU_LY | DA_XU_LY
    ngay_tao            TIMESTAMP    NOT NULL,
    ngay_cap_nhat       TIMESTAMP
);

-- [Entity 18] AuditLog — Nhật ký kiểm toán bất biến (Append-Only — BR-15)
CREATE TABLE audit_log (
    id                  UUID         PRIMARY KEY,
    de_tai_id           UUID         REFERENCES de_tai(id),   -- NULL for system-level actions
    actor_id            UUID         NOT NULL REFERENCES nguoi_dung(id),
    hanh_dong           VARCHAR(100) NOT NULL,  -- e.g., GV_SUBMIT_HO_SO, PNCKH_TIEP_NHAN
    trang_thai_truoc    VARCHAR(50),
    trang_thai_sau      VARCHAR(50),
    meta                JSONB,                  -- Additional context (amount, reason, etc.)
    thoi_gian           TIMESTAMP    NOT NULL
);

-- AuditLog: disable UPDATE and DELETE via rule (enforce BR-15 Append-Only)
CREATE RULE audit_log_no_delete AS ON DELETE TO audit_log DO INSTEAD NOTHING;
CREATE RULE audit_log_no_update AS ON UPDATE TO audit_log DO INSTEAD NOTHING;

CREATE INDEX idx_audit_log_de_tai ON audit_log(de_tai_id);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_id);
CREATE INDEX idx_audit_log_thoi_gian ON audit_log(thoi_gian DESC);
