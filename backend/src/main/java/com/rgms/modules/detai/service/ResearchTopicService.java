package com.rgms.modules.detai.service;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.*;
import com.rgms.modules.detai.event.*;
import com.rgms.modules.detai.fsm.FsmService;
import com.rgms.modules.detai.fsm.guards.*;
import com.rgms.modules.detai.repository.*;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.nguoidung.model.Role;
import com.rgms.modules.nguoidung.repository.NguoiDungRepository;
import com.rgms.shared.enums.TopicEvent;
import com.rgms.shared.enums.TopicState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * ResearchTopicService — Nghiệp vụ đề tài NCKH (Phase 1 — Luồng 1).
 *
 * QUY TẮC BẮT BUỘC: Mọi thay đổi DeTai.status PHẢI qua fsmService.transition().
 *
 * Danh sách method theo sop-member-a.md Giai đoạn 5:
 *  1.  taoDeTai()           — GV tạo mới (GUARD: role, kyDangMo)
 *  2.  guiHoSo()            — GV gửi hồ sơ (GUARD: HoSoHopLeGuard)
 *  3.  tiepNhan()           — PNCKH tiếp nhận
 *  4.  yeuCauBoSung()       — PNCKH yêu cầu bổ sung
 *  5.  gvNopBoSung()        — GV nộp lại hồ sơ
 *  6.  lapToPhanBien()      — PNCKH lập tổ PB (GUARD: >=2 thành viên)
 *  7.  pbNopKetQua()        — PB thành viên nộp kết quả (không đổi state)
 *  8.  nckhXetDuyetPB()     — PNCKH xét duyệt KQ PB (GUARD: tất cả PB đã nộp)
 *  9.  gvDongYHopDong()     — GV đồng ý HĐ (set flag, không đổi state)
 *  10. soanhHopDong()       — PNCKH soạn HĐ (không đổi state DeTai)
 *  11. kyHopDong()          — PNCKH ký HĐ (GUARD: GV đã đồng ý)
 *  12. gvNopSuaThuyetMinh() — GV nộp lại TM sau phản biện yêu cầu sửa
 *  13. tuChoiHoSo()         — PNCKH từ chối thẳng
 *  14. gvRutDeTai()         — GV chủ động rút
 *  15. autoTreoDeTai()      — Scheduler gọi khi quá hạn
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ResearchTopicService {

    private final FsmService fsmService;
    private final DeTaiRepository deTaiRepository;
    private final KyNckhRepository kyNckhRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final ToPhanBienRepository toPhanBienRepository;
    private final ThanhVienToPhanBienRepository thanhVienToPhanBienRepository;
    private final PhanBienDeXuatRepository phanBienDeXuatRepository;
    private final AuditLogRepository auditLogRepository;

    private final HoSoHopLeGuard hoSoHopLeGuard;
    private final ChuNhiemGuard chuNhiemGuard;
    private final ToPhanBienHopLeGuard toPhanBienHopLeGuard;
    private final TatCaPBDaNopGuard tatCaPBDaNopGuard;
    private final GvDaDongYHopDongGuard gvDaDongYHopDongGuard;

    private final ApplicationEventPublisher eventPublisher;

    // ──────────────────────────────────────────────────────────────────────────
    // 1. TẠO ĐỀ TÀI MỚI (F-GV-01)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * GV tạo đề tài mới → trạng thái DRAFT.
     *
     * Guards:
     *   [1] user.role == GIANG_VIEN
     *   [2] tenDeTai không rỗng — validate ở DTO (@NotBlank)
     *   [3] KyNCKH.trangThai == DANG_MO
     *
     * Side-effects:
     *   - Sinh maSo bằng PostgreSQL SEQUENCE (race-condition-safe — fix H-1)
     *   - don_vi_id = user.don_vi_id  (spec dòng 61)
     *   - Ghi AuditLog: TAO_DE_TAI
     */
    public DeTai taoDeTai(UUID gvId, String tenDeTai, String moTa,
                           String linhVuc, UUID kyNckhId) {

        // [1] Load GV & kiểm tra role
        NguoiDung gv = nguoiDungRepository.findById(gvId)
                .orElseThrow(() -> new BusinessException("TAO_DE_TAI_GV_NOT_FOUND",
                        "Không tìm thấy người dùng."));
        if (gv.getRole() != Role.GIANG_VIEN) {
            throw new BusinessException("TAO_DE_TAI_WRONG_ROLE",
                    "Vai trò của bạn không có quyền tạo đề tài. Chỉ Giảng viên mới được tạo.");
        }
        if (gv.getDonVi() == null) {
            throw new BusinessException("TAO_DE_TAI_NO_DON_VI",
                    "Tài khoản chưa được gắn đơn vị. Liên hệ Admin để cập nhật.");
        }

        // [3] Load KyNCKH & kiểm tra đang mở
        KyNckh kyNckh = kyNckhRepository.findById(kyNckhId)
                .orElseThrow(() -> new BusinessException("TAO_DE_TAI_KY_NOT_FOUND",
                        "Không tìm thấy kỳ NCKH."));
        if (!kyNckh.isDangMo()) {
            throw new BusinessException("TAO_DE_TAI_KY_DONG",
                    "Kỳ NCKH đã đóng đăng ký. Vui lòng chọn kỳ đang mở.");
        }

        // Sinh maSo an toàn bằng PostgreSQL SEQUENCE (BR — tính duy nhất + H-1 fix)
        long seq = deTaiRepository.nextMaSoSequence();
        String maSo = "NCKH-" + Year.now().getValue() + "-" + String.format("%04d", seq);

        // Tạo DeTai
        DeTai deTai = new DeTai();
        deTai.setMaSo(maSo);
        deTai.setTenDeTai(tenDeTai);
        deTai.setMoTa(moTa);
        deTai.setLinhVuc(linhVuc);
        deTai.setStatus(TopicState.DRAFT);
        deTai.setChuNhiem(gv);
        deTai.setDonVi(gv.getDonVi());   // Spec dòng 61: don_vi_id = user.don_vi_id
        deTai.setKyNckh(kyNckh);

        DeTai saved = deTaiRepository.save(deTai);

        // Ghi AuditLog (BR-15)
        auditLogRepository.save(AuditLog.builder()
                .deTai(saved)
                .actor(gv)
                .hanhDong("TAO_DE_TAI")
                .trangThaiTruoc(null)
                .trangThaiSau(TopicState.DRAFT.name())
                .build());

        log.info("[Service] Tạo đề tài maSo={} GV={} KyNCKH={}", maSo, gvId, kyNckhId);
        return saved;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. GV GỬI HỒ SƠ (T02: DRAFT → CHO_PNCKH_XEM_XET)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai guiHoSo(UUID deTaiId, UUID gvId) {
        DeTai result = fsmService.transition(
                deTaiId, TopicEvent.GV_GUI_HO_SO, gvId,
                List.of(hoSoHopLeGuard));
        eventPublisher.publishEvent(new HoSoGuiEvent(deTaiId, gvId));
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. PNCKH TIẾP NHẬN (T03: CHO_PNCKH_XEM_XET → DANG_XEM_XET_BOI_PNCKH)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai tiepNhan(UUID deTaiId, UUID nckhId) {
        validateRole(nckhId, Role.PNCKH, "tiepNhan");
        DeTai result = fsmService.transition(
                deTaiId, TopicEvent.PNCKH_TIEP_NHAN, nckhId, List.of());
        eventPublisher.publishEvent(new HoSoTiepNhanEvent(deTaiId, nckhId));
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4. PNCKH YÊU CẦU BỔ SUNG (T04: DANG_XEM_XET → CHO_BO_SUNG_HO_SO)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai yeuCauBoSung(UUID deTaiId, UUID nckhId,
                               String noiDungFeedback, LocalDate deadline) {
        validateRole(nckhId, Role.PNCKH, "yeuCauBoSung");
        // Feedback entity sẽ do Member B tạo trong FeedbackService
        return fsmService.transition(
                deTaiId, TopicEvent.PNCKH_YEU_CAU_BO_SUNG, nckhId, List.of());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5. GV NỘP LẠI HỒ SƠ (T05: CHO_BO_SUNG → DANG_XEM_XET)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai gvNopBoSung(UUID deTaiId, UUID gvId) {
        return fsmService.transition(
                deTaiId, TopicEvent.GV_NOP_BO_SUNG, gvId,
                List.of(chuNhiemGuard));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6. PNCKH LẬP TỔ PHẢN BIỆN (T06: DANG_XEM_XET → DANG_PHAN_BIEN)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai lapToPhanBien(UUID deTaiId, UUID nckhId,
                                LocalDate deadlineNop, List<UUID> danhSachPhanBienId) {
        validateRole(nckhId, Role.PNCKH, "lapToPhanBien");

        if (danhSachPhanBienId == null || danhSachPhanBienId.size() < 2) {
            throw new BusinessException("LAP_TO_PB_THIEU_THANH_VIEN",
                    "Tổ phản biện cần ít nhất 2 thành viên. Hiện được chọn: "
                    + (danhSachPhanBienId == null ? 0 : danhSachPhanBienId.size()));
        }

        DeTai deTai = deTaiRepository.findById(deTaiId)
                .orElseThrow(() -> new BusinessException("LAP_TO_PB_NOT_FOUND",
                        "Không tìm thấy đề tài."));

        // Tạo ToPhanBien và thêm thành viên
        ToPhanBien toPhanBien = new ToPhanBien();
        toPhanBien.setDeTai(deTai);
        toPhanBien.setDeadlineNop(deadlineNop);
        ToPhanBien savedToPB = toPhanBienRepository.save(toPhanBien);

        for (UUID pbId : danhSachPhanBienId) {
            NguoiDung pb = nguoiDungRepository.findById(pbId)
                    .orElseThrow(() -> new BusinessException("LAP_TO_PB_MEMBER_NOT_FOUND",
                            "Không tìm thấy phản biện id=" + pbId));
            ThanhVienToPhanBien tv = new ThanhVienToPhanBien();
            tv.setToPhanBien(savedToPB);
            tv.setNguoiDung(pb);
            thanhVienToPhanBienRepository.save(tv);
        }

        DeTai result = fsmService.transition(
                deTaiId, TopicEvent.PNCKH_LAP_TO_PB, nckhId,
                List.of(toPhanBienHopLeGuard));
        eventPublisher.publishEvent(new ToPhanBienLapEvent(deTaiId, nckhId));
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7. PB THÀNH VIÊN NỘP KẾT QUẢ (không đổi state DeTai)
    // ──────────────────────────────────────────────────────────────────────────

    public ThanhVienToPhanBien pbNopKetQua(UUID deTaiId, UUID pbId,
                                            String ketQua, String nhanXet) {
        ThanhVienToPhanBien tv = thanhVienToPhanBienRepository
                .findByToPhanBienDeTaiIdAndNguoiDungId(deTaiId, pbId)
                .orElseThrow(() -> new BusinessException("PB_NOP_NOT_MEMBER",
                        "Bạn không phải thành viên tổ phản biện của đề tài này."));

        if (tv.isDaNop()) {
            throw new BusinessException("PB_NOP_ALREADY",
                    "Bạn đã nộp kết quả phản biện. Không thể nộp lại.");
        }

        tv.setKetQua(ketQua);
        tv.setNhanXet(nhanXet);
        tv.setNgayNop(java.time.LocalDateTime.now());
        return thanhVienToPhanBienRepository.save(tv);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 8. PNCKH XÉT DUYỆT KẾT QUẢ PHẢN BIỆN
    //    CHAP_NHAN → DANG_LAP_HOP_DONG  (T09)
    //    CAN_SUA   → CHO_CHINH_SUA_TM   (T07)
    //    TU_CHOI   → BI_TU_CHOI         (E04)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai nckhXetDuyetPB(UUID deTaiId, UUID nckhId,
                                  String quyetDinh, String ghiChu) {
        validateRole(nckhId, Role.PNCKH, "nckhXetDuyetPB");

        // Cập nhật ketQuaTongHop ToPhanBien
        ToPhanBien toPB = toPhanBienRepository.findByDeTaiId(deTaiId)
                .orElseThrow(() -> new BusinessException("XET_DUYET_PB_NO_TOPB",
                        "Không tìm thấy tổ phản biện cho đề tài này."));
        toPB.setKetQuaTongHop(quyetDinh.toUpperCase());
        toPB.setGhiChuPnckh(ghiChu);
        toPhanBienRepository.save(toPB);

        TopicEvent event = switch (quyetDinh.toUpperCase()) {
            case "CHAP_NHAN" -> TopicEvent.PNCKH_ACCEPT_PB;
            case "CAN_SUA"   -> TopicEvent.PNCKH_YEU_CAU_SUA_TM;
            case "TU_CHOI"   -> TopicEvent.PNCKH_TU_CHOI;
            default -> throw new BusinessException("XET_DUYET_PB_INVALID",
                    "Quyết định không hợp lệ. Phải là: CHAP_NHAN | CAN_SUA | TU_CHOI.");
        };

        return fsmService.transition(
                deTaiId, event, nckhId,
                List.of(tatCaPBDaNopGuard)); // tất cả case đều cần check đã nộp
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 9. GV NỘP LẠI THUYẾT MINH (T08: CHO_CHINH_SUA_TM → DANG_PHAN_BIEN)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai gvNopSuaThuyetMinh(UUID deTaiId, UUID gvId) {
        return fsmService.transition(
                deTaiId, TopicEvent.GV_NOP_SUA_TM, gvId,
                List.of(chuNhiemGuard));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 10. PNCKH SOẠN HĐ (không đổi state DeTai — chỉ tạo HopDong entity)
    //     Member B implement HopDongService; method này chỉ ghi AuditLog
    // ──────────────────────────────────────────────────────────────────────────

    public void soanhHopDong(UUID deTaiId, UUID nckhId) {
        validateRole(nckhId, Role.PNCKH, "soanhHopDong");
        DeTai deTai = deTaiRepository.findById(deTaiId)
                .orElseThrow(() -> new BusinessException("SOAN_HD_NOT_FOUND",
                        "Không tìm thấy đề tài."));

        if (deTai.getStatus() != TopicState.DANG_LAP_HOP_DONG) {
            throw new BusinessException("SOAN_HD_WRONG_STATUS",
                    "Chỉ được soạn hợp đồng khi đề tài ở DANG_LAP_HOP_DONG.");
        }

        NguoiDung nckh = nguoiDungRepository.findById(nckhId).orElseThrow();
        auditLogRepository.save(AuditLog.builder()
                .deTai(deTai).actor(nckh)
                .hanhDong("PNCKH_SOAN_HOP_DONG")
                .trangThaiTruoc(TopicState.DANG_LAP_HOP_DONG.name())
                .trangThaiSau(TopicState.DANG_LAP_HOP_DONG.name())
                .build());
        // HopDong entity do Member B tạo trong HopDongService
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 11. GV ĐỒNG Ý HĐ (không đổi state — set flag gvDaDongYHopDong)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai gvDongYHopDong(UUID deTaiId, UUID gvId) {
        DeTai deTai = deTaiRepository.findByIdWithChuNhiem(deTaiId)
                .orElseThrow(() -> new BusinessException("GV_DONG_Y_NOT_FOUND",
                        "Không tìm thấy đề tài."));

        // IDOR
        if (!deTai.getChuNhiem().getId().equals(gvId)) {
            throw new BusinessException("GV_DONG_Y_IDOR",
                    "Bạn không có quyền xác nhận hợp đồng của đề tài này.");
        }
        if (deTai.getStatus() != TopicState.DANG_LAP_HOP_DONG) {
            throw new BusinessException("GV_DONG_Y_WRONG_STATUS",
                    "Chỉ được đồng ý hợp đồng khi đề tài ở DANG_LAP_HOP_DONG.");
        }

        deTai.setGvDaDongYHopDong(true);

        // Ghi AuditLog thủ công (không qua FSM — state không đổi)
        NguoiDung gv = deTai.getChuNhiem();
        auditLogRepository.save(AuditLog.builder()
                .deTai(deTai).actor(gv)
                .hanhDong("GV_DONG_Y_HOP_DONG")
                .trangThaiTruoc(TopicState.DANG_LAP_HOP_DONG.name())
                .trangThaiSau(TopicState.DANG_LAP_HOP_DONG.name())
                .build());

        return deTaiRepository.save(deTai);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 12. KÝ HỢP ĐỒNG (T10: DANG_LAP_HOP_DONG → DANG_THUC_HIEN)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai kyHopDong(UUID deTaiId, UUID nckhId) {
        validateRole(nckhId, Role.PNCKH, "kyHopDong");
        DeTai result = fsmService.transition(
                deTaiId, TopicEvent.HAI_BEN_KY_HD, nckhId,
                List.of(gvDaDongYHopDongGuard));
        eventPublisher.publishEvent(new HopDongKyEvent(deTaiId, nckhId));
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 13. PNCKH TỪ CHỐI THẲNG HỒ SƠ (E03)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai tuChoiHoSo(UUID deTaiId, UUID nckhId) {
        validateRole(nckhId, Role.PNCKH, "tuChoiHoSo");
        return fsmService.transition(
                deTaiId, TopicEvent.PNCKH_TU_CHOI, nckhId, List.of());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 14. GV RÚT ĐỀ TÀI (E21)
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai gvRutDeTai(UUID deTaiId, UUID gvId) {
        return fsmService.transition(
                deTaiId, TopicEvent.GV_RUT, gvId,
                List.of(chuNhiemGuard));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 15. SCHEDULER: AUTO TREO (E1, E2, E6, E8)
    //     Gọi bởi DeTaiScheduler — không phải user action
    // ──────────────────────────────────────────────────────────────────────────

    public DeTai autoTreoDeTai(UUID deTaiId, UUID systemActorId) {
        return fsmService.transition(
                deTaiId, TopicEvent.HE_THONG_TREO, systemActorId, List.of());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateRole(UUID userId, Role expectedRole, String op) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("VALIDATE_ROLE_NOT_FOUND",
                        "Không tìm thấy người dùng."));
        if (user.getRole() != expectedRole) {
            throw new BusinessException("VALIDATE_ROLE_WRONG",
                    "Thao tác [" + op + "] yêu cầu vai trò " + expectedRole
                    + ". Vai trò hiện tại: " + user.getRole());
        }
    }
}
