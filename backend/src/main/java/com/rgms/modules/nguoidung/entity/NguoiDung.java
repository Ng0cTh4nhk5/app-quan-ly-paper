package com.rgms.modules.nguoidung.entity;

import com.rgms.modules.nguoidung.model.Role;
import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
public class NguoiDung extends BaseEntity {

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "mat_khau_hash", nullable = false, length = 255)
    private String matKhauHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Column(name = "loai_tai_khoan", nullable = false, length = 20)
    private String loaiTaiKhoan = "CHINH_THUC"; // CHINH_THUC | TAM_THOI

    @Column(name = "hieu_luc_den")
    private java.time.LocalDateTime hieuLucDen; // null với CHINH_THUC

    @Column(name = "ma_giang_vien", length = 50)
    private String maGiangVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_vi_id")
    private DonVi donVi;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai = "HOAT_DONG"; // HOAT_DONG | DA_KHOA
}
