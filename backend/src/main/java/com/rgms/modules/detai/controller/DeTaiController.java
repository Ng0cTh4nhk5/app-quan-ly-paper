package com.rgms.modules.detai.controller;

import com.rgms.modules.detai.dto.*;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import com.rgms.modules.detai.service.ResearchTopicService;
import com.rgms.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * DeTaiController — REST API cho Luồng 1 (Phase 1).
 *
 * URL prefix: /api/v1/de-tai
 * RBAC được enforce bằng @PreAuthorize (method security).
 *
 * Actor ID lấy từ JWT principal (Authentication.getName() trả về userId string).
 * Đây là convention chung của dự án (Member F — Auth Service sẽ set principal là UUID string).
 *
 * Tham chiếu: sop-member-a.md — Giai đoạn 5
 */
@RestController
@RequestMapping("/api/v1/de-tai")
@RequiredArgsConstructor
public class DeTaiController {

    private final ResearchTopicService researchTopicService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai  — Giảng viên tạo đề tài mới
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTai>> taoDeTai(
            @Valid @RequestBody TaoDeTaiRequest request,
            Authentication auth) {

        UUID gvId = UUID.fromString(auth.getName());
        DeTai deTai = researchTopicService.taoDeTai(
                gvId,
                request.getTenDeTai(),
                request.getMoTa(),
                request.getLinhVuc(),
                request.getKyNckhId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(deTai));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/gui-ho-so  — GV gửi hồ sơ đến PNCKH
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/gui-ho-so")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTai>> guiHoSo(
            @PathVariable UUID id,
            Authentication auth) {

        UUID gvId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.guiHoSo(id, gvId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/tiep-nhan  — PNCKH tiếp nhận hồ sơ
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/tiep-nhan")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTai>> tiepNhan(
            @PathVariable UUID id,
            Authentication auth) {

        UUID nckhId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.tiepNhan(id, nckhId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/yeu-cau-bo-sung  — PNCKH yêu cầu bổ sung
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/yeu-cau-bo-sung")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTai>> yeuCauBoSung(
            @PathVariable UUID id,
            @RequestBody YeuCauBoSungRequest request,
            Authentication auth) {

        UUID nckhId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.yeuCauBoSung(
                id, nckhId, request.getNoiDungFeedback(), request.getDeadline());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/gv-nop-bo-sung  — GV nộp lại hồ sơ bổ sung
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/gv-nop-bo-sung")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTai>> gvNopBoSung(
            @PathVariable UUID id,
            Authentication auth) {

        UUID gvId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.gvNopBoSung(id, gvId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/lap-to-phan-bien  — PNCKH lập tổ phản biện
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/lap-to-phan-bien")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTai>> lapToPhanBien(
            @PathVariable UUID id,
            @RequestBody LapToPhanBienRequest request,
            Authentication auth) {

        UUID nckhId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.lapToPhanBien(
                id, nckhId, request.getDeadlineNop(), request.getDanhSachPhanBienId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/pb-nop-ket-qua  — Phản biện nộp kết quả
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/pb-nop-ket-qua")
    @PreAuthorize("hasAnyRole('PNCKH', 'GIANG_VIEN')")
    public ResponseEntity<ApiResponse<ThanhVienToPhanBien>> pbNopKetQua(
            @PathVariable UUID id,
            @RequestBody PbNopKetQuaRequest request,
            Authentication auth) {

        UUID pbId = UUID.fromString(auth.getName());
        ThanhVienToPhanBien result = researchTopicService.pbNopKetQua(
                id, pbId, request.getKetQua(), request.getNhanXet());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/xet-duyet-pb  — PNCKH xét duyệt kết quả PB
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/xet-duyet-pb")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTai>> xetDuyetPB(
            @PathVariable UUID id,
            @RequestBody XetDuyetPBRequest request,
            Authentication auth) {

        UUID nckhId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.nckhXetDuyetPB(
                id, nckhId, request.getQuyetDinh(), request.getGhiChu());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/gv-dong-y-hop-dong  — GV đồng ý hợp đồng
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/gv-dong-y-hop-dong")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTai>> gvDongYHopDong(
            @PathVariable UUID id,
            Authentication auth) {

        UUID gvId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.gvDongYHopDong(id, gvId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/ky-hop-dong  — PNCKH ký hợp đồng chính thức
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/ky-hop-dong")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTai>> kyHopDong(
            @PathVariable UUID id,
            Authentication auth) {

        UUID nckhId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.kyHopDong(id, nckhId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/rut  — GV chủ động rút đề tài
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/rut")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTai>> rutDeTai(
            @PathVariable UUID id,
            Authentication auth) {

        UUID gvId = UUID.fromString(auth.getName());
        DeTai result = researchTopicService.gvRutDeTai(id, gvId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
