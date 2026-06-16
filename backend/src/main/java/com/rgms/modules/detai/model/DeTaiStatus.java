package com.rgms.modules.detai.model;

/**
 * Toàn bộ 20 trạng thái vòng đời đề tài NCKH.
 * Khớp 100% với state-machine.md và er-diagram.md (Enum status của DeTai).
 *
 * NHÓM TRẠNG THÁI:
 *   Khởi tạo    : DRAFT
 *   Trung gian  : CHO_PNCKH_XEM_XET → DANG_PHAN_BIEN → DANG_LAP_HOP_DONG
 *                 → DANG_THUC_HIEN → CHO_NGHIEM_THU → CHO_QUYET_TOAN → DANG_QUYET_TOAN
 *   Terminal ✅  : HOAN_TAT
 *   Terminal ❌  : BI_TU_CHOI, DA_HUY, DA_RUT, KHONG_NGHIEM_THU, BI_TREO, BI_THU_HOI
 */
public enum DeTaiStatus {

    // ── Khởi tạo ────────────────────────────────────────────────────────────
    DRAFT,

    // ── Hồ sơ & Sơ thẩm ─────────────────────────────────────────────────────
    CHO_PNCKH_XEM_XET,
    DANG_XEM_XET_BOI_PNCKH,
    CHO_BO_SUNG_HO_SO,

    // ── Phản biện ────────────────────────────────────────────────────────────
    DANG_PHAN_BIEN,
    CHO_CHINH_SUA_THUYET_MINH,

    // ── Ký hợp đồng & Thực hiện ──────────────────────────────────────────────
    DANG_LAP_HOP_DONG,
    DANG_THUC_HIEN,

    // ── Nghiệm thu ───────────────────────────────────────────────────────────
    CHO_NGHIEM_THU,
    CHO_CHINH_SUA_SAU_NGHIEM_THU,

    // ── Quyết toán ───────────────────────────────────────────────────────────
    CHO_QUYET_TOAN,
    DANG_QUYET_TOAN,
    CHO_HOAN_TRA_TAM_UNG,

    // ── Terminal — Thành công ─────────────────────────────────────────────────
    HOAN_TAT,

    // ── Terminal — Thất bại / Ngoại lệ ──────────────────────────────────────
    BI_TU_CHOI,
    DA_HUY,
    DA_RUT,
    KHONG_NGHIEM_THU,
    BI_TREO,
    BI_THU_HOI;

    public boolean isTerminal() {
        return this == HOAN_TAT
                || this == BI_TU_CHOI
                || this == DA_HUY
                || this == DA_RUT
                || this == KHONG_NGHIEM_THU
                || this == BI_TREO
                || this == BI_THU_HOI;
    }
}
