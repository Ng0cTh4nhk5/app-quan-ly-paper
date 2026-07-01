package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.fsm.guards.ToPhanBienHopLeGuard;
import com.rgms.modules.detai.repo.ThanhVienToPhanBienRepository;
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
 * ToPhanBienHopLeGuardTest — Unit test.
 * DoD: 1 test positive + 1 test negative.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ToPhanBienHopLeGuard — Tổ phản biện phải có >= 2 thành viên")
class ToPhanBienHopLeGuardTest {

    @Mock
    private ThanhVienToPhanBienRepository thanhVienRepository;

    @InjectMocks
    private ToPhanBienHopLeGuard guard;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Test
    @DisplayName("POSITIVE: Tổ phản biện có >= 2 thành viên → pass")
    void duThanhVien_pass() {
        when(thanhVienRepository.countByToPhanBienDeTaiId(DE_TAI_ID)).thenReturn(2L);

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
    }

    @Test
    @DisplayName("NEGATIVE: Tổ phản biện có 1 thành viên → ném GUARD_THIEU_THANH_VIEN_PB")
    void thieuThanhVien_fail() {
        when(thanhVienRepository.countByToPhanBienDeTaiId(DE_TAI_ID)).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_THANH_VIEN_PB");
    }

    @Test
    @DisplayName("NEGATIVE: Tổ phản biện có 0 thành viên → ném GUARD_THIEU_THANH_VIEN_PB")
    void khongCoThanhVien_fail() {
        when(thanhVienRepository.countByToPhanBienDeTaiId(DE_TAI_ID)).thenReturn(0L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_THANH_VIEN_PB");
    }
}
