CREATE TABLE audit_log (
    id             BIGSERIAL PRIMARY KEY,
    de_tai_id      BIGINT NOT NULL REFERENCES de_tai(id),
    hanh_dong      VARCHAR(100) NOT NULL,
    actor_id       BIGINT REFERENCES nguoi_dung(id),
    tu_trang_thai  VARCHAR(50),
    sang_trang_thai VARCHAR(50),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_de_tai ON audit_log(de_tai_id);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_id);
