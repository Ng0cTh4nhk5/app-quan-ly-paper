CREATE SEQUENCE IF NOT EXISTS de_tai_ma_so_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 10;

CREATE TABLE de_tai (
    id          BIGSERIAL PRIMARY KEY,
    ma_so       VARCHAR(20) UNIQUE NOT NULL,
    ten_de_tai  VARCHAR(500) NOT NULL,
    mo_ta       TEXT,
    linh_vuc    VARCHAR(100),
    trang_thai  VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    chu_nhiem_id BIGINT NOT NULL REFERENCES users(id),
    don_vi_id    BIGINT REFERENCES don_vi(id),
    ky_nckh_id   BIGINT NOT NULL REFERENCES ky_nckh(id),
    gv_da_dong_y_hop_dong BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS phan_bien_de_xuat (
    id                  BIGSERIAL    PRIMARY KEY,
    de_tai_id           BIGINT       NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    ho_ten              VARCHAR(150) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    don_vi_chuyen_nganh VARCHAR(200),
    ngay_tao            TIMESTAMP    NOT NULL DEFAULT NOW(),
    ngay_cap_nhat       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_phan_bien_de_xuat_de_tai ON phan_bien_de_xuat(de_tai_id);
