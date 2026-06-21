CREATE TABLE de_tai (
    id           BIGSERIAL PRIMARY KEY,
    ma_so        VARCHAR(50) NOT NULL UNIQUE,
    ten_de_tai   VARCHAR(500) NOT NULL,
    mo_ta        TEXT,
    linh_vuc     VARCHAR(255),
    trang_thai   VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    chu_nhiem_id BIGINT NOT NULL REFERENCES nguoi_dung(id),
    ky_nckh_id   BIGINT NOT NULL REFERENCES ky_nckh(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_de_tai_trang_thai
        CHECK (trang_thai IN (
            'DRAFT',
            'CHO_PNCKH_XEM_XET',
            'DANG_XEM_XET_BOI_PNCKH',
            'CHO_BO_SUNG_HO_SO',
            'DANG_PHAN_BIEN',
            'CHO_CHINH_SUA_THUYET_MINH',
            'DANG_LAP_HOP_DONG',
            'DANG_THUC_HIEN',
            'BI_TREO',
            'BI_TU_CHOI'
        ))
);

CREATE INDEX idx_de_tai_trang_thai ON de_tai(trang_thai);
CREATE INDEX idx_de_tai_chu_nhiem ON de_tai(chu_nhiem_id);
CREATE INDEX idx_de_tai_ky_nckh ON de_tai(ky_nckh_id);
