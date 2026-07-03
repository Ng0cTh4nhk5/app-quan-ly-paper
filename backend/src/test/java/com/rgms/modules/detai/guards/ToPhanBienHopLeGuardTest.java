package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.fsm.guards.ToPhanBienHopLeGuard;
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
 * ToPhanBienHopLeGuardTest — Unit test.
 * DoD: 1 test positive + 1 test negative.
 *
 * Guard dùng ToPhanBienRepository.countThanhVienByDeTaiId()
 * (JPQL count qua quan hệ toPhanBien.deTai.id).
 *
 * Đã cập nhật: dùng Long ID + đúng repository/method.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ToPhanBienHopLeGuard — Tổ phản biện phải có >= 2 thành viên")
class ToPhanBienHopLeGuardTest {

    // Guard inject ToPhanBienRepository — không phải ThanhVienToPhanBienRepository
    @Mock
    private ToPhanBienRepository toPhanBienRepository;

    @InjectMocks
    private ToPhanBienHopLeGuard guard;

    // Dùng Long thay UUID — khớp với BaseEntity @Id Long
    private static final Long DE_TAI_ID = 1L;
    private static final Long ACTOR_ID  = 2L;

    @Test
    @DisplayName("POSITIVE: Tổ phản biện có >= 2 thành viên → pass")
    void duThanhVien_pass() {
        when(toPhanBienRepository.countThanhVienByDeTaiId(DE_TAI_ID)).thenReturn(2L);

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, ACTOR_ID));
    }

    @Test
    @DisplayName("NEGATIVE: Tổ phản biện có 1 thành viên → ném GUARD_THIEU_THANH_VIEN_PB")
    void thieuThanhVien_fail() {
        when(toPhanBienRepository.countThanhVienByDeTaiId(DE_TAI_ID)).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_THANH_VIEN_PB");
    }

    @Test
    @DisplayName("NEGATIVE: Tổ phản biện có 0 thành viên → ném GUARD_THIEU_THANH_VIEN_PB")
    void khongCoThanhVien_fail() {
        when(toPhanBienRepository.countThanhVienByDeTaiId(DE_TAI_ID)).thenReturn(0L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, ACTOR_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_THIEU_THANH_VIEN_PB");
    }
}
