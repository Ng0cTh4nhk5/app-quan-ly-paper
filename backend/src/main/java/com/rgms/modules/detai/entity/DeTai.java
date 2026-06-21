package com.rgms.modules.detai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "de_tai")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeTai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_so", nullable = false, unique = true, length = 50)
    private String maSo;

    @Column(name = "ten_de_tai", nullable = false, length = 500)
    private String tenDeTai;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "linh_vuc")
    private String linhVuc;

    @Builder.Default
    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chu_nhiem_id", nullable = false)
    private NguoiDung chuNhiem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ky_nckh_id", nullable = false)
    private KyNCKH kyNckh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_vi_id", nullable = false)
    private DonVi donVi;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
