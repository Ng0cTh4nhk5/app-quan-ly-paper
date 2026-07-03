CREATE TABLE IF NOT EXISTS phan_bien_de_xuat (
    id        BIGSERIAL PRIMARY KEY,
    de_tai_id BIGINT NOT NULL REFERENCES de_tai(id) ON DELETE CASCADE,
    ho_ten    VARCHAR(255) NOT NULL,
    email     VARCHAR(255),
    co_quan   VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_phan_bien_de_xuat_de_tai ON phan_bien_de_xuat(de_tai_id);
