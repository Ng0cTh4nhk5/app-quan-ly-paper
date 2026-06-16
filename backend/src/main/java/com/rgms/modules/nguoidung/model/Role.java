package com.rgms.modules.nguoidung.model;

/**
 * Vai trò trong hệ thống — khớp với 7 Actor trong core-business.md.
 * GIANG_VIEN: Chủ nhiệm đề tài (nộp hồ sơ, xác nhận hợp đồng, tạo tạm ứng)
 * PNCKH:      Phòng NCKH (xem xét, phản biện, lập HĐ nghiệm thu)
 * KE_TOAN:    Phòng Kế toán (xử lý tạm ứng, quyết toán, thu hồi)
 * ADMIN:      Vận hành, cấp tài khoản tạm
 * LANH_DAO:   Xem dashboard báo cáo tổng hợp
 * HOI_DONG:   Tài khoản tạm của thành viên hội đồng nghiệm thu
 */
public enum Role {
    GIANG_VIEN,
    PNCKH,
    KE_TOAN,
    ADMIN,
    LANH_DAO,
    HOI_DONG   // tài khoản tạm — kết hợp với loaiTaiKhoan = TAM_THOI
}
