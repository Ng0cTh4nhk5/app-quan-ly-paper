package com.rgms.modules.detai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ky_nckh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KyNCKH {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_ky", nullable = false)
    private String tenKy;

    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
