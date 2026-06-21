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
    private LocalDateTime updatedAt;
    private String moTa;
    private String linhVuc;
    private List<TaiLieuResponse> taiLieu;
    private List<AuditLogEntry> auditLog;
}
