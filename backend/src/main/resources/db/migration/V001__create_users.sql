CREATE TABLE nguoi_dung (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    ho_ten        VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    vai_tro       VARCHAR(50) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_nguoi_dung_vai_tro
        CHECK (vai_tro IN ('GIANG_VIEN', 'NCKH', 'TO_PHAN_BIEN', 'ADMIN'))
);

INSERT INTO nguoi_dung (id, username, password_hash, ho_ten, email, vai_tro) VALUES
    (1, 'gv_a', '{noop}password', 'Nguyễn Văn A', 'gv_a@test.com', 'GIANG_VIEN'),
    (2, 'nckh_b', '{noop}password', 'Trần Thị B', 'nckh_b@test.com', 'NCKH'),
    (3, 'pb_c', '{noop}password', 'Hoàng Văn C', 'pb_c@test.com', 'TO_PHAN_BIEN'),
    (4, 'pb_d', '{noop}password', 'Hoàng Văn D', 'pb_d@test.com', 'TO_PHAN_BIEN');

SELECT setval('nguoi_dung_id_seq', 4, true);
