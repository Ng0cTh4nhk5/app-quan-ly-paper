package com.rgms.shared.enums;

/**
 * Sự kiện kích hoạt chuyển trạng thái trong FSM đề tài (Phase 1 — Luồng 1).
 * Naming convention: {ACTOR}_{HANH_DONG} hoặc HE_THONG_{ACTION}.
 *
 * Khớp với sop-member-a.md và state-machine.md (Bảng 3. Transition).
 */
public enum TopicEvent {

    // ── Hồ sơ & Sơ thẩm ─────────────────────────────────────────────────────
    GV_GUI_HO_SO,           // GV gửi hồ sơ đến P.NCKH  (DRAFT → CHO_PNCKH_XEM_XET)
    PNCKH_TIEP_NHAN,        // P.NCKH bấm tiếp nhận      (CHO_PNCKH → DANG_XEM_XET)
    PNCKH_YEU_CAU_BO_SUNG,  // P.NCKH yêu cầu bổ sung    (DANG_XEM_XET → CHO_BO_SUNG)
    GV_NOP_BO_SUNG,         // GV nộp lại hồ sơ          (CHO_BO_SUNG → DANG_XEM_XET)
    PNCKH_TU_CHOI,          // P.NCKH từ chối thẳng       (DANG_XEM_XET / DANG_PB → BI_TU_CHOI)

    // ── Phản biện ────────────────────────────────────────────────────────────
    PNCKH_LAP_TO_PB,        // P.NCKH lập tổ phản biện   (DANG_XEM_XET → DANG_PHAN_BIEN)
    PB_NOP_KET_QUA,         // Tổ PB nộp kết quả         (không đổi state DeTai, cập nhật ToPhanBien)
    PNCKH_ACCEPT_PB,        // P.NCKH chấp nhận KQ PB    (DANG_PB → DANG_LAP_HOP_DONG)
    PNCKH_YEU_CAU_SUA_TM,   // P.NCKH yêu cầu sửa TM    (DANG_PB → CHO_CHINH_SUA_TM)
    GV_NOP_SUA_TM,          // GV nộp thuyết minh đã sửa (CHO_CHINH_SUA_TM → DANG_PB)

    // ── Hợp đồng ─────────────────────────────────────────────────────────────
    PNCKH_SOAN_HD,          // P.NCKH soạn hợp đồng xong (không đổi state DeTai, tạo HopDong)
    GV_DONG_Y_HD,           // GV đồng ý hợp đồng        (DANG_LAP_HOP_DONG → DANG_LAP_HOP_DONG, set flag)
    HAI_BEN_KY_HD,          // Hai bên ký HĐ hoàn tất    (DANG_LAP_HOP_DONG → DANG_THUC_HIEN)

    // ── Chấm dứt / Ngoại lệ ─────────────────────────────────────────────────
    HE_THONG_TREO,          // Hệ thống auto-treo khi quá hạn (E1, E2, E6, E8)
    GV_RUT;                 // GV chủ động rút đề tài (E21)
}
