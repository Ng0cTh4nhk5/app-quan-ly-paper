package com.rgms.modules.detai.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.fsm.guards.ChuNhiemGuard;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.nguoidung.entity.NguoiDung;
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
 * ChuNhiemGuardTest — Unit test.
 * DoD: 1 test positive + 1 test negative.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChuNhiemGuard — Chỉ chủ nhiệm mới được thao tác")
class ChuNhiemGuardTest {

    @Mock
    private DeTaiRepository deTaiRepository;

    @InjectMocks
    private ChuNhiemGuard guard;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID CHU_NHIEM_ID = UUID.randomUUID();
    private static final UUID NGUOI_KHAC_ID = UUID.randomUUID();

    private DeTai mockDeTai;
    private NguoiDung mockChuNhiem;

    @BeforeEach
    void setUp() {
        mockChuNhiem = new NguoiDung();
        setId(mockChuNhiem, CHU_NHIEM_ID);

        mockDeTai = new DeTai();
        mockDeTai.setChuNhiem(mockChuNhiem);
    }

    @Test
    @DisplayName("POSITIVE: actorId là chủ nhiệm đề tài → pass")
    void isChuNhiem_pass() {
        when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                .thenReturn(Optional.of(mockDeTai));

        assertDoesNotThrow(() -> guard.check(DE_TAI_ID, CHU_NHIEM_ID));
    }

    @Test
    @DisplayName("NEGATIVE: actorId KHÔNG phải chủ nhiệm đề tài → ném GUARD_NOT_CHU_NHIEM")
    void notChuNhiem_fail() {
        when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                .thenReturn(Optional.of(mockDeTai));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, NGUOI_KHAC_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_NOT_CHU_NHIEM");
    }

    @Test
    @DisplayName("NEGATIVE: Đề tài không tồn tại → ném GUARD_NOT_FOUND")
    void detaiNotFound_fail() {
        when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> guard.check(DE_TAI_ID, CHU_NHIEM_ID));

        assertThat(ex.getErrorCode()).isEqualTo("GUARD_NOT_FOUND");
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = com.rgms.shared.model.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
