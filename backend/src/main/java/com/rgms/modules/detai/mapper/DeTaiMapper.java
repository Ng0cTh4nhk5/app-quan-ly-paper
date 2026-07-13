package com.rgms.modules.detai.mapper;

import com.rgms.modules.detai.dto.AuditLogEntry;
import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.dto.ToPhanBienMemberResponse;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.HopDong;
import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import com.rgms.modules.files.dto.TaiLieuResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DeTaiMapper {

    private static final Set<String> HD_GV_DA_DONG_Y = Set.of("CHO_KY", "DA_KY");

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
                                                List<AuditLogEntry> auditLog,
                                                HopDong hopDong,
                                                List<ThanhVienToPhanBien> tvList,
                                                boolean hasOpenHdFeedback) {
        String hdStatus = hopDong != null ? hopDong.getTrangThaiHopDong() : null;
        Boolean gvDaDongY = hdStatus != null ? HD_GV_DA_DONG_Y.contains(hdStatus) : null;

        List<ToPhanBienMemberResponse> pbMembers = tvList != null
                ? tvList.stream().map(tv -> ToPhanBienMemberResponse.builder()
                    .id(tv.getNguoiDung().getId())
                    .hoTen(tv.getNguoiDung().getHoTen())
                    .ketQua(tv.getKetQua())
                    .nhanXet(tv.getNhanXet())
                    .build()).toList()
                : null;

        return DeTaiDetailResponse.builder()
                .id(deTai.getId())
                .maSo(deTai.getMaSo())
                .tenDeTai(deTai.getTenDeTai())
                .trangThai(deTai.getStatus() != null ? deTai.getStatus().name() : null)
                .chuNhiem(deTai.getChuNhiem() != null ? deTai.getChuNhiem().getHoTen() : null)
                .chuNhiemId(deTai.getChuNhiem() != null ? deTai.getChuNhiem().getId() : null)
                .updatedAt(deTai.getUpdatedAt())
                .moTa(deTai.getMoTa())
                .linhVuc(deTai.getLinhVuc())
                .donVi(deTai.getDonVi() != null ? deTai.getDonVi().getTen() : null)
                .kyNckh(deTai.getKyNckh() != null ? deTai.getKyNckh().getTenKy() : null)
                .taiLieu(taiLieu)
                .auditLog(auditLog)
                // B5-1: guard fields
                .hopDongStatus(hdStatus)
                .gvDaDongYHopDong(gvDaDongY)
                .hopDongFeedback(hasOpenHdFeedback)
                .toPhanBien(pbMembers)
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

