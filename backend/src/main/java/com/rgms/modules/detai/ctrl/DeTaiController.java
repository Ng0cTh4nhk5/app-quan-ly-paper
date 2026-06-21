package com.rgms.modules.detai.ctrl;

import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.dto.PageDeTaiResponse;
import com.rgms.modules.detai.dto.TaoDeTaiRequest;
import com.rgms.modules.detai.service.ResearchTopicService;
import com.rgms.shared.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/de-tai")
@Tag(name = "DeTai", description = "API quản lý đề tài nghiên cứu khoa học giai đoạn khởi tạo")
@SecurityRequirement(name = "bearerAuth")
public class DeTaiController {

    private final ResearchTopicService topicService;

    @PostMapping
    @PreAuthorize("hasRole('GIANG_VIEN')")
    @Operation(
            summary = "GV tạo đề tài mới",
            description = "Tạo đề tài nghiên cứu ở trạng thái DRAFT. API chỉ dành cho giảng viên, kiểm tra kỳ NCKH phải đang mở và tự sinh mã số dạng NCKH-[Năm]-000X."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đề tài nháp thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc kỳ NCKH đã đóng"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò GIANG_VIEN"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<DeTaiResponse> taoDeTai(
            @Valid @RequestBody TaoDeTaiRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        DeTaiResponse response = topicService.taoDeTai(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Danh sách đề tài",
            description = "Trả về danh sách đề tài có phân trang và lọc theo trạng thái. Giảng viên chỉ thấy đề tài do chính mình làm chủ nhiệm; NCKH và các vai trò khác đã đăng nhập thấy toàn bộ đề tài."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = PageDeTaiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Tham số phân trang không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<PageDeTaiResponse> danhSach(
            @Parameter(description = "Trạng thái đề tài cần lọc, ví dụ DRAFT hoặc CHO_PNCKH_XEM_XET")
            @RequestParam(required = false) String trangThai,
            @Parameter(description = "Số trang, bắt đầu từ 0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @Parameter(description = "Kích thước trang, tối đa 50")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size phải >= 1") @Max(value = 50, message = "size tối đa là 50") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.danhSach(trangThai, PageRequest.of(page, size), currentUser));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Chi tiết đề tài",
            description = "Trả về thông tin chi tiết của một đề tài, bao gồm danh sách tài liệu đính kèm và lịch sử audit. Nếu người gọi là giảng viên, hệ thống chỉ cho phép xem đề tài do chính giảng viên đó làm chủ nhiệm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = DeTaiDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Giảng viên không phải chủ nhiệm đề tài"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<DeTaiDetailResponse> layChiTiet(
            @Parameter(description = "ID đề tài cần xem", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(topicService.layChiTiet(id, currentUser));
    }
}
