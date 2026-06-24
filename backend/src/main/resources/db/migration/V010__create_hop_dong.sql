CREATE TABLE hop_dong (
    id                    BIGSERIAL PRIMARY KEY,
    de_tai_id              BIGINT NOT NULL UNIQUE REFERENCES de_tai(id),
    kinh_phi               DECIMAL(19, 2) NOT NULL,
    ngay_bat_dau           DATE NOT NULL,
    ngay_ket_thuc          DATE NOT NULL,
    ty_le_tam_ung          DECIMAL(5, 2) NOT NULL,
    file_scan_path         VARCHAR(1000),
    trang_thai_hop_dong    VARCHAR(50) NOT NULL,
    ngay_ky                DATE,
    created_at             TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_hop_dong_trang_thai
        CHECK (trang_thai_hop_dong IN ('DRAFT', 'CHO_PHAN_HOI', 'CHO_KY', 'DA_KY', 'YEU_CAU_SUA')),
    CONSTRAINT chk_hop_dong_ty_le_tam_ung
        CHECK (ty_le_tam_ung >= 0 AND ty_le_tam_ung <= 1),
    CONSTRAINT chk_hop_dong_ngay
        CHECK (ngay_ket_thuc >= ngay_bat_dau)
);
