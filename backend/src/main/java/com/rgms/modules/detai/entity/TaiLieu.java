package com.rgms.modules.detai.entity;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity TaiLieu — File đính kèm đề tài.
 *
 * Phiên bản tự tăng theo cặp (de_tai_id, loai) — giữ lịch sử (BR-14).
 * Loại: THUYET_MINH | BAO_CAO | MINH_CHUNG_NT | MINH_CHUNG_QT |
 *        HOP_DONG_SCAN | BIEN_BAN_NT | BIEN_BAN_THANH_LY | BIEU_MAU_KHAC
 *
 * Tham chiếu: er-diagram.md — Entity 15. TaiLieu
 */
@Entity
@Table(name = "tai_lieu")
@Getter
@Setter
public class TaiLieu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_tai_id", nullable = false)
    private DeTai deTai;

    @Column(name = "loai", nullable = false, length = 30)
    private String loai; // THUYET_MINH | BAO_CAO | HOP_DONG_SCAN | BIEN_BAN_NT | BIEN_BAN_THANH_LY | BIEU_MAU_KHAC

    @Column(name = "ten_file", nullable = false, length = 300)
    private String tenFile;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "dinh_dang", nullable = false, length = 20)
    private String dinhDang; // pdf | docx | xlsx | jpg | png

    @Column(name = "kich_thuoc_byte", nullable = false)
    private Long kichThuocByte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_upload_id", nullable = false)
    private NguoiDung nguoiUpload;

    @Column(name = "ngay_upload", nullable = false)
    private java.time.LocalDateTime ngayUpload;

    @Column(name = "phien_ban", nullable = false)
    private Integer phienBan = 1; // Tự tăng theo (de_tai_id, loai) — BR-14

    @PrePersist
    void prePersist() {
        if (ngayUpload == null) ngayUpload = java.time.LocalDateTime.now();
    }
}
