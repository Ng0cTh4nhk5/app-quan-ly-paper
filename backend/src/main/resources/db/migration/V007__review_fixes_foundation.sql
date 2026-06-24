CREATE TABLE don_vi (
    id         BIGSERIAL PRIMARY KEY,
    ten_don_vi VARCHAR(255) NOT NULL,
    ma_don_vi  VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO don_vi (id, ten_don_vi, ma_don_vi) VALUES
    (1, 'Khoa Công nghệ thông tin', 'CNTT'),
    (2, 'Phòng Nghiên cứu khoa học', 'PNCKH'),
    (3, 'Tổ phản biện', 'TPB'),
    (4, 'Ban quản trị hệ thống', 'ADMIN'),
    (5, 'Phòng Kế toán', 'PKT'),
    (6, 'Hội đồng nghiệm thu', 'HDNT');

SELECT setval('don_vi_id_seq', 6, true);

ALTER TABLE nguoi_dung ADD COLUMN don_vi_id BIGINT REFERENCES don_vi(id);

UPDATE nguoi_dung SET don_vi_id = 1 WHERE id = 1;
UPDATE nguoi_dung SET don_vi_id = 2 WHERE id = 2;
UPDATE nguoi_dung SET don_vi_id = 3 WHERE id IN (3, 4);

INSERT INTO nguoi_dung (username, password_hash, ho_ten, email, vai_tro, don_vi_id)
VALUES ('admin', '{noop}password', 'Quản trị viên', 'admin@test.com', 'ADMIN', 4)
ON CONFLICT (username) DO NOTHING;

SELECT setval('nguoi_dung_id_seq', GREATEST((SELECT MAX(id) FROM nguoi_dung), 1), true);

ALTER TABLE nguoi_dung DROP CONSTRAINT chk_nguoi_dung_vai_tro;
ALTER TABLE nguoi_dung
    ADD CONSTRAINT chk_nguoi_dung_vai_tro
        CHECK (vai_tro IN (
            'GIANG_VIEN',
            'NCKH',
            'TO_PHAN_BIEN',
            'ADMIN',
            'LANH_DAO',
            'PHONG_KE_TOAN',
            'HOI_DONG_NGHIEM_THU'
        ));
ALTER TABLE nguoi_dung ALTER COLUMN don_vi_id SET NOT NULL;

ALTER TABLE de_tai ADD COLUMN don_vi_id BIGINT REFERENCES don_vi(id);
UPDATE de_tai dt
SET don_vi_id = nd.don_vi_id
FROM nguoi_dung nd
WHERE dt.chu_nhiem_id = nd.id;
ALTER TABLE de_tai ALTER COLUMN don_vi_id SET NOT NULL;
CREATE INDEX idx_de_tai_don_vi ON de_tai(don_vi_id);

CREATE SEQUENCE de_tai_ma_so_seq START WITH 1 INCREMENT BY 1;
SELECT setval(
    'de_tai_ma_so_seq',
    COALESCE((
        SELECT MAX(CAST(SUBSTRING(ma_so FROM '[0-9]+$') AS BIGINT))
        FROM de_tai
        WHERE ma_so ~ '[0-9]+$'
    ), 0) + 1,
    false
);

ALTER TABLE audit_log
    ADD COLUMN severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    ADD COLUMN ghi_chu TEXT;

CREATE TABLE cau_hinh_he_thong (
    id           BIGSERIAL PRIMARY KEY,
    ma_cau_hinh  VARCHAR(100) NOT NULL UNIQUE,
    gia_tri      TEXT NOT NULL,
    mo_ta        VARCHAR(500),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO cau_hinh_he_thong (ma_cau_hinh, gia_tri, mo_ta) VALUES
    ('MAX_FILE_SIZE_MB', '20', 'Dung lượng file upload tối đa tính bằng MB'),
    ('DRAFT_TIMEOUT_DAYS', '30', 'Số ngày tối đa đề tài được giữ ở trạng thái nháp');

CREATE TABLE bieu_mau_template (
    id          BIGSERIAL PRIMARY KEY,
    ma_bieu_mau VARCHAR(100) NOT NULL UNIQUE,
    ten_bieu_mau VARCHAR(255) NOT NULL,
    loai_file   VARCHAR(50) NOT NULL,
    file_path   VARCHAR(1000),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO bieu_mau_template (ma_bieu_mau, ten_bieu_mau, loai_file, file_path) VALUES
    ('THUYET_MINH_DE_TAI', 'Mẫu thuyết minh đề tài NCKH', 'THUYET_MINH', 'templates/thuyet-minh-de-tai.docx');
