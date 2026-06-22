CREATE TABLE to_phan_bien (
    id          BIGSERIAL PRIMARY KEY,
    de_tai_id   BIGINT NOT NULL REFERENCES de_tai(id),
    thanh_vien_id BIGINT NOT NULL REFERENCES users(id),
    da_nop_ket_qua BOOLEAN DEFAULT FALSE,
    ket_qua     VARCHAR(20),   -- CHAP_NHAN, SUA_NHO, SUA_LON, TU_CHOI
    nhan_xet    TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    de_tai_id   BIGINT REFERENCES de_tai(id),
    actor_id    BIGINT REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,
    tu_trang_thai VARCHAR(50),
    sang_trang_thai VARCHAR(50),
    ghi_chu     TEXT,
    severity    VARCHAR(20) DEFAULT 'INFO',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action);
