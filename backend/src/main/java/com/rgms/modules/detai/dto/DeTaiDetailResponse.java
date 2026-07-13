package com.rgms.modules.detai.dto;

import com.rgms.modules.files.dto.TaiLieuResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeTaiDetailResponse {

    private Long id;
    private String maSo;
    private String tenDeTai;
    private String trangThai;
    private String chuNhiem;
    private Long chuNhiemId;
    private LocalDateTime updatedAt;
    private String moTa;
    private String linhVuc;
    private String donVi;
    private String kyNckh;
    private List<TaiLieuResponse> taiLieu;
    private List<AuditLogEntry> auditLog;

    // B5-1: Guard fields cho FE sopDGuards.js
    /** Trạng thái hợp đồng: CHO_PHAN_HOI, CHO_KY, DA_KY, YEU_CAU_SUA, v.v. */
    private String hopDongStatus;
    /** GV đã đồng ý HĐ? (HD trạng thái CHO_KY hoặc DA_KY) */
    private Boolean gvDaDongYHopDong;
    /** Có feedback HOP_DONG đang CHO_XU_LY? */
    private Boolean hopDongFeedback;
    /** Danh sách thành viên tổ phản biện (id, hoTen, ketQua, nhanXet) */
    private List<ToPhanBienMemberResponse> toPhanBien;
}
