package com.rgms.modules.detai;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.fsm.FsmService;
import com.rgms.modules.detai.repo.AuditLogRepository;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.detai.repo.DetaiNguoiDungRepository;
import com.rgms.shared.enums.TopicEvent;
import com.rgms.shared.enums.TopicState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FsmServiceTest — Unit test cho FsmService (sop-member-a.md Giai đoạn 4).
 *
 * DoD checklist:
 *   [x] Mỗi guard có ít nhất 1 test positive + 1 test negative
 *   [x] Tất cả 12 state transitions được test (happy path)
 *   [x] Terminal state → ném BusinessException
 *   [x] Event sai tại state → ném BusinessException
 *   [x] AuditLog được ghi sau mỗi transition thành công
 *
 * Tham chiếu: sop-member-a.md Bước 6.1 — FsmServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FsmService — State Machine Unit Tests")
class FsmServiceTest {

    @Mock DeTaiRepository deTaiRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock DetaiNguoiDungRepository nguoiDungRepository;

    @InjectMocks FsmService fsmService;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID  = UUID.randomUUID();

    private DeTai mockDeTai;
    private NguoiDung mockActor;

    @BeforeEach
    void setUp() {
        mockActor = new NguoiDung();
        mockActor.setHoTen("Test Actor");
        mockActor.setRole(Role.PNCKH);

        mockDeTai = new DeTai();
        mockDeTai.setTenDeTai("Đề tài test FSM");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HELPER
    // ──────────────────────────────────────────────────────────────────────────

    private DeTai givenDeTaiAtState(TopicState state) {
        mockDeTai.setStatus(state);
        when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID))
                .thenReturn(Optional.of(mockDeTai));
        when(deTaiRepository.save(any(DeTai.class))).thenAnswer(inv -> inv.getArgument(0));
        when(nguoiDungRepository.findById(ACTOR_ID)).thenReturn(Optional.of(mockActor));
        return mockDeTai;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 1: HAPPY PATH — 12 transitions cơ bản
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Happy Path — 12 transitions Phase 1")
    class HappyPathTransitions {

        @Test
        @DisplayName("T02: DRAFT + GV_GUI_HO_SO → CHO_PNCKH_XEM_XET")
        void t02_draft_guiHoSo_chuyenSangChoPnckh() {
            givenDeTaiAtState(TopicState.DRAFT);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.CHO_PNCKH_XEM_XET);
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("T03: CHO_PNCKH_XEM_XET + TIEP_NHAN → DANG_XEM_XET_BOI_PNCKH")
        void t03_choPnckh_tiepNhan_chuyenSangDangXemXet() {
            givenDeTaiAtState(TopicState.CHO_PNCKH_XEM_XET);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_TIEP_NHAN, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DANG_XEM_XET_BOI_PNCKH);
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("T04: DANG_XEM_XET + YEU_CAU_BO_SUNG → CHO_BO_SUNG_HO_SO")
        void t04_dangXemXet_yeuCauBoSung_chuyenSangChoBoSung() {
            givenDeTaiAtState(TopicState.DANG_XEM_XET_BOI_PNCKH);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_YEU_CAU_BO_SUNG, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.CHO_BO_SUNG_HO_SO);
        }

        @Test
        @DisplayName("T05: CHO_BO_SUNG_HO_SO + GV_NOP_BO_SUNG → DANG_XEM_XET_BOI_PNCKH")
        void t05_choBoSung_gvNopBoSung_quayVeDangXemXet() {
            givenDeTaiAtState(TopicState.CHO_BO_SUNG_HO_SO);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.GV_NOP_BO_SUNG, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DANG_XEM_XET_BOI_PNCKH);
        }

        @Test
        @DisplayName("T06: DANG_XEM_XET + PNCKH_LAP_TO_PB → DANG_PHAN_BIEN")
        void t06_dangXemXet_lapToPhanBien_chuyenSangDangPhanBien() {
            givenDeTaiAtState(TopicState.DANG_XEM_XET_BOI_PNCKH);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_LAP_TO_PB, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DANG_PHAN_BIEN);
        }

        @Test
        @DisplayName("T07: DANG_PHAN_BIEN + YEU_CAU_SUA_TM → CHO_CHINH_SUA_THUYET_MINH")
        void t07_dangPhanBien_yeuCauSuaTm_chuyenSangChoChinhSua() {
            givenDeTaiAtState(TopicState.DANG_PHAN_BIEN);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_YEU_CAU_SUA_TM, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.CHO_CHINH_SUA_THUYET_MINH);
        }

        @Test
        @DisplayName("T08: CHO_CHINH_SUA_TM + GV_NOP_SUA_TM → DANG_PHAN_BIEN")
        void t08_choChinhSua_gvNopSuaTm_quayVeDangPhanBien() {
            givenDeTaiAtState(TopicState.CHO_CHINH_SUA_THUYET_MINH);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.GV_NOP_SUA_TM, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DANG_PHAN_BIEN);
        }

        @Test
        @DisplayName("T09: DANG_PHAN_BIEN + ACCEPT_PB → DANG_LAP_HOP_DONG")
        void t09_dangPhanBien_acceptPb_chuyenSangDangLapHd() {
            givenDeTaiAtState(TopicState.DANG_PHAN_BIEN);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_ACCEPT_PB, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DANG_LAP_HOP_DONG);
        }

        @Test
        @DisplayName("T10: DANG_LAP_HOP_DONG + HAI_BEN_KY_HD → DANG_THUC_HIEN")
        void t10_dangLapHd_kyHd_chuyenSangDangThucHien() {
            givenDeTaiAtState(TopicState.DANG_LAP_HOP_DONG);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.HAI_BEN_KY_HD, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DANG_THUC_HIEN);
        }

        @Test
        @DisplayName("E01: DRAFT + HE_THONG_TREO → BI_TREO")
        void e01_draft_heThongTreo_chuyenSangBiTreo() {
            givenDeTaiAtState(TopicState.DRAFT);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.HE_THONG_TREO, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.BI_TREO);
        }

        @Test
        @DisplayName("E03: DANG_XEM_XET + TU_CHOI → BI_TU_CHOI")
        void e03_dangXemXet_tuChoi_chuyenSangBiTuChoi() {
            givenDeTaiAtState(TopicState.DANG_XEM_XET_BOI_PNCKH);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_TU_CHOI, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.BI_TU_CHOI);
        }

        @Test
        @DisplayName("E21: DRAFT + GV_RUT → DA_RUT")
        void e21_draft_gvRut_chuyenSangDaRut() {
            givenDeTaiAtState(TopicState.DRAFT);

            DeTai result = fsmService.transition(DE_TAI_ID, TopicEvent.GV_RUT, ACTOR_ID, List.of());

            assertThat(result.getStatus()).isEqualTo(TopicState.DA_RUT);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 2: TERMINAL STATE — không cho phép bất kỳ transition nào
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Terminal States — BusinessException khi cố transition")
    class TerminalStateTests {

        @Test
        @DisplayName("BI_TU_CHOI + GV_GUI_HO_SO → ném BusinessException")
        void biTuChoi_khongChoTransition() {
            givenDeTaiAtState(TopicState.BI_TU_CHOI);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of()));

            assertThat(ex.getErrorCode()).isEqualTo("FSM_TERMINAL_STATE");
            verify(auditLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("DA_RUT + PNCKH_TIEP_NHAN → ném BusinessException")
        void daRut_khongChoTransition() {
            givenDeTaiAtState(TopicState.DA_RUT);

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_TIEP_NHAN, ACTOR_ID, List.of()));
        }

        @Test
        @DisplayName("BI_TREO + bất kỳ event → ném BusinessException")
        void biTreo_khongChoTransition() {
            givenDeTaiAtState(TopicState.BI_TREO);

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of()));
        }

        @Test
        @DisplayName("DA_HUY + bất kỳ event → ném BusinessException")
        void daHuy_khongChoTransition() {
            givenDeTaiAtState(TopicState.DA_HUY);

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 3: EVENT SAI TẠI STATE HIỆN TẠI
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Invalid Transition — BusinessException với code FSM_INVALID_TRANSITION")
    class InvalidTransitionTests {

        @Test
        @DisplayName("DRAFT + PNCKH_TIEP_NHAN → không hợp lệ")
        void draft_tiepNhan_nemException() {
            givenDeTaiAtState(TopicState.DRAFT);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_TIEP_NHAN, ACTOR_ID, List.of()));

            assertThat(ex.getErrorCode()).isEqualTo("FSM_INVALID_TRANSITION");
        }

        @Test
        @DisplayName("CHO_PNCKH_XEM_XET + HAI_BEN_KY_HD → không hợp lệ")
        void choPnckh_kyHd_nemException() {
            givenDeTaiAtState(TopicState.CHO_PNCKH_XEM_XET);

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.HAI_BEN_KY_HD, ACTOR_ID, List.of()));
        }

        @Test
        @DisplayName("DANG_THUC_HIEN + GV_GUI_HO_SO → không hợp lệ")
        void dangThucHien_guiHoSo_nemException() {
            givenDeTaiAtState(TopicState.DANG_THUC_HIEN);

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 4: GUARD FAIL — guard ném exception → AuditLog không được ghi
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Guard Fail — AuditLog không được ghi khi guard thất bại")
    class GuardFailTests {

        @Test
        @DisplayName("Guard ném exception → AuditLog KHÔNG được ghi (BR-15 không bị vi phạm)")
        void guardFail_khongGhiAuditLog() {
            givenDeTaiAtState(TopicState.DRAFT);

            // Guard luôn fail
            com.rgms.shared.fsm.TransitionGuard failGuard = (deTaiId, actorId) -> {
                throw new BusinessException("GUARD_TEST", "Guard test fail");
            };

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of(failGuard)));

            verify(auditLogRepository, never()).save(any());
            verify(deTaiRepository, never()).save(any());
        }

        @Test
        @DisplayName("Guard pass → transition thực hiện → AuditLog được ghi")
        void guardPass_ghiAuditLog() {
            givenDeTaiAtState(TopicState.DRAFT);

            // Guard luôn pass
            com.rgms.shared.fsm.TransitionGuard passGuard = (deTaiId, actorId) -> {};

            DeTai result = fsmService.transition(
                    DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of(passGuard));

            assertThat(result.getStatus()).isEqualTo(TopicState.CHO_PNCKH_XEM_XET);
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Nhiều guards — guard đầu tiên fail → guard sau không chạy")
        void multipleGuards_failFast() {
            givenDeTaiAtState(TopicState.DRAFT);

            com.rgms.shared.fsm.TransitionGuard failGuard = (deTaiId, actorId) -> {
                throw new BusinessException("FIRST_GUARD_FAIL", "Guard 1 fail");
            };
            com.rgms.shared.fsm.TransitionGuard shouldNotRun = mock(com.rgms.shared.fsm.TransitionGuard.class);

            assertThrows(BusinessException.class,
                    () -> fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID,
                            List.of(failGuard, shouldNotRun)));

            verify(shouldNotRun, never()).check(any(), any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 5: AUDIT LOG — nội dung đúng
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AuditLog — nội dung được ghi đúng sau mỗi transition")
    class AuditLogTests {

        @Test
        @DisplayName("AuditLog chứa đúng hanhDong, trangThaiTruoc, trangThaiSau")
        void auditLog_noiDungDung() {
            givenDeTaiAtState(TopicState.DRAFT);

            fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of());

            verify(auditLogRepository).save(argThat(log ->
                    log.getHanhDong().equals("GV_GUI_HO_SO")
                    && log.getTrangThaiTruoc().equals("DRAFT")
                    && log.getTrangThaiSau().equals("CHO_PNCKH_XEM_XET")
            ));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 6: ENTITY NOT FOUND
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Entity Not Found — BusinessException khi không tìm thấy đề tài/actor")
    class EntityNotFoundTests {

        @Test
        @DisplayName("DeTai không tồn tại → FSM_NOT_FOUND")
        void deTaiKhongTonTai_nemException() {
            when(deTaiRepository.findByIdWithChuNhiem(any())).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> fsmService.transition(UUID.randomUUID(), TopicEvent.GV_GUI_HO_SO, ACTOR_ID, List.of()));

            assertThat(ex.getErrorCode()).isEqualTo("FSM_NOT_FOUND");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NHÓM 7: canTransition — không throw exception
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("canTransition — kiểm tra không throw exception")
    class CanTransitionTests {

        @Test
        @DisplayName("DRAFT có thể nhận GV_GUI_HO_SO → true")
        void draft_canGuiHoSo() {
            mockDeTai.setStatus(TopicState.DRAFT);
            when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

            boolean result = fsmService.canTransition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("BI_TU_CHOI không thể nhận bất kỳ event → false")
        void biTuChoi_cannotTransition() {
            mockDeTai.setStatus(TopicState.BI_TU_CHOI);
            when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

            boolean result = fsmService.canTransition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("DRAFT không thể nhận PNCKH_TIEP_NHAN → false")
        void draft_cannotTiepNhan() {
            mockDeTai.setStatus(TopicState.DRAFT);
            when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

            boolean result = fsmService.canTransition(DE_TAI_ID, TopicEvent.PNCKH_TIEP_NHAN);

            assertThat(result).isFalse();
        }
    }
}
