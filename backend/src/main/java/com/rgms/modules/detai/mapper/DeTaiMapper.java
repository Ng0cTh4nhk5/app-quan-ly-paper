package com.rgms.modules.detai.mapper;

import com.rgms.modules.detai.dto.AuditLogEntry;
import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.files.dto.TaiLieuResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DeTaiMapper {

    public DeTaiResponse toResponse(DeTai deTai) {
        return DeTaiResponse.builder()
                .id(deTai.getId())
                .maSo(deTai.getMaSo())
                .tenDeTai(deTai.getTenDeTai())
                .trangThai(deTai.getTrangThai())
                .chuNhiem(deTai.getChuNhiem() != null ? deTai.getChuNhiem().getHoTen() : null)
                .updatedAt(deTai.getUpdatedAt())
                .build();
    }

    public DeTaiDetailResponse toDetailResponse(DeTai deTai,
                                                List<TaiLieuResponse> taiLieu,
                                                List<AuditLogEntry> auditLog) {
        return DeTaiDetailResponse.builder()
                .id(deTai.getId())
                .maSo(deTai.getMaSo())
                .tenDeTai(deTai.getTenDeTai())
                .trangThai(deTai.getTrangThai())
                .chuNhiem(deTai.getChuNhiem() != null ? deTai.getChuNhiem().getHoTen() : null)
                .updatedAt(deTai.getUpdatedAt())
                .moTa(deTai.getMoTa())
                .linhVuc(deTai.getLinhVuc())
                .donVi(deTai.getDonVi() != null ? deTai.getDonVi().getTenDonVi() : null)
                .kyNckh(deTai.getKyNckh() != null ? deTai.getKyNckh().getTenKy() : null)
                .taiLieu(taiLieu)
                .auditLog(auditLog)
                .build();
    }

    public AuditLogEntry toAuditLogEntry(AuditLog auditLog, Map<Long, String> actorNames) {
        return AuditLogEntry.builder()
                .action(auditLog.getHanhDong())
                .actor(actorNames.getOrDefault(auditLog.getActorId(), null))
                .tuTrangThai(auditLog.getTuTrangThai())
                .sangTrangThai(auditLog.getSangTrangThai())
                .severity(auditLog.getSeverity())
                .ghiChu(auditLog.getGhiChu())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
