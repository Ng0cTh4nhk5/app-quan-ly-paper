package com.rgms.modules.detai.entity;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity AuditLog — Nhật ký kiểm toán bất biến (Append-Only).
 *
 * Quy tắc BR-15:
 *   - KHÔNG được UPDATE hay DELETE bất kỳ bản ghi nào.
 *   - Ghi mọi transition trạng thái và hành động quan trọng.
 *   - Retention tối thiểu 2 năm.
 *
 * Tham chiếu: er-diagram.md — Entity 18. AuditLog
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(generator = "uuid7")
    @org.hibernate.annotations.GenericGenerator(name = "uuid7", type = com.rgms.shared.util.Uuid7Generator.class)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private java.util.UUID id;

    /**
     * NULL cho thao tác cấp hệ thống (config, admin system action).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_tai_id")
    private DeTai deTai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private NguoiDung actor;

    /**
     * Mã hành động — ví dụ: GV_GUI_HO_SO, PNCKH_TIEP_NHAN, TAO_DE_TAI.
     */
    @Column(name = "hanh_dong", nullable = false, length = 100)
    private String hanhDong;

    @Column(name = "trang_thai_truoc", length = 50)
    private String trangThaiTruoc;

    @Column(name = "trang_thai_sau", length = 50)
    private String trangThaiSau;

    /**
     * Metadata bổ sung (số tiền, lý do, v.v.) — JSON string.
     */
    @Column(name = "meta", columnDefinition = "JSONB")
    private String meta;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @PrePersist
    void prePersist() {
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }
}
