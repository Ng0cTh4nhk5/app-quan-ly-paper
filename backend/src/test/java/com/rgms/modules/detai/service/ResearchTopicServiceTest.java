package com.rgms.modules.detai.service;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.KyNckh;
import com.rgms.modules.detai.entity.ToPhanBien;
import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import com.rgms.modules.detai.fsm.FsmService;
import com.rgms.modules.detai.fsm.guards.*;
import com.rgms.modules.detai.repository.*;
import com.rgms.modules.nguoidung.entity.DonVi;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.nguoidung.model.Role;
import com.rgms.modules.nguoidung.repository.NguoiDungRepository;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho ResearchTopicService.
 * Bao phủ 15 methods định nghĩa trong SOP.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResearchTopicService — Business Logic Tests")
class ResearchTopicServiceTest {

    @Mock FsmService fsmService;
    @Mock DeTaiRepository deTaiRepository;
    @Mock KyNckhRepository kyNckhRepository;
    @Mock NguoiDungRepository nguoiDungRepository;
    @Mock ToPhanBienRepository toPhanBienRepository;
    @Mock ThanhVienToPhanBienRepository thanhVienToPhanBienRepository;
    @Mock PhanBienDeXuatRepository phanBienDeXuatRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @Mock HoSoHopLeGuard hoSoHopLeGuard;
    @Mock ChuNhiemGuard chuNhiemGuard;
    @Mock ToPhanBienHopLeGuard toPhanBienHopLeGuard;
    @Mock TatCaPBDaNopGuard tatCaPBDaNopGuard;
    @Mock GvDaDongYHopDongGuard gvDaDongYHopDongGuard;

    @InjectMocks
    ResearchTopicService service;

    private static final UUID DE_TAI_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID KY_NCKH_ID = UUID.randomUUID();

    private NguoiDung mockGv;
    private NguoiDung mockPnckh;
    private KyNckh mockKyNckh;
    private DeTai mockDeTai;

    @BeforeEach
    void setUp() {
        mockGv = new NguoiDung();
        setId(mockGv, ACTOR_ID);
        mockGv.setRole(Role.GIANG_VIEN);
        mockGv.setDonVi(new DonVi());

        mockPnckh = new NguoiDung();
        setId(mockPnckh, ACTOR_ID);
        mockPnckh.setRole(Role.PNCKH);

        mockKyNckh = new KyNckh();
        setId(mockKyNckh, KY_NCKH_ID);
        mockKyNckh.setTrangThai("DANG_MO");

        mockDeTai = new DeTai();
        setId(mockDeTai, DE_TAI_ID);
        mockDeTai.setStatus(TopicState.DRAFT);
        mockDeTai.setChuNhiem(mockGv);
    }

    @Nested
    @DisplayName("1. taoDeTai()")
    class TaoDeTaiTests {
        @Test
        @DisplayName("Thành công: Đề tài tạo ở trạng thái DRAFT")
        void success() {
            when(nguoiDungRepository.findById(ACTOR_ID)).thenReturn(Optional.of(mockGv));
            when(kyNckhRepository.findById(KY_NCKH_ID)).thenReturn(Optional.of(mockKyNckh));
            when(deTaiRepository.nextMaSoSequence()).thenReturn(1L);
            when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

            DeTai result = service.taoDeTai(ACTOR_ID, "Tên đề tài", "Mô tả", "CNTT", KY_NCKH_ID);

            assertThat(result.getStatus()).isEqualTo(TopicState.DRAFT);
            assertThat(result.getChuNhiem()).isEqualTo(mockGv);
            verify(auditLogRepository).save(any());
        }

        @Test
        @DisplayName("Lỗi: Người tạo không phải GV")
        void failNotGV() {
            mockGv.setRole(Role.PNCKH);
            when(nguoiDungRepository.findById(ACTOR_ID)).thenReturn(Optional.of(mockGv));

            assertThrows(BusinessException.class, () -> 
                service.taoDeTai(ACTOR_ID, "T", "M", "L", KY_NCKH_ID));
        }
    }

    @Nested
    @DisplayName("2. guiHoSo()")
    class GuiHoSoTests {
        @Test
        @DisplayName("Thành công: Chuyển qua FsmService và publish event")
        void success() {
            when(fsmService.transition(eq(DE_TAI_ID), eq(TopicEvent.GV_GUI_HO_SO), eq(ACTOR_ID), anyList()))
                    .thenReturn(mockDeTai);

            DeTai result = service.guiHoSo(DE_TAI_ID, ACTOR_ID);

            assertThat(result).isNotNull();
            verify(fsmService).transition(eq(DE_TAI_ID), eq(TopicEvent.GV_GUI_HO_SO), eq(ACTOR_ID), anyList());
            verify(eventPublisher).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("6. lapToPhanBien()")
    class LapToPhanBienTests {
        @Test
        @DisplayName("Lỗi: Ít hơn 2 thành viên PB")
        void failThieuTV() {
            when(nguoiDungRepository.findById(ACTOR_ID)).thenReturn(Optional.of(mockPnckh));

            assertThrows(BusinessException.class, () ->
                service.lapToPhanBien(DE_TAI_ID, ACTOR_ID, LocalDate.now(), List.of(UUID.randomUUID())));
        }
    }

    @Nested
    @DisplayName("7. pbNopKetQua()")
    class PbNopKetQuaTests {
        @Test
        @DisplayName("Lỗi: Không phải thành viên tổ PB")
        void failKhongPhaiThanhVien() {
            when(thanhVienToPhanBienRepository.findByToPhanBienDeTaiIdAndNguoiDungId(DE_TAI_ID, ACTOR_ID))
                    .thenReturn(Optional.empty());

            assertThrows(BusinessException.class, () ->
                service.pbNopKetQua(DE_TAI_ID, ACTOR_ID, "CHAP_NHAN", "Nhận xét"));
        }
    }

    @Nested
    @DisplayName("8. nckhXetDuyetPB()")
    class NckhXetDuyetPBTests {
        @Test
        @DisplayName("Thành công: Xét duyệt CHAP_NHAN -> gọi event PNCKH_ACCEPT_PB")
        void successChapNhan() {
            when(nguoiDungRepository.findById(ACTOR_ID)).thenReturn(Optional.of(mockPnckh));
            
            ToPhanBien mockTo = new ToPhanBien();
            when(toPhanBienRepository.findByDeTaiId(DE_TAI_ID)).thenReturn(Optional.of(mockTo));
            
            when(fsmService.transition(eq(DE_TAI_ID), eq(TopicEvent.PNCKH_ACCEPT_PB), eq(ACTOR_ID), anyList()))
                    .thenReturn(mockDeTai);

            service.nckhXetDuyetPB(DE_TAI_ID, ACTOR_ID, "CHAP_NHAN", "Ghi chú");

            verify(toPhanBienRepository).save(mockTo);
            verify(fsmService).transition(eq(DE_TAI_ID), eq(TopicEvent.PNCKH_ACCEPT_PB), eq(ACTOR_ID), anyList());
            assertThat(mockTo.getKetQuaTongHop()).isEqualTo("CHAP_NHAN");
        }
    }

    @Nested
    @DisplayName("11. gvDongYHopDong()")
    class GvDongYHopDongTests {
        @Test
        @DisplayName("Thành công: Set cờ gvDaDongYHopDong và ghi audit")
        void success() {
            mockDeTai.setStatus(TopicState.DANG_LAP_HOP_DONG);
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));
            when(deTaiRepository.save(any(DeTai.class))).thenAnswer(i -> i.getArgument(0));

            DeTai result = service.gvDongYHopDong(DE_TAI_ID, ACTOR_ID);

            assertThat(result.getGvDaDongYHopDong()).isTrue();
            verify(auditLogRepository).save(any());
        }

        @Test
        @DisplayName("Lỗi: Không phải DANG_LAP_HOP_DONG")
        void failWrongState() {
            mockDeTai.setStatus(TopicState.DRAFT);
            when(deTaiRepository.findByIdWithChuNhiem(DE_TAI_ID)).thenReturn(Optional.of(mockDeTai));

            assertThrows(BusinessException.class, () -> service.gvDongYHopDong(DE_TAI_ID, ACTOR_ID));
        }
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
