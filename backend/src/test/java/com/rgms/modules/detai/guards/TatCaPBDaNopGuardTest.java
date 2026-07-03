package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.fsm.guards.TatCaPBDaNopGuard;
import com.rgms.modules.detai.repo.ToPhanBienRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * TatCaPBDaNopGuardTest — DoD: 1 positive + 1 negative.
 *
 * Guard logic:
 *   1. Nếu tongSo thành viên == 0 → GUARD_CHUA_CO_TO_PB
 *   2. Nếu còn soChuaNop > 0     → GUARD_PB_CHUA_NOP
 *
 * Đã cập nhật: dùng Long ID thay UUID (theo BaseEntity @Id Long).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TatCaPBDaNopGuard — Tất cả PB phải đã nộp kết quả")
class TatCaPBDaNopGuardTest {

    @Mock ToPhanBienRepository toPhanBienRepository;
    @InjectMocks TatCaPBDaNopGuard guard;

    // Dùng Long thay UUID — khớp với BaseEntity @Id Long
    private static final Long DE_TAI_ID = 1L;
    private static final Long ACTOR_ID  = 2L;

    @Test
    @DisplayName("POSITIVE: tất cả PB đã nộp (countChuaNop = 0) → pass")
    void tatCaDaNop_pass() {
        // Guard kiểm tra tongSo trước để bắt edge case tổ rỗng
        when(toPhanBienRepository.countThanhVienByDeTaiId(DE_TAI_ID)).thenReturn(3L);
        when(toPhanBienRepository.countPhanBienChuaNopByDeTaiId(DE_TAI_ID)).thenReturn(0L);

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
    }

    @Test
    @DisplayName("NEGATIVE: còn 2 PB chưa nộp → ném GUARD_PB_CHUA_NOP")
    void conPbChuaNop_fail() {
        when(toPhanBienRepository.countThanhVienByDeTaiId(DE_TAI_ID)).thenReturn(3L);
        when(toPhanBienRepository.countPhanBienChuaNopByDeTaiId(DE_TAI_ID)).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_PB_CHUA_NOP");
        assertThat(ex.getMessage()).contains("2");
    }

    @Test
    @DisplayName("NEGATIVE (edge case): tổ phản biện rỗng → ném GUARD_CHUA_CO_TO_PB")
    void toPhanBienRong_fail() {
        when(toPhanBienRepository.countThanhVienByDeTaiId(DE_TAI_ID)).thenReturn(0L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_CHUA_CO_TO_PB");
    }
}
