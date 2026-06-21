CREATE TABLE ky_nckh (
    id         BIGSERIAL PRIMARY KEY,
    ten_ky     VARCHAR(255) NOT NULL,
    trang_thai VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_ky_nckh_trang_thai
        CHECK (trang_thai IN ('DANG_MO', 'DA_DONG'))
);

INSERT INTO ky_nckh (id, ten_ky, trang_thai)
VALUES (1, 'NCKH 2026-2027', 'DANG_MO');

SELECT setval('ky_nckh_id_seq', 1, true);
