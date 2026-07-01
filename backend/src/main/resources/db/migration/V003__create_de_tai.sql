-- ============================================================
-- V003: Tạo bảng de_tai và phan_bien_de_xuat
-- Merge: Kiến trúc Member A (SEQUENCE, cột đầy đủ) + Constraint của Member B
-- ============================================================

CREATE SEQUENCE IF NOT EXISTS de_tai_ma_so_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 10;

CREATE TABLE de_tai (
    id                      BIGSERIAL    PRIMARY KEY,
    ma_so                   VARCHAR(50)  UNIQUE NOT NULL,
    ten_de_tai              VARCHAR(500) NOT NULL,
    mo_ta                   TEXT,
    linh_vuc                VARCHAR(100),
    trang_thai              VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    chu_nhiem_id            BIGINT       NOT NULL REFERENCES nguoi_dung(id),
    ky_nckh_id              BIGINT       NOT NULL REFERENCES ky_nckh(id),
    gv_da_dong_y_hop_dong   BOOLEAN      NOT NULL DEFAULT FALSE,
    ngay_bat_dau            DATE,
    ngay_ket_thuc           DATE,
    kinh_phi                DECIMAL(15,2),
    tong_tam_ung_da_giai_ngan DECIMAL(15,2) NOT NULL DEFAULT 0,
    nguon_cho_hoan_tra      VARCHAR(30),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_de_tai_trang_thai CHECK (trang_thai IN (
        'DRAFT',
        'CHO_PNCKH_XEM_XET',
        'DANG_XEM_XET_BOI_PNCKH',
        'CHO_BO_SUNG_HO_SO',
        'DANG_PHAN_BIEN',
        'CHO_CHINH_SUA_THUYET_MINH',
        'DANG_LAP_HOP_DONG',
        'DANG_THUC_HIEN',
        'CHO_NGHIEM_THU',
        'CHO_CHINH_SUA_SAU_NGHIEM_THU',
        'CHO_QUYET_TOAN',
        'DANG_QUYET_TOAN',
        'CHO_HOAN_TRA_TAM_UNG',
        'HOAN_TAT',
        'BI_TU_CHOI',
        'DA_HUY',
        'DA_RUT',
        'KHONG_NGHIEM_THU',
        'BI_TREO',
        'BI_THU_HOI'
    ))
);

CREATE INDEX IF NOT EXISTS idx_de_tai_trang_thai  ON de_tai(trang_thai);
CREATE INDEX IF NOT EXISTS idx_de_tai_chu_nhiem   ON de_tai(chu_nhiem_id);
CREATE INDEX IF NOT EXISTS idx_de_tai_ky_nckh     ON de_tai(ky_nckh_id);

-- Bảng đề xuất phản biện (GV tự đề xuất trước khi gửi hồ sơ)
CREATE TABLE IF NOT EXISTS phan_bien_de_xuat (
    id                  BIGSERIAL    PRIMARY KEY,
    de_tai_id           BIGINT       NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    ho_ten              VARCHAR(150) NOT NULL,
    email               VARCHAR(150),
    co_quan             VARCHAR(200),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_phan_bien_de_xuat_de_tai ON phan_bien_de_xuat(de_tai_id);
