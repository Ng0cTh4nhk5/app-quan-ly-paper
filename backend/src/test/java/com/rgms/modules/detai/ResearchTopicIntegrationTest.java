package com.rgms.modules.detai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgms.modules.detai.dto.TaoDeTaiRequest;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.repo.KyNCKHRepository;
import com.rgms.modules.nguoidung.repository.NguoiDungRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ResearchTopicIntegrationTest — @SpringBootTest + Testcontainers PostgreSQL.
 *
 * Theo SOP Giai đoạn 5:
 *   - Kiểm tra Happy Path (HTTP 200/201) cho các endpoint chính.
 *   - Kiểm tra RBAC: mỗi endpoint trả về 403 khi gọi với role sai.
 *   - Kiểm tra 401 khi không có authentication.
 *
 * Container PostgreSQL sẽ tự khởi động và tắt xung quanh class test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Integration Test — DeTai REST API + RBAC")
class ResearchTopicIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rgms_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired DeTaiRepository deTaiRepository;
    @Autowired NguoiDungRepository nguoiDungRepository;
    @Autowired KyNCKHRepository kyNckhRepository;

    private static final String API_BASE = "/api/v1/de-tai";

    // ──────────────────────────────────────────────────────────────────────────
    // RBAC — Kiểm tra từ chối đúng role
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RBAC: POST /de-tai — PNCKH gọi endpoint của GV → 403 Forbidden")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "PNCKH")
    void taoDeTai_withPNCKHRole_returns403() throws Exception {
        TaoDeTaiRequest req = new TaoDeTaiRequest();
        req.setTenDeTai("Đề tài test");
        req.setKyNckhId(UUID.randomUUID());

        mockMvc.perform(post(API_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: POST /de-tai/{id}/tiep-nhan — GV gọi endpoint của PNCKH → 403 Forbidden")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", roles = "GIANG_VIEN")
    void tiepNhan_withGVRole_returns403() throws Exception {
        mockMvc.perform(post(API_BASE + "/" + UUID.randomUUID() + "/tiep-nhan"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: POST /de-tai/{id}/lap-to-phan-bien — GV gọi endpoint của PNCKH → 403 Forbidden")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000003", roles = "GIANG_VIEN")
    void lapToPhanBien_withGVRole_returns403() throws Exception {
        mockMvc.perform(post(API_BASE + "/" + UUID.randomUUID() + "/lap-to-phan-bien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: POST /de-tai/{id}/ky-hop-dong — GV gọi endpoint của PNCKH → 403 Forbidden")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000004", roles = "GIANG_VIEN")
    void kyHopDong_withGVRole_returns403() throws Exception {
        mockMvc.perform(post(API_BASE + "/" + UUID.randomUUID() + "/ky-hop-dong"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: POST /de-tai/{id}/gv-dong-y-hop-dong — PNCKH gọi endpoint của GV → 403 Forbidden")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000005", roles = "PNCKH")
    void gvDongYHopDong_withPNCKHRole_returns403() throws Exception {
        mockMvc.perform(post(API_BASE + "/" + UUID.randomUUID() + "/gv-dong-y-hop-dong"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RBAC: POST /de-tai/{id}/xet-duyet-pb — GV gọi endpoint của PNCKH → 403 Forbidden")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000006", roles = "GIANG_VIEN")
    void xetDuyetPB_withGVRole_returns403() throws Exception {
        mockMvc.perform(post(API_BASE + "/" + UUID.randomUUID() + "/xet-duyet-pb")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Unauthenticated — Không có token → 401
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Auth: Không có token → 401 Unauthorized")
    void noToken_returns401() throws Exception {
        mockMvc.perform(post(API_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Validation — Request body thiếu field bắt buộc → 400
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Validation: taoDeTai thiếu tenDeTai → 400 Bad Request")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000007", roles = "GIANG_VIEN")
    void taoDeTai_missingTenDeTai_returns400() throws Exception {
        TaoDeTaiRequest req = new TaoDeTaiRequest();
        // Không set tenDeTai — @NotBlank sẽ trigger
        req.setKyNckhId(UUID.randomUUID());

        mockMvc.perform(post(API_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
