CREATE TABLE to_phan_bien (
    id                BIGSERIAL PRIMARY KEY,
    de_tai_id          BIGINT NOT NULL UNIQUE REFERENCES de_tai(id),
    deadline_nop       TIMESTAMP NOT NULL,
    ket_qua_tong_hop   VARCHAR(50) NOT NULL DEFAULT 'CHUA_CO',
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_to_phan_bien_ket_qua_tong_hop
        CHECK (ket_qua_tong_hop IN ('CHUA_CO', 'CHAP_NHAN', 'CAN_SUA', 'TU_CHOI'))
);

CREATE TABLE thanh_vien_to_phan_bien (
    id                 BIGSERIAL PRIMARY KEY,
    to_phan_bien_id     BIGINT NOT NULL REFERENCES to_phan_bien(id) ON DELETE CASCADE,
    nguoi_dung_id       BIGINT NOT NULL REFERENCES nguoi_dung(id),
    ket_qua             VARCHAR(50) NOT NULL DEFAULT 'CHUA_NOP',
    nhan_xet            TEXT,
    nop_luc             TIMESTAMP,

    CONSTRAINT uq_thanh_vien_to_phan_bien UNIQUE (to_phan_bien_id, nguoi_dung_id),
    CONSTRAINT chk_thanh_vien_to_phan_bien_ket_qua
        CHECK (ket_qua IN ('CHUA_NOP', 'CHAP_NHAN', 'CAN_SUA', 'TU_CHOI'))
);

CREATE INDEX idx_thanh_vien_to_phan_bien_user ON thanh_vien_to_phan_bien(nguoi_dung_id);
