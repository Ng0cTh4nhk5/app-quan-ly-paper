package com.rgms.modules.auth;

import com.rgms.exception.BusinessException;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.detai.repo.NguoiDungRepository;
import com.rgms.shared.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Mock authentication endpoints for local Phase 1 testing")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final NguoiDungRepository nguoiDungRepository;

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập mock để lấy JWT",
            description = "Sinh JWT token phục vụ kiểm thử Phase 1. Endpoint nhận username của user seed trong database và trả token chứa userId, username, role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sinh token thành công"),
            @ApiResponse(responseCode = "401", description = "Username không tồn tại trong dữ liệu seed"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi sinh token")
    })
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())) {
            throw new BusinessException("Username không được để trống.", HttpStatus.BAD_REQUEST);
        }

        NguoiDung user = nguoiDungRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BusinessException(
                        "User không tồn tại trong dữ liệu seed.",
                        HttpStatus.UNAUTHORIZED));

        Long donViId = user.getDonVi() != null ? user.getDonVi().getId() : null;
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getVaiTro(), donViId);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String username;
        private String role;
    }
}
