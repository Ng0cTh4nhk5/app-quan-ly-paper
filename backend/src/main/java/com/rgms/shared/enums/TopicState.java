package com.rgms.shared.enums;

/**
 * Trạng thái vòng đời đề tài NCKH dùng trong FSM (Phase 1 — Luồng 1).
 * Khớp 100% với state-machine.md và sop-member-a.md.
 *
 * Quy tắc bất biến:
 *   - Mọi thay đổi trangThai phải đi qua FsmService.transition() — không bypass.
 *   - Terminal states (BI_TREO, BI_TU_CHOI, DA_RUT, DA_HUY) không có outgoing transition.
 */
public enum TopicState {

    // ── Khởi tạo ─────────────────────────────────────────────────────────────
    DRAFT,

    // ── Hồ sơ & Sơ thẩm ─────────────────────────────────────────────────────
    CHO_PNCKH_XEM_XET,
    DANG_XEM_XET_BOI_PNCKH,
    CHO_BO_SUNG_HO_SO,

    // ── Phản biện ────────────────────────────────────────────────────────────
    DANG_PHAN_BIEN,
    CHO_CHINH_SUA_THUYET_MINH,

    // ── Hợp đồng & Thực hiện ─────────────────────────────────────────────────
    DANG_LAP_HOP_DONG,
    DANG_THUC_HIEN,

    // ── Terminal — Ngoại lệ ──────────────────────────────────────────────────
    BI_TREO,
    BI_TU_CHOI,
    DA_RUT,
    DA_HUY;

    /**
     * Terminal state = không có outgoing transition nào.
     * FsmService dùng để kiểm tra trước khi cho phép bất kỳ transition nào.
     */
    public boolean isTerminal() {
        return this == BI_TREO
                || this == BI_TU_CHOI
                || this == DA_RUT
                || this == DA_HUY;
    }
}
