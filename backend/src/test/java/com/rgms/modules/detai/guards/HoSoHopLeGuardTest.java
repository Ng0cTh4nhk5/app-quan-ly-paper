package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.PhanBienDeXuat;
import com.rgms.modules.detai.fsm.guards.HoSoHopLeGuard;
import com.rgms.modules.detai.repository.DeTaiRepository;
import com.rgms.modules.detai.repository.PhanBienDeXuatRepository;
import com.rgms.modules.detai.repository.TaiLieuRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * HoSoHopLeGuardTest — Unit test cho HoSoHopLeGuard.
 *
 * Mỗi guard condition phải có 1 test positive (pass) + 1 test negative (fail).
 * Tham chiếu: sop-member-a.md DoD — "Mỗi guard có ít nhất 1 unit test positive + 1 test negative"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HoSoHopLeGuard — Guard kiểm tra hồ sơ hợp lệ trước khi GV gửi")
class HoSoHopLeGuardTest {

    @Mock DeTaiRepository deTaiRepository;
    @Mock TaiLieuRepository taiLieuRepository;
    @Mock PhanBienDeXuatRepository phanBienDeXuatRepository;

    @InjectMocks HoSoHopLeGuard guard;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID  = UUID.randomUUID();

    private DeTai mockDeTai;
    private NguoiDung mockGv;

    @BeforeEach
    void setUp() {
        mockGv = new NguoiDung();
        // Dùng reflection để set ID vì BaseEntity dùng @GeneratedValue
        setId(mockGv, ACTOR_ID);

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
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
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
            UUID khacId = UUID.randomUUID(); // actor khác với chủ nhiệm
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
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
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));
            when(taiLieuRepository.existsByDeTaiIdAndLoai(DE_TAI_ID, "THUYET_MINH"))
                    .thenReturn(false); // Không có thuyết minh

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> guard.check(DE_TAI_ID, ACTOR_ID));

            assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_THUYET_MINH");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GUARD [4]: Phải có >= 2 người phản biện đề xuất
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Guard [4]: >= 2 phản biện đề xuất")
    class PhanBienDeXuatGuardTests {

        @Test
        @DisplayName("POSITIVE: >= 2 PB đề xuất → pass")
        void duPhanBienDeXuat_pass() {
            givenValidSetup(); // setup mặc định đã có 2

            assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
        }

        @Test
        @DisplayName("NEGATIVE: chỉ có 1 PB đề xuất → ném GUARD_THIEU_PHAN_BIEN")
        void thieuPhanBienDeXuat_fail() {
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));
            when(taiLieuRepository.existsByDeTaiIdAndLoai(DE_TAI_ID, "THUYET_MINH"))
                    .thenReturn(true);
            when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID))
                    .thenReturn(1L); // Chỉ có 1 (cần >= 2)

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> guard.check(DE_TAI_ID, ACTOR_ID));

            assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_PHAN_BIEN");
            assertThat(ex.getMessage()).contains("1");
        }

        @Test
        @DisplayName("NEGATIVE: không có PB đề xuất nào → ném GUARD_THIEU_PHAN_BIEN")
        void khongCoPhanBienDeXuat_fail() {
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                    .thenReturn(Optional.of(mockDeTai));
            when(taiLieuRepository.existsByDeTaiIdAndLoai(DE_TAI_ID, "THUYET_MINH"))
                    .thenReturn(true);
            when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID))
                    .thenReturn(0L);

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
        when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                .thenReturn(Optional.of(mockDeTai));
        when(taiLieuRepository.existsByDeTaiIdAndLoai(DE_TAI_ID, "THUYET_MINH"))
                .thenReturn(true);          // Có thuyết minh
        when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID))
                .thenReturn(2L);            // Đủ >= 2 PB đề xuất
    }

    /** Dùng reflection để set UUID id trong BaseEntity (không có constructor nhận id) */
    private void setId(Object entity, UUID id) {
        try {
            var field = com.rgms.shared.model.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Không thể set id bằng reflection", e);
        }
    }
}
