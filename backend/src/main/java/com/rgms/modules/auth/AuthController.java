package com.rgms.modules.auth;

import com.rgms.shared.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập mock để lấy JWT",
            description = "Sinh JWT token phục vụ kiểm thử Phase 1. Endpoint nhận username và role tuỳ chọn, sau đó tự map 4 user seed gồm gv_a, nckh_b, pb_c, pb_d theo đúng đặc tả dự án."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sinh token thành công"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi sinh token")
    })
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String role = request.getRole();
        Long userId = 999L;

        if ("gv_a".equalsIgnoreCase(username)) {
            userId = 1L;
            role = "GIANG_VIEN";
        } else if ("nckh_b".equalsIgnoreCase(username)) {
            userId = 2L;
            role = "NCKH";
        } else if ("pb_c".equalsIgnoreCase(username)) {
            userId = 3L;
            role = "TO_PHAN_BIEN";
        } else if ("pb_d".equalsIgnoreCase(username)) {
            userId = 4L;
            role = "TO_PHAN_BIEN";
        }

        String token = jwtUtil.generateToken(userId, username, role);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String username;
        private String role;
    }
}
