CREATE TABLE feedback (
    id                 BIGSERIAL PRIMARY KEY,
    de_tai_id           BIGINT NOT NULL REFERENCES de_tai(id),
    loai               VARCHAR(50) NOT NULL,
    noi_dung            TEXT NOT NULL,
    deadline_phan_hoi  TIMESTAMP,
    trang_thai          VARCHAR(50) NOT NULL DEFAULT 'CHO_XU_LY',
    nguoi_tao_id        BIGINT REFERENCES nguoi_dung(id),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_feedback_loai
        CHECK (loai IN ('BO_SUNG_HO_SO', 'HOP_DONG', 'KET_QUA_PB')),
    CONSTRAINT chk_feedback_trang_thai
        CHECK (trang_thai IN ('CHO_XU_LY', 'DA_XU_LY'))
);

CREATE INDEX idx_feedback_de_tai ON feedback(de_tai_id);
CREATE INDEX idx_feedback_de_tai_loai_trang_thai ON feedback(de_tai_id, loai, trang_thai);
