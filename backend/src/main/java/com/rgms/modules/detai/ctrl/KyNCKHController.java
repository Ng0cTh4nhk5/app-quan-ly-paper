package com.rgms.modules.detai.ctrl;

import com.rgms.modules.detai.dto.KyNCKHResponse;
import com.rgms.modules.detai.entity.KyNCKH;
import com.rgms.modules.detai.repo.KyNCKHRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller cho kỳ NCKH.
 * FE TaoDetai.vue gọi GET /api/v1/ky-nckh để populate dropdown kỳ NCKH.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ky-nckh")
@Tag(name = "KyNCKH", description = "API quản lý kỳ nghiên cứu khoa học")
@SecurityRequirement(name = "bearerAuth")
public class KyNCKHController {

    private final KyNCKHRepository kyNckhRepository;

    @GetMapping
    @Operation(
            summary = "Danh sách kỳ NCKH",
            description = "Trả về tất cả kỳ NCKH. FE sẽ filter client-side nếu cần chỉ hiện kỳ đang mở."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ")
    })
    public ResponseEntity<List<KyNCKHResponse>> danhSach() {
        List<KyNCKHResponse> result = kyNckhRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    private KyNCKHResponse toResponse(KyNCKH entity) {
        return KyNCKHResponse.builder()
                .id(entity.getId())
                .ten(entity.getTenKy())
                .trangThai(entity.getTrangThai())
                .build();
    }
}
