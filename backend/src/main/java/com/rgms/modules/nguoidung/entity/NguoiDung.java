package com.rgms.modules.nguoidung.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity NguoiDung — người dùng hệ thống (7 roles).
 * Canonical location: modules/nguoidung/entity/
 *
 * Canonical location duy nhất — không tạo thêm NguoiDung ở package khác.
 */
@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Vai trò người dùng — String để tương thích với cả RoleGuard và Legacy code.
     * Giá trị: GIANG_VIEN | NCKH | TO_PHAN_BIEN | TRUONG_KHOA | BAN_GIAM_HIEU | KE_TOAN | ADMIN
     */
    @Column(name = "vai_tro", nullable = false, length = 50)
    private String vaiTro;

    @Column(name = "loai_tai_khoan", nullable = false, length = 20)
    private String loaiTaiKhoan = "CHINH_THUC"; // CHINH_THUC | TAM_THOI

    @Column(name = "hieu_luc_den")
    private LocalDateTime hieuLucDen; // null với CHINH_THUC

    @Column(name = "ma_giang_vien", length = 50)
    private String maGiangVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_vi_id")
    private DonVi donVi;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "HOAT_DONG"; // HOAT_DONG | DA_KHOA

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
