package com.rgms.modules.detai.entity;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity ToPhanBien — Tổ phản biện được P.NCKH lập (1-1 với DeTai tại giai đoạn phản biện).
 *
 * Guard PNCKH_LAP_TO_PB: phải có ít nhất 2 ThanhVienToPhanBien trước khi transition.
 * Kết quả tổng hợp (ketQuaTongHop) do P.NCKH xác định thủ công sau khi tất cả thành viên nộp.
 *
 * Tham chiếu: er-diagram.md — Entity 7. ToPhanBien
 */
@Entity
@Table(name = "to_phan_bien")
@Getter
@Setter
public class ToPhanBien extends BaseEntity {

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
}
