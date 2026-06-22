package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.fsm.guards.GvDaDongYHopDongGuard;
import com.rgms.modules.detai.repository.DeTaiRepository;
import com.rgms.shared.enums.TopicState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * GvDaDongYHopDongGuardTest — Unit test cho guard ký hợp đồng.
 * DoD: 1 test positive + 1 test negative.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GvDaDongYHopDongGuard — GV phải đồng ý HĐ trước khi ký")
class GvDaDongYHopDongGuardTest {

    @Mock DeTaiRepository deTaiRepository;
    @InjectMocks GvDaDongYHopDongGuard guard;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID  = UUID.randomUUID();

    private DeTai mockDeTai;

    @BeforeEach
    void setUp() {
        mockDeTai = new DeTai();
        mockDeTai.setStatus(TopicState.DANG_LAP_HOP_DONG);
    }

    @Test
    @DisplayName("POSITIVE: gvDaDongYHopDong = true → không ném exception")
    void gvDaDongY_pass() {
        mockDeTai.setGvDaDongYHopDong(true);
        when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
    }

    @Test
    @DisplayName("NEGATIVE: gvDaDongYHopDong = false → ném GUARD_GV_CHUA_DONG_Y_HD")
    void gvChuaDongY_fail() {
        mockDeTai.setGvDaDongYHopDong(false);
        when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_GV_CHUA_DONG_Y_HD");
    }

    @Test
    @DisplayName("NEGATIVE: gvDaDongYHopDong = null → ném GUARD_GV_CHUA_DONG_Y_HD")
    void gvNullFlag_fail() {
        mockDeTai.setGvDaDongYHopDong(null);
        when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

        assertThrows(BusinessException.class, () -> guard.check(DE_TAI_ID, ACTOR_ID));
    }
}
