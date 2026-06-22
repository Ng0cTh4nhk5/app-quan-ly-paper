package com.rgms.modules.detai.entity;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity PhanBienDeXuat — Danh sách phản biện GV đề xuất khi soạn hồ sơ (Draft).
 *
 * Đây là gợi ý của GV — P.NCKH có thể bỏ qua khi lập tổ PB chính thức.
 * Guard HoSoHopLeGuard kiểm tra countByDeTai() >= 2 trước khi cho GV gửi hồ sơ.
 *
 * Tham chiếu: gd-1-1-khoi-tao.md — F-GV-03 (Input / Form Danh sách phản biện đề xuất)
 */
@Entity
@Table(name = "phan_bien_de_xuat")
@Getter
@Setter
public class PhanBienDeXuat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_tai_id", nullable = false)
    private DeTai deTai;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "don_vi_chuyen_nganh", length = 200)
    private String donViChuyenNganh;
}
