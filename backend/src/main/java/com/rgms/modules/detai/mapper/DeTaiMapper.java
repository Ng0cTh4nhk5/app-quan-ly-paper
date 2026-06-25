package com.rgms.modules.detai.mapper;

import com.rgms.modules.detai.dto.AuditLogEntry;
import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.files.dto.TaiLieuResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeTaiMapper {

    public DeTaiResponse toResponse(DeTai deTai) {
        return DeTaiResponse.builder()
                .id(deTai.getId())
                .maSo(deTai.getMaSo())
                .tenDeTai(deTai.getTenDeTai())
                .trangThai(deTai.getStatus() != null ? deTai.getStatus().name() : null)
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
                .trangThai(deTai.getStatus() != null ? deTai.getStatus().name() : null)
                .chuNhiem(deTai.getChuNhiem() != null ? deTai.getChuNhiem().getHoTen() : null)
                .updatedAt(deTai.getUpdatedAt())
                .moTa(deTai.getMoTa())
                .linhVuc(deTai.getLinhVuc())
                // DonVi.ten (không phải tenDonVi)
                .donVi(deTai.getDonVi() != null ? deTai.getDonVi().getTen() : null)
                .kyNckh(deTai.getKyNckh() != null ? deTai.getKyNckh().getTenKy() : null)
                .taiLieu(taiLieu)
                .auditLog(auditLog)
                .build();
    }

    /**
     * Map AuditLog → AuditLogEntry DTO.
     * actor là NguoiDung reference, actorId lấy từ actor.getId().
     * Dùng trangThaiTruoc/trangThaiSau (field name của entity).
     */
    public AuditLogEntry toAuditLogEntry(AuditLog log) {
        String actorName = log.getActor() != null ? log.getActor().getHoTen() : "(system)";
        return AuditLogEntry.builder()
                .action(log.getHanhDong())
                .actor(actorName)
                .tuTrangThai(log.getTrangThaiTruoc())
                .sangTrangThai(log.getTrangThaiSau())
                .ghiChu(log.getMeta())
                .createdAt(log.getThoiGian())
                .build();
    }
}
