package com.rgms.modules.detai.entity;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity ThanhVienToPhanBien — Thành viên trong tổ phản biện.
 *
 * Kết quả cá nhân của từng thành viên (ketQua) đóng góp vào ketQuaTongHop của ToPhanBien.
 * Tham chiếu: er-diagram.md — Entity 8. ThanhVienToPhanBien
 */
@Entity
@Table(name = "thanh_vien_to_phan_bien",
        uniqueConstraints = @UniqueConstraint(columnNames = {"to_phan_bien_id", "nguoi_dung_id"}))
@Getter
@Setter
public class ThanhVienToPhanBien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_phan_bien_id", nullable = false)
    private ToPhanBien toPhanBien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dung_id", nullable = false)
    private NguoiDung nguoiDung;

    /**
     * Kết quả phản biện cá nhân.
     * CHUA_NOP | CHAP_NHAN | CAN_SUA | TU_CHOI
     */
    @Column(name = "ket_qua", nullable = false, length = 20)
    private String ketQua = "CHUA_NOP";

    @Column(name = "nhan_xet", columnDefinition = "TEXT")
    private String nhanXet;

    @Column(name = "ngay_nop")
    private LocalDateTime ngayNop;

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

    public boolean isDaNop() {
        return !"CHUA_NOP".equals(ketQua);
    }
}
