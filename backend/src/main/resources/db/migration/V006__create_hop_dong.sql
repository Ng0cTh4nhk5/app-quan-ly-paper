CREATE TABLE hop_dong (
    id          BIGSERIAL PRIMARY KEY,
    de_tai_id   BIGINT NOT NULL REFERENCES de_tai(id),
    so_hop_dong VARCHAR(50),
    noi_dung    TEXT,
    trang_thai  VARCHAR(30) DEFAULT 'DRAFT',  -- DRAFT, CHO_GV_XAC_NHAN, DA_KY
    file_path   VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
