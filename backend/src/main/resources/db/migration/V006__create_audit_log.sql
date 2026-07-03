CREATE TABLE audit_log (
    id                BIGSERIAL PRIMARY KEY,
    de_tai_id         BIGINT NOT NULL REFERENCES de_tai(id),
    hanh_dong         VARCHAR(100) NOT NULL,
    actor_id          BIGINT REFERENCES nguoi_dung(id),
    trang_thai_truoc  VARCHAR(50),
    trang_thai_sau    VARCHAR(50),
    meta              TEXT,
    thoi_gian         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_de_tai ON audit_log(de_tai_id);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_id);
