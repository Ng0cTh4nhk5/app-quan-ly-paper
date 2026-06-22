CREATE TABLE tai_lieu (
    id          BIGSERIAL PRIMARY KEY,
    de_tai_id   BIGINT NOT NULL REFERENCES de_tai(id),
    loai        VARCHAR(50) NOT NULL,  -- THUYET_MINH, BIEU_MAU, KET_QUA_PB, HOP_DONG
    ten_file    VARCHAR(255) NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    phien_ban   INT NOT NULL DEFAULT 1,
    uploaded_by BIGINT REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
