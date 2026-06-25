package com.rgms.modules.nguoidung.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity DonVi — Khoa/Phòng ban trong trường.
 * Canonical location: modules/nguoidung/entity/
 */
@Entity
@Table(name = "don_vi")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonVi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
