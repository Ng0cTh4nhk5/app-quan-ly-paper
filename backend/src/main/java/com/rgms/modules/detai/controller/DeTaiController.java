package com.rgms.modules.detai.controller;

import com.rgms.modules.detai.dto.*;
import com.rgms.modules.detai.service.ResearchTopicService;
import com.rgms.shared.dto.ApiResponse;
import com.rgms.shared.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * DeTaiController — REST API cho Luồng 1 (Phase 1).
 *
 * URL prefix: /api/v1/de-tai
 * RBAC được enforce bằng @PreAuthorize (method security).
 *
 * Actor ID lấy từ JWT CustomUserDetails.getId() (Long).
 *
 * Tham chiếu: sop-member-a.md — Giai đoạn 5
 */
@RestController
@RequestMapping("/api/v1/de-tai")
@RequiredArgsConstructor
public class DeTaiController {

    private final ResearchTopicService researchTopicService;

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/de-tai  — Danh sách đề tài (có filter)
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<PageDeTaiResponse>> danhSach(
            @RequestParam(required = false) String trangThai,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails cur) {

        PageDeTaiResponse page = researchTopicService.danhSach(trangThai, pageable, cur);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/de-tai/{id}  — Chi tiết đề tài
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeTaiDetailResponse>> chiTiet(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiDetailResponse detail = researchTopicService.layChiTiet(id, cur);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai  — Giảng viên tạo đề tài mới
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> taoDeTai(
            @Valid @RequestBody TaoDeTaiRequest request,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse deTai = researchTopicService.taoDeTai(request, cur.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(deTai));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/gui-ho-so  — GV gửi hồ sơ đến PNCKH
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/gui-ho-so")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> guiHoSo(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.guiHoSo(id, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/tiep-nhan  — PNCKH tiếp nhận hồ sơ
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/tiep-nhan")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> tiepNhan(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.tiepNhan(id, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/yeu-cau-bo-sung  — PNCKH yêu cầu bổ sung
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/yeu-cau-bo-sung")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> yeuCauBoSung(
            @PathVariable Long id,
            @Valid @RequestBody YeuCauBoSungRequest request,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.yeuCauBoSung(id, request, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/gv-nop-bo-sung  — GV nộp lại hồ sơ bổ sung
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/gv-nop-bo-sung")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> gvNopBoSung(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.gvNopBoSung(id, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/lap-to-phan-bien  — PNCKH lập tổ phản biện
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/lap-to-phan-bien")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> lapToPhanBien(
            @PathVariable Long id,
            @Valid @RequestBody LapToPhanBienRequest request,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.lapToPhanBien(id, request, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/pb-nop-ket-qua  — Phản biện nộp kết quả
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/pb-nop-ket-qua")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> pbNopKetQua(
            @PathVariable Long id,
            @Valid @RequestBody KetQuaPBRequest request,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.pbNopKetQua(id, request, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/xet-duyet-pb  — PNCKH xét duyệt kết quả PB
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/xet-duyet-pb")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> xetDuyetPB(
            @PathVariable Long id,
            @Valid @RequestBody XetDuyetPBRequest request,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.xetDuyetPB(id, request, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/gv-dong-y-hop-dong  — GV đồng ý hợp đồng
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/gv-dong-y-hop-dong")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> gvDongYHopDong(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails cur) {

        // GV đồng ý → dongY = true, ghiChu = null
        PhanHoiHopDongRequest req = new PhanHoiHopDongRequest();
        req.setDongY(Boolean.TRUE);
        DeTaiResponse result = researchTopicService.phanHoiHopDong(id, req, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/ky-hop-dong  — PNCKH ký hợp đồng (upload scan)
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/ky-hop-dong")
    @PreAuthorize("hasRole('PNCKH')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> kyHopDong(
            @PathVariable Long id,
            @RequestParam("fileScan") MultipartFile fileScan,
            @RequestParam("ngayKy") String ngayKy,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.kyHopDong(
                id, fileScan, LocalDate.parse(ngayKy), cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/de-tai/{id}/rut  — GV chủ động rút đề tài
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/rut")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public ResponseEntity<ApiResponse<DeTaiResponse>> rutDeTai(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails cur) {

        DeTaiResponse result = researchTopicService.gvRutDeTai(id, cur.getId());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
