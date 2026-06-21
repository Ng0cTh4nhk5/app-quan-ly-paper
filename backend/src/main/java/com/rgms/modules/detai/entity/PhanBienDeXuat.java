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

@Entity
@Table(name = "phan_bien_de_xuat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanBienDeXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "de_tai_id", nullable = false)
    private Long deTaiId;

    @Column(name = "ho_ten", nullable = false)
    private String hoTen;

    private String email;

    @Column(name = "co_quan")
    private String coQuan;
}
