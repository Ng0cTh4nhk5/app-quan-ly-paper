package com.rgms.modules.detai.service;

import com.rgms.exception.BusinessException;
import com.rgms.exception.ResourceNotFoundException;
import com.rgms.modules.detai.dto.AuditLogEntry;
import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.dto.PageDeTaiResponse;
import com.rgms.modules.detai.dto.TaoDeTaiRequest;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.KyNCKH;
import com.rgms.modules.detai.entity.NguoiDung;
import com.rgms.modules.detai.mapper.DeTaiMapper;
import com.rgms.modules.detai.repo.AuditLogRepository;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.repo.KyNCKHRepository;
import com.rgms.modules.detai.repo.NguoiDungRepository;
import com.rgms.modules.files.dto.TaiLieuResponse;
import com.rgms.modules.files.mapper.TaiLieuMapper;
import com.rgms.modules.files.repo.TaiLieuRepository;
import com.rgms.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResearchTopicService {

    private static final String ROLE_GIANG_VIEN = "GIANG_VIEN";
    private static final String TRANG_THAI_DANG_MO = "DANG_MO";
    private static final String TRANG_THAI_DRAFT = "DRAFT";

    private final DeTaiRepository deTaiRepository;
    private final KyNCKHRepository kyNCKHRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final TaiLieuRepository taiLieuRepository;
    private final AuditLogRepository auditLogRepository;
    private final DeTaiMapper deTaiMapper;
    private final TaiLieuMapper taiLieuMapper;

    @Transactional
    public DeTaiResponse taoDeTai(TaoDeTaiRequest request, Long gvId) {
        NguoiDung chuNhiem = nguoiDungRepository.findById(gvId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", gvId));
        if (!ROLE_GIANG_VIEN.equals(chuNhiem.getVaiTro())) {
            throw new AccessDeniedException("Chỉ giảng viên được tạo đề tài.");
        }

        KyNCKH kyNCKH = kyNCKHRepository.findById(request.getKyNckhId())
                .orElseThrow(() -> new BusinessException("Kỳ NCKH không tồn tại.", HttpStatus.BAD_REQUEST));
        if (!TRANG_THAI_DANG_MO.equals(kyNCKH.getTrangThai())) {
            throw new BusinessException("Kỳ NCKH đã đóng, không thể tạo đề tài.", HttpStatus.BAD_REQUEST);
        }

        DeTai deTai = DeTai.builder()
                .maSo(generateMaSo())
                .tenDeTai(request.getTenDeTai().trim())
                .moTa(request.getMoTa())
                .linhVuc(request.getLinhVuc())
                .trangThai(TRANG_THAI_DRAFT)
                .chuNhiem(chuNhiem)
                .kyNckh(kyNCKH)
                .donVi(chuNhiem.getDonVi())
                .build();
        DeTai saved = deTaiRepository.save(deTai);

        auditLogRepository.save(AuditLog.builder()
                .deTaiId(saved.getId())
                .hanhDong("TAO_DE_TAI")
                .actorId(gvId)
                .tuTrangThai(null)
                .sangTrangThai(TRANG_THAI_DRAFT)
                .severity("INFO")
                .ghiChu("Khởi tạo đề tài nháp")
                .build());

        return deTaiMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageDeTaiResponse danhSach(String trangThai, Pageable pageable, CustomUserDetails currentUser) {
        String normalizedTrangThai = StringUtils.hasText(trangThai) ? trangThai.trim() : null;
        Page<DeTai> pageData;

        if (ROLE_GIANG_VIEN.equals(currentUser.getRole())) {
            pageData = normalizedTrangThai == null
                    ? deTaiRepository.findByChuNhiem_Id(currentUser.getId(), pageable)
                    : deTaiRepository.findByChuNhiem_IdAndTrangThai(currentUser.getId(), normalizedTrangThai, pageable);
        } else {
            pageData = normalizedTrangThai == null
                    ? deTaiRepository.findAll(pageable)
                    : deTaiRepository.findByTrangThai(normalizedTrangThai, pageable);
        }

        return PageDeTaiResponse.builder()
                .content(pageData.getContent().stream().map(deTaiMapper::toResponse).toList())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .page(pageData.getNumber())
                .build();
    }

    @Transactional(readOnly = true)
    public DeTaiDetailResponse layChiTiet(Long id, CustomUserDetails currentUser) {
        DeTai deTai = deTaiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", id));

        if (ROLE_GIANG_VIEN.equals(currentUser.getRole())
                && (deTai.getChuNhiem() == null || !deTai.getChuNhiem().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Bạn không có quyền xem đề tài này.");
        }

        var taiLieu = taiLieuRepository.findByDeTaiIdOrderByUploadedAtDesc(id)
                .stream()
                .map(taiLieuMapper::toResponse)
                .toList();

        var auditLogs = auditLogRepository.findByDeTaiIdOrderByCreatedAtAsc(id);
        Map<Long, String> actorNames = loadActorNames(auditLogs);
        List<AuditLogEntry> auditLogEntries = auditLogs.stream()
                .map(auditLog -> deTaiMapper.toAuditLogEntry(auditLog, actorNames))
                .toList();

        return deTaiMapper.toDetailResponse(deTai, taiLieu, auditLogEntries);
    }

    private String generateMaSo() {
        int year = Year.now().getValue();
        String prefix = "NCKH-" + year + "-";
        long nextNumber = deTaiRepository.nextMaSoSequence();
        return prefix + String.format("%04d", nextNumber);
    }

    private Map<Long, String> loadActorNames(List<AuditLog> auditLogs) {
        Set<Long> actorIds = auditLogs.stream()
                .map(AuditLog::getActorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (actorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return nguoiDungRepository.findByIdIn(actorIds)
                .stream()
                .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen));
    }
}
