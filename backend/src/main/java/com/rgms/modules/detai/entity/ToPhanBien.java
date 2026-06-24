package com.rgms.modules.detai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity ToPhanBien — Tổ phản biện do P.NCKH lập (1-1 với DeTai tại giai đoạn phản biện).
 *
 * Guard PNCKH_LAP_TO_PB: phải có ít nhất 2 ThanhVienToPhanBien trước khi transition.
 * Kết quả tổng hợp (ketQuaTongHop) do P.NCKH xác nhận thủ công sau khi tất cả thành viên nộp.
 *
 * Tham chiếu: er-diagram.md — Entity 7. ToPhanBien
 */
@Entity
@Table(name = "to_phan_bien")
@Getter
@Setter
public class ToPhanBien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_tai_id", nullable = false, unique = true)
    private DeTai deTai;

    @Column(name = "deadline_nop", nullable = false)
    private LocalDate deadlineNop;

    /**
     * Kết quả tổng hợp của toàn tổ.
     * CHUA_CO | CHAP_NHAN | CAN_SUA | TU_CHOI
     */
    @Column(name = "ket_qua_tong_hop", nullable = false, length = 20)
    private String ketQuaTongHop = "CHUA_CO";

    @Column(name = "ghi_chu_pnckh", columnDefinition = "TEXT")
    private String ghiChuPnckh;

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
