package com.rgms.modules.detai.entity;

import com.rgms.modules.nguoidung.entity.DonVi;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.enums.TopicState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity DeTai — Đề tài NCKH (trung tâm của toàn bộ hệ thống).
 *
 * Quy tắc bắt buộc (BR-15):
 *   - Trường `status` KHÔNG được set trực tiếp từ ngoài FsmService.
 *   - Mọi transition phải gọi FsmService.transition() để đảm bảo AuditLog được ghi.
 *
 * Tham chiếu: er-diagram.md — Entity 1. DeTai
 */
@Entity
@Table(name = "de_tai")
@Getter
@Setter
public class DeTai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    // ── Thông tin cơ bản ──────────────────────────────────────────────────────

    @Column(name = "ma_so", unique = true, length = 50)
    private String maSo; // Auto-generated: NCKH-{YEAR}-{SEQ} — bất biến sau khi tạo

    @Column(name = "ten_de_tai", nullable = false, length = 500)
    private String tenDeTai;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "linh_vuc", length = 100)
    private String linhVuc;

    // ── FSM State ─────────────────────────────────────────────────────────────

    /**
     * Trạng thái hiện tại trong FSM.
     * ONLY FsmService được phép gọi setStatus() — mọi code khác phải check và throw.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 50)
    private TopicState status = TopicState.DRAFT;

    // ── Thời gian thực hiện ───────────────────────────────────────────────────

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau; // Set khi ký hợp đồng

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc; // Deadline — cập nhật khi gia hạn

    // ── Tài chính ─────────────────────────────────────────────────────────────

    @Column(name = "kinh_phi", precision = 15, scale = 2)
    private BigDecimal kinhPhi; // Tổng kinh phí theo hợp đồng

    @Column(name = "tong_tam_ung_da_giai_ngan", nullable = false, precision = 15, scale = 2)
    private BigDecimal tongTamUngDaGiaiNgan = BigDecimal.ZERO; // Denormalized (BR-02)

    // ── Quan hệ ───────────────────────────────────────────────────────────────

    /**
     * Chủ nhiệm đề tài — người tạo ra đề tài, luôn là GIANG_VIEN.
     * Dùng LAZY, dùng @EntityGraph khi cần load.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chu_nhiem_id", nullable = false)
    private NguoiDung chuNhiem;

    /**
     * Đơn vị của chủ nhiệm — lấy từ user.donVi khi tạo đề tài.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_vi_id")
    private DonVi donVi;

    /**
     * Kỳ NCKH đăng ký — phải đang DANG_MO khi tạo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ky_nckh_id", nullable = false)
    private KyNCKH kyNckh;

    /**
     * Nguồn khi vào Cho_Hoan_Tra_Tam_Ung.
     * HUY | RUT | KHONG_NGHIEM_THU | DU_TAM_UNG — NULL nếu không ở state đó.
     */
    @Column(name = "nguon_cho_hoan_tra", length = 30)
    private String nguonChoHoanTra;

    // ── GV đồng ý HĐ flag ────────────────────────────────────────────────────

    /**
     * Flag: GV đã đồng ý nội dung hợp đồng.
     * Cần = true trước khi PNCKH được gọi kyHopDong().
     * Không thay đổi state — chỉ là điều kiện guard.
     */
    @Column(name = "gv_da_dong_y_hop_dong")
    private Boolean gvDaDongYHopDong = false;

    // ── Timestamps ────────────────────────────────────────────────────────────

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
