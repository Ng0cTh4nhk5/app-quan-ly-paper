package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.fsm.guards.HoSoHopLeGuard;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.repo.PhanBienDeXuatRepository;
import com.rgms.modules.files.repo.TaiLieuRepository;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.enums.TopicState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * HoSoHopLeGuardTest — Unit test cho HoSoHopLeGuard.
 *
 * Mỗi guard condition phải có 1 test positive (pass) + 1 test negative (fail).
 * Tham chiếu: sop-member-a.md DoD — "Mỗi guard có ít nhất 1 unit test positive + 1 test negative"
 *
 * Đã cập nhật: dùng Long ID thay UUID (theo BaseEntity @Id Long).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HoSoHopLeGuard — Guard kiểm tra hồ sơ hợp lệ trước khi GV gửi")
class HoSoHopLeGuardTest {

    @Mock DeTaiRepository deTaiRepository;
    @Mock TaiLieuRepository taiLieuRepository;
    @Mock PhanBienDeXuatRepository phanBienDeXuatRepository;

    @InjectMocks HoSoHopLeGuard guard;

    // Dùng Long thay UUID — khớp với BaseEntity @Id Long
    private static final Long DE_TAI_ID = 1L;
    private static final Long ACTOR_ID  = 2L;

    private DeTai mockDeTai;
    private NguoiDung mockGv;

    @BeforeEach
    void setUp() {
        mockGv = new NguoiDung();
        // BaseEntity có @Setter từ Lombok — set trực tiếp không cần reflection
        mockGv.setId(ACTOR_ID);

        mockDeTai = new DeTai();
        mockDeTai.setStatus(TopicState.DRAFT);
        mockDeTai.setChuNhiem(mockGv);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GUARD [1]: State phải là DRAFT
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Guard [1]: State phải là DRAFT")
    class StateGuardTests {

        @Test
        @DisplayName("POSITIVE: state = DRAFT → pass")
        void state_draft_pass() {
            mockDeTai.setStatus(TopicState.DRAFT);
            givenValidSetup();

            assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
        }

        @Test
        @DisplayName("NEGATIVE: state = CHO_PNCKH_XEM_XET → ném GUARD_WRONG_STATUS")
        void state_khongPhai_draft_fail() {
            mockDeTai.setStatus(TopicState.CHO_PNCKH_XEM_XET);
            when(deTaiRepository.findById(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> guard.check(DE_TAI_ID, ACTOR_ID));

            assertThat(ex.getErrorCode()).isEqualTo("GUARD_WRONG_STATUS");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GUARD [2]: IDOR — actor phải là chủ nhiệm
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Guard [2]: IDOR — actor phải là chủ nhiệm")
    class IdorGuardTests {

        @Test
        @DisplayName("POSITIVE: actor == chuNhiem → pass")
        void actor_laChuNhiem_pass() {
            givenValidSetup();

            assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
        }

        @Test
        @DisplayName("NEGATIVE: actor != chuNhiem → ném GUARD_IDOR")
        void actor_khongPhai_chuNhiem_fail() {
            Long khacId = 99L; // actor khác với chủ nhiệm
            when(deTaiRepository.findById(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> guard.check(DE_TAI_ID, khacId));

            assertThat(ex.getErrorCode()).isEqualTo("GUARD_IDOR");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GUARD [3]: Phải có file thuyết minh
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Guard [3]: Phải có file thuyết minh")
    class ThuyetMinhGuardTests {

        @Test
        @DisplayName("POSITIVE: có THUYET_MINH → pass")
        void coThuyetMinh_pass() {
            givenValidSetup();

            assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
        }

        @Test
        @DisplayName("NEGATIVE: không có THUYET_MINH → ném GUARD_THIEU_THUYET_MINH")
        void khongCoThuyetMinh_fail() {
            when(deTaiRepository.findById(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));
            when(taiLieuRepository.existsByDeTaiIdAndLoaiFile(DE_TAI_ID, "THUYET_MINH"))
                    .thenReturn(false); // Không có thuyết minh

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> guard.check(DE_TAI_ID, ACTOR_ID));

            assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_THUYET_MINH");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GUARD [4]: Phải có >= 1 người phản biện đề xuất (MIN_PB_DE_XUAT = 1)
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Guard [4]: >= 1 phản biện đề xuất")
    class PhanBienDeXuatGuardTests {

        @Test
        @DisplayName("POSITIVE: >= 1 PB đề xuất → pass")
        void duPhanBienDeXuat_pass() {
            givenValidSetup(); // setup mặc định đã có 2

            assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
        }

        @Test
        @DisplayName("NEGATIVE: không có PB đề xuất nào → ném GUARD_THIEU_PHAN_BIEN")
        void thieuPhanBienDeXuat_fail() {
            when(deTaiRepository.findById(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));
            when(taiLieuRepository.existsByDeTaiIdAndLoaiFile(DE_TAI_ID, "THUYET_MINH"))
                    .thenReturn(true);
            when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID))
                    .thenReturn(0L); // Không có ai

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> guard.check(DE_TAI_ID, ACTOR_ID));

            assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_PHAN_BIEN");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FULL HAPPY PATH — tất cả 4 guards pass
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FULL POSITIVE: đủ mọi điều kiện → không ném exception")
    void fullHappyPath_tatCaGuardPass() {
        givenValidSetup();

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────────────────

    /** Setup mặc định: tất cả điều kiện hợp lệ */
    private void givenValidSetup() {
        // Guard gọi findById (có @EntityGraph bao gồm chuNhiem)
        when(deTaiRepository.findById(DE_TAI_ID))
                .thenReturn(Optional.of(mockDeTai));
        when(taiLieuRepository.existsByDeTaiIdAndLoaiFile(DE_TAI_ID, "THUYET_MINH"))
                .thenReturn(true);          // Có thuyết minh
        when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID))
                .thenReturn(2L);            // Đủ >= 1 PB đề xuất
    }
}
