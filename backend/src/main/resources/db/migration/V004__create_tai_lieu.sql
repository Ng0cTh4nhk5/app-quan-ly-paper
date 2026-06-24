-- ============================================================
-- V004: Tạo bảng tai_lieu (tài liệu đính kèm đề tài)
-- Merge: Member B structure + Member A column names
-- ============================================================

CREATE TABLE tai_lieu (
    id          BIGSERIAL    PRIMARY KEY,
    de_tai_id   BIGINT       NOT NULL REFERENCES de_tai(id) ON DELETE RESTRICT,
    ten_file    VARCHAR(255) NOT NULL,
    loai_file   VARCHAR(50)  NOT NULL,    -- THUYET_MINH | BIEU_MAU | KET_QUA_PB | HOP_DONG
    file_path   VARCHAR(1000) NOT NULL,
    phien_ban   INT          NOT NULL DEFAULT 1,
    uploaded_by BIGINT       REFERENCES nguoi_dung(id),
    uploaded_at TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tai_lieu_loai_file CHECK (loai_file IN (
        'THUYET_MINH',
        'BIEU_MAU',
        'KET_QUA_PB',
        'HOP_DONG'
    ))
);

CREATE INDEX IF NOT EXISTS idx_tai_lieu_de_tai    ON tai_lieu(de_tai_id);
CREATE INDEX IF NOT EXISTS idx_tai_lieu_loai_file ON tai_lieu(de_tai_id, loai_file);
