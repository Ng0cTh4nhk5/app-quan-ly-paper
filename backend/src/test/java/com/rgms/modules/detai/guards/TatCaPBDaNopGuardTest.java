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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * TatCaPBDaNopGuardTest — DoD: 1 positive + 1 negative.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TatCaPBDaNopGuard — Tất cả PB phải đã nộp kết quả")
class TatCaPBDaNopGuardTest {

    @Mock ToPhanBienRepository toPhanBienRepository;
    @InjectMocks TatCaPBDaNopGuard guard;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID  = UUID.randomUUID();

    @Test
    @DisplayName("POSITIVE: tất cả PB đã nộp (countChuaNop = 0) → pass")
    void tatCaDaNop_pass() {
        when(toPhanBienRepository.countPhanBienChuaNopByDeTaiId(DE_TAI_ID)).thenReturn(0L);

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
    }

    @Test
    @DisplayName("NEGATIVE: còn 2 PB chưa nộp → ném GUARD_PB_CHUA_NOP")
    void conPbChuaNop_fail() {
        when(toPhanBienRepository.countPhanBienChuaNopByDeTaiId(DE_TAI_ID)).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_PB_CHUA_NOP");
        assertThat(ex.getMessage()).contains("2");
    }
}
