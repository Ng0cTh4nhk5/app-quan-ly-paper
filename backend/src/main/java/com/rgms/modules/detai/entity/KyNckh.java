package com.rgms.modules.detai.entity;

import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity KyNckh — Kỳ Nghiên Cứu Khoa Học.
 * Tham chiếu: er-diagram.md — Entity 4. KyNCKH
 *
 * Guard F-GV-01 GUARD-3: KyNCKH.trangThai phải == DANG_MO khi GV tạo đề tài.
 */
@Entity
@Table(name = "ky_nckh")
@Getter
@Setter
public class KyNckh extends BaseEntity {

    @Column(name = "ten", nullable = false, length = 200)
    private String ten; // "Kỳ NCKH 2025–2026 (HK1)"

    @Column(name = "nam_hoc", nullable = false, length = 20)
    private String namHoc; // "2025-2026"

    @Column(name = "ngay_bat_dau_dang_ky", nullable = false)
    private LocalDate ngayBatDauDangKy;

    @Column(name = "ngay_ket_thuc_dang_ky", nullable = false)
    private LocalDate ngayKetThucDangKy;

    /**
     * Trạng thái cổng đăng ký.
     * SAP_MO | DANG_MO | DA_DONG
     */
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "SAP_MO";

    public boolean isDangMo() {
        return "DANG_MO".equals(trangThai);
    }
}
