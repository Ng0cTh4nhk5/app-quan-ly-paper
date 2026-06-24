package com.rgms.modules.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
public class PhanBienDeXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_tai_id", nullable = false)
    private DeTai deTai;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "co_quan", length = 200)
    private String coQuan;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
