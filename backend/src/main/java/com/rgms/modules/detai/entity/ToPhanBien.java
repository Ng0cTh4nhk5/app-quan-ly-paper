package com.rgms.modules.detai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "to_phan_bien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToPhanBien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "de_tai_id", nullable = false, unique = true)
    private Long deTaiId;

    @Column(name = "deadline_nop", nullable = false)
    private LocalDateTime deadlineNop;

    @Builder.Default
    @Column(name = "ket_qua_tong_hop", nullable = false, length = 50)
    private String ketQuaTongHop = "CHUA_CO";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
