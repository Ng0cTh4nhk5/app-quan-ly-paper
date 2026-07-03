package com.rgms.modules.detai.service;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.dto.KetQuaPBRequest;
import com.rgms.modules.detai.dto.LapToPhanBienRequest;
import com.rgms.modules.detai.dto.PhanHoiHopDongRequest;
import com.rgms.modules.detai.dto.TaoDeTaiRequest;
import com.rgms.modules.detai.dto.XetDuyetPBRequest;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.Feedback;
import com.rgms.modules.detai.entity.HopDong;
import com.rgms.modules.detai.entity.KyNCKH;
import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import com.rgms.modules.detai.entity.ToPhanBien;
import com.rgms.modules.detai.fsm.FsmService;
import com.rgms.modules.detai.mapper.DeTaiMapper;
import com.rgms.modules.detai.repo.AuditLogRepository;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.repo.FeedbackRepository;
import com.rgms.modules.detai.repo.HopDongRepository;
import com.rgms.modules.detai.repo.KyNCKHRepository;
import com.rgms.modules.detai.repo.DetaiNguoiDungRepository;
import com.rgms.modules.detai.repo.PhanBienDeXuatRepository;
import com.rgms.modules.detai.repo.ThanhVienToPhanBienRepository;
import com.rgms.modules.detai.repo.ToPhanBienRepository;
import com.rgms.modules.email.EmailService;
import com.rgms.modules.files.mapper.TaiLieuMapper;
import com.rgms.modules.files.repo.TaiLieuRepository;
import com.rgms.modules.files.service.FileUploadService;
import com.rgms.modules.nguoidung.entity.DonVi;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.enums.TopicEvent;
import com.rgms.shared.enums.TopicState;
import com.rgms.shared.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResearchTopicService - FSM integration tests")
class ResearchTopicServiceTest {

    private static final Long DE_TAI_ID = 1L;
    private static final Long GV_ID = 2L;
    private static final Long NCKH_ID = 3L;
    private static final Long KY_NCKH_ID = 4L;
    private static final Long PB_1_ID = 5L;
    private static final Long PB_2_ID = 6L;

    @Mock FsmService fsmService;
    @Mock DeTaiRepository deTaiRepository;
    @Mock KyNCKHRepository kyNckhRepository;
    @Mock DetaiNguoiDungRepository nguoiDungRepository;
    @Mock TaiLieuRepository taiLieuRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock PhanBienDeXuatRepository phanBienDeXuatRepository;
    @Mock FeedbackRepository feedbackRepository;
    @Mock ToPhanBienRepository toPhanBienRepository;
    @Mock ThanhVienToPhanBienRepository thanhVienToPhanBienRepository;
    @Mock HopDongRepository hopDongRepository;
    @Mock DeTaiMapper deTaiMapper;
    @Mock TaiLieuMapper taiLieuMapper;
    @Mock FileUploadService fileUploadService;
    @Mock EmailService emailService;
    @Mock DeTaiResponse mappedResponse;

    @InjectMocks
    ResearchTopicService service;

    private NguoiDung mockGv;
    private NguoiDung mockNckh;
    private KyNCKH mockKyNckh;
    private DeTai mockDeTai;

    @BeforeEach
    void setUp() {
        mockGv = user(GV_ID, "GIANG_VIEN", "gv@rgms.test");
        mockGv.setDonVi(new DonVi());
        mockNckh = user(NCKH_ID, "NCKH", "nckh@rgms.test");

        mockKyNckh = new KyNCKH();
        mockKyNckh.setId(KY_NCKH_ID);
        mockKyNckh.setTrangThai("DANG_MO");

        mockDeTai = new DeTai();
        mockDeTai.setId(DE_TAI_ID);
        mockDeTai.setMaSo("NCKH-2026-0001");
        mockDeTai.setTenDeTai("De tai kiem thu");
        mockDeTai.setStatus(TopicState.DRAFT);
        mockDeTai.setChuNhiem(mockGv);
    }

    @Nested
    @DisplayName("1. taoDeTai()")
    class TaoDeTaiTests {

        @Test
        @DisplayName("Thanh cong: tao de tai o trang thai DRAFT voi ID Long")
        void createsDraftTopicWithLongIds() {
            TaoDeTaiRequest request = taoDeTaiRequest();
            stubUser(mockGv);
            when(kyNckhRepository.findById(KY_NCKH_ID)).thenReturn(Optional.of(mockKyNckh));
            when(deTaiRepository.nextMaSoSequence()).thenReturn(1L);
            when(deTaiRepository.save(any(DeTai.class))).thenAnswer(invocation -> invocation.getArgument(0));
            stubMappedResponse();

            DeTaiResponse result = service.taoDeTai(request, GV_ID);

            ArgumentCaptor<DeTai> topicCaptor = ArgumentCaptor.forClass(DeTai.class);
            verify(deTaiRepository).save(topicCaptor.capture());
            assertThat(topicCaptor.getValue().getStatus()).isEqualTo(TopicState.DRAFT);
            assertThat(topicCaptor.getValue().getChuNhiem()).isSameAs(mockGv);
            assertThat(result).isSameAs(mappedResponse);
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Loi: nguoi tao khong co vai tro GIANG_VIEN")
        void rejectsNonLecturer() {
            NguoiDung invalidActor = user(GV_ID, "NCKH", "actor@rgms.test");
            stubUser(invalidActor);

            assertThrows(AccessDeniedException.class, () -> service.taoDeTai(taoDeTaiRequest(), GV_ID));

            verify(deTaiRepository, never()).save(any(DeTai.class));
            verify(fsmService, never()).transition(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("2. danhSach()")
    class DanhSachTests {

        @Test
        @DisplayName("Thanh cong: GV loc bang TopicState va chi xem de tai cua minh")
        void filtersLecturerTopicsWithTypedStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            CustomUserDetails currentUser = currentUser(GV_ID, "GIANG_VIEN");
            when(deTaiRepository.findByChuNhiem_IdAndStatus(GV_ID, TopicState.DRAFT, pageable))
                    .thenReturn(new PageImpl<>(List.of(mockDeTai), pageable, 1));
            stubMappedResponse();

            var result = service.danhSach(" draft ", pageable, currentUser);

            assertThat(result.getContent()).containsExactly(mappedResponse);
            verify(deTaiRepository).findByChuNhiem_IdAndStatus(GV_ID, TopicState.DRAFT, pageable);
            verify(deTaiRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Thanh cong: NCKH loc tat ca de tai bang TopicState")
        void filtersAllTopicsWithTypedStatusForNckh() {
            Pageable pageable = PageRequest.of(0, 10);
            CustomUserDetails currentUser = currentUser(NCKH_ID, "NCKH");
            when(deTaiRepository.findByStatus(TopicState.DANG_PHAN_BIEN, pageable))
                    .thenReturn(new PageImpl<>(List.of(mockDeTai), pageable, 1));
            stubMappedResponse();

            var result = service.danhSach("dang_phan_bien", pageable, currentUser);

            assertThat(result.getContent()).containsExactly(mappedResponse);
            verify(deTaiRepository).findByStatus(TopicState.DANG_PHAN_BIEN, pageable);
            verify(deTaiRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Loi: trang thai khong hop le bi chan truoc repository")
        void rejectsInvalidStatusBeforeRepositoryCall() {
            Pageable pageable = PageRequest.of(0, 10);
            CustomUserDetails currentUser = currentUser(NCKH_ID, "NCKH");

            BusinessException thrown = assertThrows(
                    BusinessException.class,
                    () -> service.danhSach("KHONG_TON_TAI", pageable, currentUser));

            assertThat(thrown.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            verifyNoInteractions(deTaiRepository);
        }
    }

    @Nested
    @DisplayName("3. guiHoSo()")
    class GuiHoSoTests {

        @Test
        @DisplayName("Thanh cong: goi FSM voi event GV_GUI_HO_SO")
        void delegatesToFsm() {
            stubUser(mockGv);
            stubTopic(TopicState.DRAFT);
            when(taiLieuRepository.existsByDeTaiIdAndLoaiFile(DE_TAI_ID, "THUYET_MINH")).thenReturn(true);
            when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID)).thenReturn(1L);
            when(fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, GV_ID)).thenReturn(mockDeTai);
            stubMappedResponse();

            DeTaiResponse result = service.guiHoSo(DE_TAI_ID, GV_ID);

            assertThat(result).isSameAs(mappedResponse);
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, GV_ID);
            verify(nguoiDungRepository).findByVaiTro("NCKH");
        }

        @Test
        @DisplayName("Loi FSM: FSM_TERMINAL_STATE duoc propagate va khong gui email")
        void propagatesTerminalStateError() {
            stubUser(mockGv);
            stubTopic(TopicState.DRAFT);
            when(taiLieuRepository.existsByDeTaiIdAndLoaiFile(DE_TAI_ID, "THUYET_MINH")).thenReturn(true);
            when(phanBienDeXuatRepository.countByDeTaiId(DE_TAI_ID)).thenReturn(1L);
            BusinessException fsmError = new BusinessException("FSM_TERMINAL_STATE", "Trang thai ket thuc");
            when(fsmService.transition(DE_TAI_ID, TopicEvent.GV_GUI_HO_SO, GV_ID)).thenThrow(fsmError);

            BusinessException thrown = assertThrows(
                    BusinessException.class,
                    () -> service.guiHoSo(DE_TAI_ID, GV_ID));

            assertThat(thrown).isSameAs(fsmError);
            assertThat(thrown.getErrorCode()).isEqualTo("FSM_TERMINAL_STATE");
            verify(emailService, never()).guiEmail(any(), any(), any(), anyMap());
            verify(deTaiMapper, never()).toResponse(any(DeTai.class));
        }
    }

    @Nested
    @DisplayName("4. tiepNhan()")
    class TiepNhanTests {

        @Test
        @DisplayName("Loi: actor khong co vai tro NCKH")
        void rejectsNonNckhActorBeforeFsm() {
            NguoiDung invalidActor = user(NCKH_ID, "GIANG_VIEN", "invalid@rgms.test");
            stubUser(invalidActor);

            assertThrows(AccessDeniedException.class, () -> service.tiepNhan(DE_TAI_ID, NCKH_ID));

            verify(deTaiRepository, never()).findById(any());
            verify(fsmService, never()).transition(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("5. lapToPhanBien()")
    class LapToPhanBienTests {

        @Test
        @DisplayName("Thanh cong: luu to va hai thanh vien truoc khi chuyen FSM")
        void createsTeamWithTwoValidReviewers() {
            LapToPhanBienRequest request = lapToPhanBienRequest(List.of(PB_1_ID, PB_2_ID));
            NguoiDung pb1 = user(PB_1_ID, "TO_PHAN_BIEN", "pb1@rgms.test");
            NguoiDung pb2 = user(PB_2_ID, "TO_PHAN_BIEN", "pb2@rgms.test");
            stubUser(mockNckh);
            stubTopic(TopicState.DANG_XEM_XET_BOI_PNCKH);
            when(nguoiDungRepository.findByIdIn(anyCollection())).thenReturn(List.of(pb1, pb2));
            when(toPhanBienRepository.save(any(ToPhanBien.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_LAP_TO_PB, NCKH_ID))
                    .thenReturn(mockDeTai);
            stubMappedResponse();

            DeTaiResponse result = service.lapToPhanBien(DE_TAI_ID, request, NCKH_ID);

            ArgumentCaptor<ToPhanBien> teamCaptor = ArgumentCaptor.forClass(ToPhanBien.class);
            verify(toPhanBienRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getKetQuaTongHop()).isEqualTo("CHUA_CO");
            ArgumentCaptor<ThanhVienToPhanBien> memberCaptor =
                    ArgumentCaptor.forClass(ThanhVienToPhanBien.class);
            verify(thanhVienToPhanBienRepository, times(2)).save(memberCaptor.capture());
            assertThat(memberCaptor.getAllValues())
                    .extracting(ThanhVienToPhanBien::getKetQua)
                    .containsOnly("CHUA_NOP");
            assertThat(result).isSameAs(mappedResponse);
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.PNCKH_LAP_TO_PB, NCKH_ID);
        }

        @Test
        @DisplayName("Loi: danh sach thanh vien rong")
        void rejectsEmptyMemberList() {
            stubUser(mockNckh);
            stubTopic(TopicState.DANG_XEM_XET_BOI_PNCKH);

            assertThrows(
                    BusinessException.class,
                    () -> service.lapToPhanBien(
                            DE_TAI_ID,
                            lapToPhanBienRequest(List.of()),
                            NCKH_ID));

            verify(toPhanBienRepository, never()).save(any(ToPhanBien.class));
            verify(fsmService, never()).transition(any(), any(), any());
        }

        @Test
        @DisplayName("Loi FSM: FSM_INVALID_TRANSITION duoc propagate sau side effects")
        void propagatesFsmErrorAfterPersistingTeam() {
            LapToPhanBienRequest request = lapToPhanBienRequest(List.of(PB_1_ID, PB_2_ID));
            NguoiDung pb1 = user(PB_1_ID, "TO_PHAN_BIEN", "pb1@rgms.test");
            NguoiDung pb2 = user(PB_2_ID, "TO_PHAN_BIEN", "pb2@rgms.test");
            stubUser(mockNckh);
            stubTopic(TopicState.DANG_XEM_XET_BOI_PNCKH);
            when(nguoiDungRepository.findByIdIn(anyCollection())).thenReturn(List.of(pb1, pb2));
            when(toPhanBienRepository.save(any(ToPhanBien.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            BusinessException fsmError = new BusinessException("FSM_INVALID_TRANSITION", "Transition sai");
            when(fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_LAP_TO_PB, NCKH_ID))
                    .thenThrow(fsmError);

            BusinessException thrown = assertThrows(
                    BusinessException.class,
                    () -> service.lapToPhanBien(DE_TAI_ID, request, NCKH_ID));

            assertThat(thrown).isSameAs(fsmError);
            verify(toPhanBienRepository).save(any(ToPhanBien.class));
            verify(thanhVienToPhanBienRepository, times(2)).save(any(ThanhVienToPhanBien.class));
            verify(emailService, never()).guiEmail(any(), any(), any(), anyMap());
        }
    }

    @Nested
    @DisplayName("6. pbNopKetQua()")
    class PbNopKetQuaTests {

        @Test
        @DisplayName("Loi: actor khong thuoc to phan bien")
        void rejectsActorOutsideReviewTeam() {
            NguoiDung pb = user(PB_1_ID, "TO_PHAN_BIEN", "pb@rgms.test");
            KetQuaPBRequest request = new KetQuaPBRequest();
            request.setKetQua("CHAP_NHAN");
            request.setNhanXet("Dong y");
            stubUser(pb);
            stubTopic(TopicState.DANG_PHAN_BIEN);
            when(thanhVienToPhanBienRepository
                    .findByToPhanBien_DeTaiIdAndNguoiDung_Id(DE_TAI_ID, PB_1_ID))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccessDeniedException.class,
                    () -> service.pbNopKetQua(DE_TAI_ID, request, PB_1_ID));

            verify(thanhVienToPhanBienRepository, never()).save(any(ThanhVienToPhanBien.class));
            verify(fsmService, never()).transition(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("7. xetDuyetPB()")
    class XetDuyetPBTests {

        @Test
        @DisplayName("Thanh cong: CHAP_NHAN goi event PNCKH_ACCEPT_PB")
        void acceptsReviewResult() {
            ToPhanBien team = stubReviewDecision(TopicState.DANG_PHAN_BIEN);
            when(fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_ACCEPT_PB, NCKH_ID))
                    .thenReturn(mockDeTai);
            stubMappedResponse();

            DeTaiResponse result = service.xetDuyetPB(
                    DE_TAI_ID,
                    xetDuyetRequest("CHAP_NHAN"),
                    NCKH_ID);

            assertThat(team.getKetQuaTongHop()).isEqualTo("CHAP_NHAN");
            assertThat(result).isSameAs(mappedResponse);
            verify(toPhanBienRepository).save(team);
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.PNCKH_ACCEPT_PB, NCKH_ID);
        }

        @Test
        @DisplayName("Thanh cong: YEU_CAU_SUA tao feedback va goi event PNCKH_YEU_CAU_SUA_TM")
        void requestsThesisRevision() {
            ToPhanBien team = stubReviewDecision(TopicState.DANG_PHAN_BIEN);
            XetDuyetPBRequest request = xetDuyetRequest("YEU_CAU_SUA");
            request.setNoiDungYeuCauSua("Bo sung phuong phap nghien cuu");
            request.setDeadlineNopLai(LocalDate.now().plusDays(7));
            when(fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_YEU_CAU_SUA_TM, NCKH_ID))
                    .thenReturn(mockDeTai);
            stubMappedResponse();

            service.xetDuyetPB(DE_TAI_ID, request, NCKH_ID);

            assertThat(team.getKetQuaTongHop()).isEqualTo("CAN_SUA");
            ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
            verify(feedbackRepository).save(feedbackCaptor.capture());
            assertThat(feedbackCaptor.getValue().getDeTaiId()).isEqualTo(DE_TAI_ID);
            assertThat(feedbackCaptor.getValue().getLoai()).isEqualTo("KET_QUA_PB");
            assertThat(feedbackCaptor.getValue().getTrangThai()).isEqualTo("CHO_XU_LY");
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.PNCKH_YEU_CAU_SUA_TM, NCKH_ID);
        }

        @Test
        @DisplayName("Thanh cong: TU_CHOI goi event PNCKH_TU_CHOI")
        void rejectsReviewResult() {
            ToPhanBien team = stubReviewDecision(TopicState.DANG_PHAN_BIEN);
            when(fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_TU_CHOI, NCKH_ID))
                    .thenReturn(mockDeTai);
            stubMappedResponse();

            service.xetDuyetPB(DE_TAI_ID, xetDuyetRequest("TU_CHOI"), NCKH_ID);

            assertThat(team.getKetQuaTongHop()).isEqualTo("TU_CHOI");
            verify(toPhanBienRepository).save(team);
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.PNCKH_TU_CHOI, NCKH_ID);
        }

        @Test
        @DisplayName("Loi: trang thai DRAFT bi chan truoc khi goi FSM")
        void rejectsWrongTopicStateBeforeFsm() {
            stubUser(mockNckh);
            stubTopic(TopicState.DRAFT);

            assertThrows(
                    BusinessException.class,
                    () -> service.xetDuyetPB(
                            DE_TAI_ID,
                            xetDuyetRequest("CHAP_NHAN"),
                            NCKH_ID));

            verify(toPhanBienRepository, never()).findByDeTaiId(any());
            verify(fsmService, never()).transition(any(), any(), any());
        }

        @Test
        @DisplayName("Loi FSM: FSM_INVALID_TRANSITION duoc propagate sau khi luu ket qua tong hop")
        void propagatesFsmErrorAfterSavingDecision() {
            ToPhanBien team = stubReviewDecision(TopicState.DANG_PHAN_BIEN);
            BusinessException fsmError = new BusinessException("FSM_INVALID_TRANSITION", "Transition sai");
            when(fsmService.transition(DE_TAI_ID, TopicEvent.PNCKH_ACCEPT_PB, NCKH_ID))
                    .thenThrow(fsmError);

            BusinessException thrown = assertThrows(
                    BusinessException.class,
                    () -> service.xetDuyetPB(
                            DE_TAI_ID,
                            xetDuyetRequest("CHAP_NHAN"),
                            NCKH_ID));

            assertThat(thrown).isSameAs(fsmError);
            verify(toPhanBienRepository).save(team);
            verify(emailService, never()).guiEmail(any(), any(), any(), anyMap());
            verify(deTaiMapper, never()).toResponse(any(DeTai.class));
        }
    }

    @Nested
    @DisplayName("8. gvNopBoSung()")
    class GvNopBoSungTests {

        @Test
        @DisplayName("Thanh cong: xu ly feedback trong deadline va goi event GV_NOP_BO_SUNG")
        void submitsSupplementBeforeDeadline() {
            Feedback feedback = openSupplementFeedback(LocalDateTime.now().plusDays(1));
            stubUser(mockGv);
            stubTopic(TopicState.CHO_BO_SUNG_HO_SO);
            when(feedbackRepository.findFirstByDeTaiIdAndLoaiAndTrangThaiOrderByCreatedAtDesc(
                    DE_TAI_ID, "BO_SUNG_HO_SO", "CHO_XU_LY"))
                    .thenReturn(Optional.of(feedback));
            when(fsmService.transition(DE_TAI_ID, TopicEvent.GV_NOP_BO_SUNG, GV_ID))
                    .thenReturn(mockDeTai);
            stubMappedResponse();

            service.gvNopBoSung(DE_TAI_ID, GV_ID);

            assertThat(feedback.getTrangThai()).isEqualTo("DA_XU_LY");
            verify(feedbackRepository).save(feedback);
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.GV_NOP_BO_SUNG, GV_ID);
        }

        @Test
        @DisplayName("Loi: qua deadline nop bo sung nen khong goi FSM")
        void rejectsSupplementAfterDeadline() {
            Feedback feedback = openSupplementFeedback(LocalDateTime.now().minusDays(1));
            stubUser(mockGv);
            stubTopic(TopicState.CHO_BO_SUNG_HO_SO);
            when(feedbackRepository.findFirstByDeTaiIdAndLoaiAndTrangThaiOrderByCreatedAtDesc(
                    DE_TAI_ID, "BO_SUNG_HO_SO", "CHO_XU_LY"))
                    .thenReturn(Optional.of(feedback));

            BusinessException thrown = assertThrows(
                    BusinessException.class,
                    () -> service.gvNopBoSung(DE_TAI_ID, GV_ID));

            assertThat(thrown.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(feedbackRepository, never()).save(any(Feedback.class));
            verify(fsmService, never()).transition(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("9. phanHoiHopDong()")
    class PhanHoiHopDongTests {

        @Test
        @DisplayName("Thanh cong: GV dong y chuyen hop dong sang CHO_KY")
        void acceptsContractDraft() {
            HopDong contract = new HopDong();
            contract.setTrangThaiHopDong("CHO_PHAN_HOI");
            PhanHoiHopDongRequest request = new PhanHoiHopDongRequest();
            request.setDongY(true);
            stubUser(mockGv);
            stubTopic(TopicState.DANG_LAP_HOP_DONG);
            when(hopDongRepository.findByDeTaiId(DE_TAI_ID)).thenReturn(Optional.of(contract));
            stubMappedResponse();

            DeTaiResponse result = service.phanHoiHopDong(DE_TAI_ID, request, GV_ID);

            assertThat(contract.getTrangThaiHopDong()).isEqualTo("CHO_KY");
            assertThat(result).isSameAs(mappedResponse);
            verify(hopDongRepository).save(contract);
            verify(auditLogRepository).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Loi: de tai khong o trang thai DANG_LAP_HOP_DONG")
        void rejectsWrongTopicState() {
            PhanHoiHopDongRequest request = new PhanHoiHopDongRequest();
            request.setDongY(true);
            stubUser(mockGv);
            stubTopic(TopicState.DRAFT);

            assertThrows(
                    BusinessException.class,
                    () -> service.phanHoiHopDong(DE_TAI_ID, request, GV_ID));

            verify(hopDongRepository, never()).findByDeTaiId(any());
            verify(hopDongRepository, never()).save(any(HopDong.class));
        }
    }

    @Nested
    @DisplayName("10. autoTreoDeTai()")
    class AutoTreoDeTaiTests {

        @Test
        @DisplayName("Thanh cong: scheduler goi truc tiep event HE_THONG_TREO")
        void delegatesSystemSuspensionToFsm() {
            when(fsmService.transition(DE_TAI_ID, TopicEvent.HE_THONG_TREO, NCKH_ID))
                    .thenReturn(mockDeTai);

            DeTai result = service.autoTreoDeTai(DE_TAI_ID, NCKH_ID);

            assertThat(result).isSameAs(mockDeTai);
            verify(fsmService).transition(DE_TAI_ID, TopicEvent.HE_THONG_TREO, NCKH_ID);
        }
    }

    private TaoDeTaiRequest taoDeTaiRequest() {
        TaoDeTaiRequest request = new TaoDeTaiRequest();
        request.setTenDeTai("De tai kiem thu");
        request.setMoTa("Mo ta");
        request.setLinhVuc("CNTT");
        request.setKyNckhId(KY_NCKH_ID);
        return request;
    }

    private LapToPhanBienRequest lapToPhanBienRequest(List<Long> memberIds) {
        LapToPhanBienRequest request = new LapToPhanBienRequest();
        request.setThanhVienIds(memberIds);
        request.setDeadlineNop(LocalDate.now().plusDays(7));
        return request;
    }

    private XetDuyetPBRequest xetDuyetRequest(String decision) {
        XetDuyetPBRequest request = new XetDuyetPBRequest();
        request.setQuyetDinh(decision);
        return request;
    }

    private Feedback openSupplementFeedback(LocalDateTime deadline) {
        Feedback feedback = new Feedback();
        feedback.setDeTaiId(DE_TAI_ID);
        feedback.setLoai("BO_SUNG_HO_SO");
        feedback.setNoiDung("Bo sung tai lieu");
        feedback.setDeadlinePhanHoi(deadline);
        feedback.setTrangThai("CHO_XU_LY");
        return feedback;
    }

    private ToPhanBien stubReviewDecision(TopicState state) {
        ToPhanBien team = new ToPhanBien();
        team.setDeTai(mockDeTai);
        stubUser(mockNckh);
        stubTopic(state);
        when(toPhanBienRepository.findByDeTaiId(DE_TAI_ID)).thenReturn(Optional.of(team));
        return team;
    }

    private void stubUser(NguoiDung user) {
        when(nguoiDungRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    private void stubTopic(TopicState state) {
        mockDeTai.setStatus(state);
        when(deTaiRepository.findById(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));
    }

    private void stubMappedResponse() {
        when(deTaiMapper.toResponse(any(DeTai.class))).thenReturn(mappedResponse);
    }

    private CustomUserDetails currentUser(Long id, String role) {
        return new CustomUserDetails(id, "user" + id, "password", role, true, false);
    }

    private NguoiDung user(Long id, String role, String email) {
        NguoiDung user = new NguoiDung();
        user.setId(id);
        user.setVaiTro(role);
        user.setHoTen("User " + id);
        user.setEmail(email);
        return user;
    }
}
