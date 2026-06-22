package com.rgms.modules.nguoidung.entity;

import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity DonVi — Khoa/Phòng ban trong trường.
 * Canonical location: modules/nguoidung/entity/
 */
@Entity
@Table(name = "don_vi")
@Getter
@Setter
public class DonVi extends BaseEntity {

    @Column(name = "ten", nullable = false, length = 200)
    private String ten;

    @Column(name = "ma_don_vi", nullable = false, unique = true, length = 50)
    private String maDonVi;

    /**
     * Trưởng đơn vị — nullable vì có thể chưa được phân công.
     * Dùng LAZY để tránh N+1 khi chỉ cần DonVi.ten.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truong_don_vi_id")
    private NguoiDung truongDonVi;
}
