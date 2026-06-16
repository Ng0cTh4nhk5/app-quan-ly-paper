package com.rgms.modules.detai.model;

/**
 * Các sự kiện kích hoạt chuyển trạng thái trong FSM đề tài.
 * Mỗi Event tương ứng với 1 action do Actor thực hiện hoặc hệ thống tự động.
 *
 * Naming: {ACTOR}_{HANH_DONG} hoặc AUTO_{SYSTEM_ACTION}
 */
public enum DeTaiEvent {

    // ── Hồ sơ & Sơ thẩm ─────────────────────────────────────────────────────
    GV_SUBMIT_HO_SO,            // Giảng viên nộp hồ sơ
    PNCKH_TIEP_NHAN,            // P.NCKH tiếp nhận hồ sơ hợp lệ
    PNCKH_TAO_FEEDBACK,         // P.NCKH yêu cầu bổ sung hồ sơ
    GV_NOP_LAI_HO_SO,           // Giảng viên nộp lại sau feedback
    PNCKH_TU_CHOI_HO_SO,        // P.NCKH từ chối thẳng hồ sơ (E3)

    // ── Phản biện ────────────────────────────────────────────────────────────
    PNCKH_LAP_TO_PHAN_BIEN,     // P.NCKH lập tổ phản biện
    PNCKH_DUYET_PHAN_BIEN,      // P.NCKH xét duyệt kết quả phản biện (pass/fail/revision)
    GV_NOP_LAI_THUYET_MINH,     // Giảng viên nộp lại thuyết minh sau phản biện

    // ── Hợp đồng ─────────────────────────────────────────────────────────────
    PNCKH_TAO_HOP_DONG,         // P.NCKH tạo hợp đồng
    GV_KY_HOP_DONG,             // Hai bên đồng thuận, ký hợp đồng

    // ── Thực hiện ────────────────────────────────────────────────────────────
    GV_NOP_BAO_CAO,             // Giảng viên nộp báo cáo kết quả + minh chứng

    // ── Gia hạn ──────────────────────────────────────────────────────────────
    GV_YEU_CAU_GIA_HAN,         // Giảng viên tạo đề nghị gia hạn
    PNCKH_DUYET_GIA_HAN,        // P.NCKH phê duyệt gia hạn

    // ── Nghiệm thu ───────────────────────────────────────────────────────────
    PNCKH_LAP_HOI_DONG,         // P.NCKH lập hội đồng nghiệm thu
    HOIDONG_GHI_KET_QUA,        // Hội đồng ghi kết quả nghiệm thu
    PNCKH_XAC_NHAN_CHINH_SUA_NHO, // P.NCKH xác nhận sửa nhỏ đã đủ
    HOIDONG_DUYET_LAI,          // Chủ tịch + Phản biện duyệt lại sau sửa bổ sung

    // ── Quyết toán ───────────────────────────────────────────────────────────
    GV_TAO_QUYET_TOAN,          // Giảng viên tạo đề nghị quyết toán
    KETOAN_HOAN_TAT_QUYET_TOAN, // Kế toán xác nhận hoàn tất quyết toán

    // ── Hoàn trả tạm ứng ────────────────────────────────────────────────────
    KETOAN_YEU_CAU_HOAN_TRA,    // Kế toán khởi động thu hồi tạm ứng
    KETOAN_XAC_NHAN_HOAN_TRA,   // Kế toán xác nhận đã thu hồi

    // ── Chấm dứt thủ công ───────────────────────────────────────────────────
    GV_RUT_DE_TAI,              // Giảng viên xin rút (E21)
    PNCKH_HUY_DE_TAI,           // P.NCKH hủy đề tài
    ADMIN_DONG_VI_PHAM,         // Admin đóng do vi phạm học thuật (E23)

    // ── Hệ thống tự động ─────────────────────────────────────────────────────
    AUTO_TREO_DE_TAI,           // Hết hạn không phản hồi → BI_TREO (E1, E2, E6, E9)
}
