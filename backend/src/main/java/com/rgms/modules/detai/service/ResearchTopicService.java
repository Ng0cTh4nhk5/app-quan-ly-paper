package com.rgms.modules.detai.service;

import com.rgms.exception.BusinessException;
import com.rgms.exception.ResourceNotFoundException;
import com.rgms.modules.detai.dto.*;
import com.rgms.modules.detai.entity.*;
import com.rgms.modules.detai.fsm.FsmService;
import com.rgms.modules.detai.mapper.DeTaiMapper;
import com.rgms.modules.detai.repo.*;
import com.rgms.modules.email.EmailService;
import com.rgms.modules.files.mapper.TaiLieuMapper;
import com.rgms.modules.files.repo.TaiLieuRepository;
import com.rgms.modules.files.service.FileUploadService;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.detai.repo.DetaiNguoiDungRepository;
import com.rgms.shared.enums.TopicEvent;
import com.rgms.shared.enums.TopicState;
import com.rgms.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/** ResearchTopicService — Nghiệp vụ đề tài NCKH (Phase 1).
 *  BR-15: Mọi thay đổi status PHẢI đi qua fsmService.transition(). */
@Slf4j @Service @Transactional @RequiredArgsConstructor
public class ResearchTopicService {

    private static final String ROLE_GV  = "GIANG_VIEN";
    private static final String ROLE_NCK = "NCKH";
    private static final String ROLE_TPB = "TO_PHAN_BIEN";
    private static final String LOAI_TM  = "THUYET_MINH";
    private static final String FB_BOSUN = "BO_SUNG_HO_SO";
    private static final String FB_KQPB  = "KET_QUA_PB";
    private static final String FB_HD    = "HOP_DONG";
    private static final String FB_CHO_XL = "CHO_XU_LY";
    private static final String FB_DA_XL  = "DA_XU_LY";
    private static final String KQ_CHUA_CO  = "CHUA_CO";
    private static final String KQ_CHUA_NOP = "CHUA_NOP";
    private static final String KQ_CHAP  = "CHAP_NHAN";
    private static final String KQ_CAN_SUA = "CAN_SUA";
    private static final String KQ_TU_CHOI = "TU_CHOI";
    private static final String QD_YEU_SUA = "YEU_CAU_SUA";
    private static final String HD_CHO_PH  = "CHO_PHAN_HOI";
    private static final String HD_CHO_KY  = "CHO_KY";
    private static final String HD_DA_KY   = "DA_KY";
    private static final String HD_YEU_SUA = "YEU_CAU_SUA";

    private final FsmService                    fsmService;
    private final DeTaiRepository               deTaiRepository;
    private final KyNCKHRepository              kyNckhRepository;
    private final DetaiNguoiDungRepository      nguoiDungRepository;
    private final TaiLieuRepository             taiLieuRepository;
    private final AuditLogRepository            auditLogRepository;
    private final PhanBienDeXuatRepository      phanBienDeXuatRepository;
    private final FeedbackRepository            feedbackRepository;
    private final ToPhanBienRepository          toPhanBienRepository;
    private final ThanhVienToPhanBienRepository thanhVienToPhanBienRepository;
    private final HopDongRepository             hopDongRepository;
    private final DeTaiMapper                   deTaiMapper;
    private final TaiLieuMapper                 taiLieuMapper;
    private final FileUploadService             fileUploadService;
    private final EmailService                  emailService;

    // 1. TẠO ĐỀ TÀI → DRAFT
    public DeTaiResponse taoDeTai(TaoDeTaiRequest req, Long gvId) {
        NguoiDung gv = loadUser(gvId);
        requireRole(gv, ROLE_GV);
        KyNCKH ky = kyNckhRepository.findById(req.getKyNckhId())
                .orElseThrow(() -> new BusinessException("Kỳ NCKH không tồn tại.", HttpStatus.BAD_REQUEST));
        if (!"DANG_MO".equals(ky.getTrangThai()))
            throw new BusinessException("Kỳ NCKH đã đóng.", HttpStatus.BAD_REQUEST);
        DeTai dt = new DeTai();
        dt.setMaSo(generateMaSo()); dt.setTenDeTai(req.getTenDeTai().trim());
        dt.setMoTa(req.getMoTa()); dt.setLinhVuc(req.getLinhVuc());
        dt.setStatus(TopicState.DRAFT); dt.setChuNhiem(gv);
        dt.setKyNckh(ky); dt.setDonVi(gv.getDonVi());
        DeTai saved = deTaiRepository.save(dt);
        recordAudit(saved, "TAO_DE_TAI", gvId, null, TopicState.DRAFT.name(), "Khởi tạo nháp");
        return deTaiMapper.toResponse(saved);
    }

    // 2. DANH SÁCH
    @Transactional(readOnly = true)
    public PageDeTaiResponse danhSach(String trangThai, Pageable pg, CustomUserDetails cur) {
        TopicState statusFilter = null;
        if (StringUtils.hasText(trangThai)) {
            try {
                statusFilter = TopicState.valueOf(trangThai.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Trạng thái không hợp lệ: " + trangThai, HttpStatus.BAD_REQUEST);
            }
        }
        Page<DeTai> page = ROLE_GV.equals(cur.getRole())
                ? (statusFilter == null
                        ? deTaiRepository.findByChuNhiem_Id(cur.getId(), pg)
                        : deTaiRepository.findByChuNhiem_IdAndStatus(cur.getId(), statusFilter, pg))
                : (statusFilter == null
                        ? deTaiRepository.findAll(pg)
                        : deTaiRepository.findByStatus(statusFilter, pg));
        return PageDeTaiResponse.builder()
                .content(page.getContent().stream().map(deTaiMapper::toResponse).toList())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .page(page.getNumber()).build();
    }

    // 3. CHI TIẾT
    @Transactional(readOnly = true)
    public DeTaiDetailResponse layChiTiet(Long id, CustomUserDetails cur) {
        DeTai dt = loadDeTai(id);
        if (ROLE_GV.equals(cur.getRole()) && !dt.getChuNhiem().getId().equals(cur.getId()))
            throw new AccessDeniedException("Bạn không có quyền xem đề tài này.");
        var tl = taiLieuRepository.findByDeTaiIdOrderByUploadedAtDesc(id)
                .stream().map(taiLieuMapper::toResponse).toList();
        var logs = auditLogRepository.findByDeTai_IdOrderByThoiGianAsc(id);
        var entries = logs.stream().map(deTaiMapper::toAuditLogEntry).toList();
        return deTaiMapper.toDetailResponse(dt, tl, entries);
    }

    // 4. THÊM PHẢN BIỆN ĐỀ XUẤT
    public PhanBienDeXuatResponse themPhanBienDeXuat(Long id, PhanBienDeXuatRequest req, Long gvId) {
        requireRole(loadUser(gvId), ROLE_GV);
        DeTai dt = loadDeTai(id); requireOwner(dt, gvId); requireState(dt, TopicState.DRAFT);
        PhanBienDeXuat pb = new PhanBienDeXuat();
        pb.setDeTai(dt); pb.setHoTen(req.getHoTen().trim());
        pb.setEmail(trimToNull(req.getEmail())); pb.setCoQuan(trimToNull(req.getCoQuan()));
        PhanBienDeXuat sv = phanBienDeXuatRepository.save(pb);
        recordAudit(dt, "THEM_PHAN_BIEN_DE_XUAT", gvId, null, null, "Thêm PB: " + sv.getHoTen());
        return toPhanBienDeXuatResponse(sv);
    }

    // 5. GỬI HỒ SƠ [FSM: GV_GUI_HO_SO]
    public DeTaiResponse guiHoSo(Long id, Long gvId) {
        requireRole(loadUser(gvId), ROLE_GV);
        DeTai dt = loadDeTai(id); requireOwner(dt, gvId); requireState(dt, TopicState.DRAFT);
        if (!taiLieuRepository.existsByDeTaiIdAndLoaiFile(id, LOAI_TM))
            throw new BusinessException("Hồ sơ phải có file thuyết minh.", HttpStatus.BAD_REQUEST);
        if (phanBienDeXuatRepository.countByDeTaiId(id) < 1)
            throw new BusinessException("Vui lòng đề xuất ít nhất một phản biện.", HttpStatus.BAD_REQUEST);
        DeTai saved = fsmService.transition(id, TopicEvent.GV_GUI_HO_SO, gvId);
        notifyNckh("RGMS - Hồ sơ mới chờ xem xét", saved, "Có hồ sơ đề tài mới.");
        return deTaiMapper.toResponse(saved);
    }

    // 6. TIẾP NHẬN [FSM: PNCKH_TIEP_NHAN]
    public DeTaiResponse tiepNhan(Long id, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        requireState(loadDeTai(id), TopicState.CHO_PNCKH_XEM_XET);
        DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_TIEP_NHAN, nckhId);
        notifyOwner(saved, "RGMS - Hồ sơ đã được tiếp nhận", "Hồ sơ đề tài đã được tiếp nhận để sơ thẩm.");
        return deTaiMapper.toResponse(saved);
    }

    // 7. YÊU CẦU BỔ SUNG [FSM: PNCKH_YEU_CAU_BO_SUNG]
    public DeTaiResponse yeuCauBoSung(Long id, YeuCauBoSungRequest req, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        DeTai dt = loadDeTai(id); requireState(dt, TopicState.DANG_XEM_XET_BOI_PNCKH);
        Feedback fb = new Feedback();
        fb.setDeTaiId(dt.getId()); fb.setLoai(FB_BOSUN); fb.setNoiDung(req.getNoiDung().trim());
        fb.setDeadlinePhanHoi(toEndOfDay(req.getDeadlinePhanHoi()));
        fb.setTrangThai(FB_CHO_XL); fb.setNguoiTaoId(nckhId);
        feedbackRepository.save(fb);
        DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_YEU_CAU_BO_SUNG, nckhId);
        notifyOwner(saved, "RGMS - Yêu cầu bổ sung hồ sơ", "Hạn: " + req.getDeadlinePhanHoi() + ".");
        return deTaiMapper.toResponse(saved);
    }

    // 8. GV NỘP BỔ SUNG [FSM: GV_NOP_BO_SUNG]
    public DeTaiResponse gvNopBoSung(Long id, Long gvId) {
        requireRole(loadUser(gvId), ROLE_GV);
        DeTai dt = loadDeTai(id); requireOwner(dt, gvId); requireState(dt, TopicState.CHO_BO_SUNG_HO_SO);
        Feedback fb = loadOpenFeedback(id, FB_BOSUN);
        requireBeforeDeadline(fb.getDeadlinePhanHoi(), "Đã quá hạn nộp bổ sung.");
        fb.setTrangThai(FB_DA_XL); feedbackRepository.save(fb);
        DeTai saved = fsmService.transition(id, TopicEvent.GV_NOP_BO_SUNG, gvId);
        notifyNckh("RGMS - GV đã nộp bổ sung", saved, "GV đã nộp bổ sung hồ sơ.");
        return deTaiMapper.toResponse(saved);
    }
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 9. Tá»ª CHá»I Há»’ SÆ : DANG_XEM_XET â†’ BI_TU_CHOI  [FSM: PNCKH_TU_CHOI]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse tuChoiHoSo(Long id, TuChoiHoSoRequest req, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        requireState(loadDeTai(id), TopicState.DANG_XEM_XET_BOI_PNCKH);
        DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_TU_CHOI, nckhId);
        notifyOwner(saved, "RGMS - Há»“ sÆ¡ bá»‹ tá»« chá»‘i",
                "Há»“ sÆ¡ bá»‹ tá»« chá»‘i á»Ÿ bÆ°á»›c sÆ¡ tháº©m. LÃ½ do: " + req.getLyDo().trim());
        return deTaiMapper.toResponse(saved);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 10. Láº¬P Tá»” PB: DANG_XEM_XET â†’ DANG_PHAN_BIEN  [FSM: PNCKH_LAP_TO_PB]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse lapToPhanBien(Long id, LapToPhanBienRequest req, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        DeTai dt = loadDeTai(id);
        requireState(dt, TopicState.DANG_XEM_XET_BOI_PNCKH);

        Set<Long> ids = new HashSet<>(req.getThanhVienIds());
        if (ids.isEmpty())
            throw new BusinessException("Danh sÃ¡ch thÃ nh viÃªn PB khÃ´ng Ä‘Æ°á»£c rá»—ng.", HttpStatus.BAD_REQUEST);
        if (toPhanBienRepository.existsByDeTaiId(id))
            throw new BusinessException("Äá» tÃ i Ä‘Ã£ Ä‘Æ°á»£c láº­p tá»• pháº£n biá»‡n.", HttpStatus.CONFLICT);

        List<NguoiDung> members = nguoiDungRepository.findByIdIn(ids);
        if (members.size() != ids.size())
            throw new BusinessException("CÃ³ ngÆ°á»i dÃ¹ng khÃ´ng tá»“n táº¡i trong danh sÃ¡ch.", HttpStatus.BAD_REQUEST);
        if (members.stream().anyMatch(m -> !ROLE_TPB.equals(m.getVaiTro())))
            throw new BusinessException("Táº¥t cáº£ thÃ nh viÃªn PB pháº£i cÃ³ vai trÃ² TO_PHAN_BIEN.", HttpStatus.BAD_REQUEST);

        ToPhanBien tpb = new ToPhanBien();
        tpb.setDeTai(dt);
        tpb.setDeadlineNop(req.getDeadlineNop());
        tpb.setKetQuaTongHop(KQ_CHUA_CO);
        ToPhanBien savedTpb = toPhanBienRepository.save(tpb);

        members.forEach(m -> {
            ThanhVienToPhanBien tv = new ThanhVienToPhanBien();
            tv.setToPhanBien(savedTpb); tv.setNguoiDung(m); tv.setKetQua(KQ_CHUA_NOP);
            thanhVienToPhanBienRepository.save(tv);
        });

        DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_LAP_TO_PB, nckhId);
        members.forEach(m -> notifyUser(m, saved, "RGMS - Má»i pháº£n biá»‡n Ä‘á» tÃ i",
                "Tháº§y/cÃ´ Ä‘Æ°á»£c phÃ¢n cÃ´ng pháº£n biá»‡n. Háº¡n ná»™p: " + req.getDeadlineNop() + "."));
        notifyOwner(saved, "RGMS - Äá» tÃ i chuyá»ƒn sang pháº£n biá»‡n",
                "Äá» tÃ i cá»§a tháº§y/cÃ´ Ä‘Ã£ chuyá»ƒn sang giai Ä‘oáº¡n pháº£n biá»‡n.");
        return deTaiMapper.toResponse(saved);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 11. PB Ná»˜P Káº¾T QUáº¢ (khÃ´ng Ä‘á»•i state DeTai)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse pbNopKetQua(Long id, KetQuaPBRequest req, Long pbId) {
        requireRole(loadUser(pbId), ROLE_TPB);
        DeTai dt = loadDeTai(id);
        requireState(dt, TopicState.DANG_PHAN_BIEN);
        String kq = normalizeCode(req.getKetQua(), Set.of(KQ_CHAP, KQ_CAN_SUA, KQ_TU_CHOI),
                "Káº¿t quáº£ pháº£n biá»‡n khÃ´ng há»£p lá»‡.");
        ThanhVienToPhanBien tv = thanhVienToPhanBienRepository
                .findByToPhanBien_DeTaiIdAndNguoiDung_Id(id, pbId)
                .orElseThrow(() -> new AccessDeniedException("Báº¡n khÃ´ng thuá»™c tá»• pháº£n biá»‡n cá»§a Ä‘á» tÃ i nÃ y."));
        if (!KQ_CHUA_NOP.equals(tv.getKetQua()))
            throw new BusinessException("ThÃ nh viÃªn PB Ä‘Ã£ ná»™p káº¿t quáº£ trÆ°á»›c Ä‘Ã³.", HttpStatus.CONFLICT);
        requireBeforeDeadline(tv.getToPhanBien().getDeadlineNop(), "ÄÃ£ quÃ¡ háº¡n ná»™p káº¿t quáº£ pháº£n biá»‡n.");
        tv.setKetQua(kq); tv.setNhanXet(req.getNhanXet().trim()); tv.setNgayNop(LocalDateTime.now());
        thanhVienToPhanBienRepository.save(tv);
        recordAudit(dt, "PB_NOP_KET_QUA", pbId, TopicState.DANG_PHAN_BIEN.name(), TopicState.DANG_PHAN_BIEN.name(),
                "ThÃ nh viÃªn PB ná»™p káº¿t quáº£: " + kq);
        notifyNckh("RGMS - ThÃ nh viÃªn PB Ä‘Ã£ ná»™p káº¿t quáº£", dt, "Má»™t thÃ nh viÃªn tá»• PB Ä‘Ã£ ná»™p káº¿t quáº£ Ä‘Ã¡nh giÃ¡.");
        return deTaiMapper.toResponse(dt);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 12. XÃ‰T DUYá»†T KQ PB  [FSM: PNCKH_ACCEPT_PB | PNCKH_YEU_CAU_SUA_TM | PNCKH_TU_CHOI]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse xetDuyetPB(Long id, XetDuyetPBRequest req, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        DeTai dt = loadDeTai(id);
        requireState(dt, TopicState.DANG_PHAN_BIEN);
        ToPhanBien tpb = toPhanBienRepository.findByDeTaiId(id)
                .orElseThrow(() -> new BusinessException("Äá» tÃ i chÆ°a cÃ³ tá»• pháº£n biá»‡n.", HttpStatus.BAD_REQUEST));

        String qd = normalizeCode(req.getQuyetDinh(), Set.of(KQ_CHAP, QD_YEU_SUA, KQ_TU_CHOI),
                "Quyáº¿t Ä‘á»‹nh xÃ©t duyá»‡t khÃ´ng há»£p lá»‡.");

        if (KQ_CHAP.equals(qd)) {
            tpb.setKetQuaTongHop(KQ_CHAP); toPhanBienRepository.save(tpb);
            DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_ACCEPT_PB, nckhId);
            notifyOwner(saved, "RGMS - Káº¿t quáº£ PB Ä‘Æ°á»£c cháº¥p nháº­n",
                    "Káº¿t quáº£ pháº£n biá»‡n cháº¥p nháº­n. Äá» tÃ i chuyá»ƒn sang láº­p há»£p Ä‘á»“ng.");
            return deTaiMapper.toResponse(saved);
        }

        if (QD_YEU_SUA.equals(qd)) {
            String nd = requireText(req.getNoiDungYeuCauSua(), "Ná»™i dung yÃªu cáº§u sá»­a lÃ  báº¯t buá»™c.");
            if (req.getDeadlineNopLai() == null)
                throw new BusinessException("Deadline ná»™p láº¡i lÃ  báº¯t buá»™c khi yÃªu cáº§u sá»­a.", HttpStatus.BAD_REQUEST);
            tpb.setKetQuaTongHop(KQ_CAN_SUA); toPhanBienRepository.save(tpb);
            Feedback fb = new Feedback();
            fb.setDeTaiId(dt.getId()); fb.setLoai(FB_KQPB); fb.setNoiDung(nd);
            fb.setDeadlinePhanHoi(toEndOfDay(req.getDeadlineNopLai()));
            fb.setTrangThai(FB_CHO_XL); fb.setNguoiTaoId(nckhId);
            feedbackRepository.save(fb);
            DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_YEU_CAU_SUA_TM, nckhId);
            notifyOwner(saved, "RGMS - YÃªu cáº§u chá»‰nh sá»­a thuyáº¿t minh",
                    "Há»™i Ä‘á»“ng yÃªu cáº§u chá»‰nh sá»­a TM. Háº¡n ná»™p láº¡i: " + req.getDeadlineNopLai() + ".");
            return deTaiMapper.toResponse(saved);
        }

        tpb.setKetQuaTongHop(KQ_TU_CHOI); toPhanBienRepository.save(tpb);
        DeTai saved = fsmService.transition(id, TopicEvent.PNCKH_TU_CHOI, nckhId);
        notifyOwner(saved, "RGMS - Äá» tÃ i bá»‹ tá»« chá»‘i sau PB", "Äá» tÃ i bá»‹ tá»« chá»‘i sau xÃ©t duyá»‡t káº¿t quáº£ PB.");
        return deTaiMapper.toResponse(saved);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 13. GV Ná»˜P Láº I THUYáº¾T MINH: CHO_CHINH_SUA â†’ DANG_PHAN_BIEN  [FSM: GV_NOP_SUA_TM]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse gvNopLaiThuyetMinh(Long id, Long gvId) {
        requireRole(loadUser(gvId), ROLE_GV);
        DeTai dt = loadDeTai(id);
        requireOwner(dt, gvId);
        requireState(dt, TopicState.CHO_CHINH_SUA_THUYET_MINH);
        Feedback fb = loadOpenFeedback(id, FB_KQPB);
        requireBeforeDeadline(fb.getDeadlinePhanHoi(), "ÄÃ£ quÃ¡ háº¡n ná»™p láº¡i thuyáº¿t minh.");
        if (!taiLieuRepository.existsByDeTaiIdAndLoaiFileAndUploadedAtAfter(id, LOAI_TM, fb.getCreatedAt()))
            throw new BusinessException("Vui lÃ²ng upload thuyáº¿t minh Ä‘Ã£ chá»‰nh sá»­a trÆ°á»›c khi ná»™p láº¡i.", HttpStatus.BAD_REQUEST);
        fb.setTrangThai(FB_DA_XL); feedbackRepository.save(fb);
        DeTai saved = fsmService.transition(id, TopicEvent.GV_NOP_SUA_TM, gvId);
        notifyNckh("RGMS - GV Ä‘Ã£ ná»™p láº¡i thuyáº¿t minh", saved, "Giáº£ng viÃªn Ä‘Ã£ ná»™p láº¡i TM chá»‰nh sá»­a.");
        return deTaiMapper.toResponse(saved);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 14. SOáº N Há»¢P Äá»’NG (khÃ´ng Ä‘á»•i state DeTai â€” táº¡o HopDong entity)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse soanHopDong(Long id, SoanHopDongRequest req, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        DeTai dt = loadDeTai(id);
        requireState(dt, TopicState.DANG_LAP_HOP_DONG);
        if (req.getNgayKetThuc().isBefore(req.getNgayBatDau()))
            throw new BusinessException("NgÃ y káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c trÆ°á»›c ngÃ y báº¯t Ä‘áº§u.", HttpStatus.BAD_REQUEST);
        if (hopDongRepository.existsByDeTaiId(id))
            throw new BusinessException("Äá» tÃ i Ä‘Ã£ cÃ³ há»£p Ä‘á»“ng.", HttpStatus.CONFLICT);
        HopDong hd = new HopDong();
        hd.setDeTaiId(dt.getId()); hd.setKinhPhi(req.getKinhPhi());
        hd.setNgayBatDau(req.getNgayBatDau()); hd.setNgayKetThuc(req.getNgayKetThuc());
        hd.setTyLeTamUng(req.getTyLeTamUng()); hd.setTrangThaiHopDong(HD_CHO_PH);
        hopDongRepository.save(hd);
        recordAudit(dt, "SOAN_HOP_DONG", nckhId, TopicState.DANG_LAP_HOP_DONG.name(),
                TopicState.DANG_LAP_HOP_DONG.name(), "PhÃ²ng NCKH soáº¡n dá»± tháº£o há»£p Ä‘á»“ng");
        notifyOwner(dt, "RGMS - CÃ³ dá»± tháº£o há»£p Ä‘á»“ng", "PhÃ²ng NCKH Ä‘Ã£ soáº¡n dá»± tháº£o há»£p Ä‘á»“ng, vui lÃ²ng xem xÃ©t.");
        return deTaiMapper.toResponse(dt);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 15. PHáº¢N Há»’I Há»¢P Äá»’NG (GV Ä‘á»“ng Ã½/khÃ´ng Ä‘á»“ng Ã½ â€” khÃ´ng Ä‘á»•i state DeTai)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse phanHoiHopDong(Long id, PhanHoiHopDongRequest req, Long gvId) {
        requireRole(loadUser(gvId), ROLE_GV);
        DeTai dt = loadDeTai(id);
        requireOwner(dt, gvId);
        requireState(dt, TopicState.DANG_LAP_HOP_DONG);
        HopDong hd = loadHopDong(id);
        requireHopDongState(hd, HD_CHO_PH);

        if (Boolean.TRUE.equals(req.getDongY())) {
            hd.setTrangThaiHopDong(HD_CHO_KY); hopDongRepository.save(hd);
            recordAudit(dt, "GV_DONG_Y_HOP_DONG", gvId, TopicState.DANG_LAP_HOP_DONG.name(),
                    TopicState.DANG_LAP_HOP_DONG.name(), "GV Ä‘á»“ng Ã½ dá»± tháº£o HÄ");
            notifyNckh("RGMS - GV Ä‘Ã£ Ä‘á»“ng Ã½ dá»± tháº£o HÄ", dt, "Giáº£ng viÃªn Ä‘á»“ng Ã½, cÃ³ thá»ƒ tiáº¿n hÃ nh kÃ½ HÄ.");
            return deTaiMapper.toResponse(dt);
        }

        String gc = requireText(req.getNoiDungGhiChu(), "Ná»™i dung gÃ³p Ã½ lÃ  báº¯t buá»™c khi khÃ´ng Ä‘á»“ng Ã½.");
        hd.setTrangThaiHopDong(HD_YEU_SUA); hopDongRepository.save(hd);
        Feedback fb = new Feedback();
        fb.setDeTaiId(dt.getId()); fb.setLoai(FB_HD); fb.setNoiDung(gc);
        fb.setTrangThai(FB_CHO_XL); fb.setNguoiTaoId(gvId);
        feedbackRepository.save(fb);
        recordAudit(dt, "GV_PHAN_HOI_HOP_DONG", gvId, TopicState.DANG_LAP_HOP_DONG.name(),
                TopicState.DANG_LAP_HOP_DONG.name(), gc);
        notifyNckh("RGMS - GV cÃ³ gÃ³p Ã½ dá»± tháº£o HÄ", dt, "GV cÃ³ gÃ³p Ã½: " + gc);
        return deTaiMapper.toResponse(dt);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 16. KÃ Há»¢P Äá»’NG: DANG_LAP â†’ DANG_THUC_HIEN  [FSM: HAI_BEN_KY_HD]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse kyHopDong(Long id, MultipartFile fileScan, LocalDate ngayKy, Long nckhId) {
        requireRole(loadUser(nckhId), ROLE_NCK);
        DeTai dt = loadDeTai(id);
        requireState(dt, TopicState.DANG_LAP_HOP_DONG);
        HopDong hd = loadHopDong(id);
        requireHopDongState(hd, HD_CHO_KY);
        String path = fileUploadService.saveContractScan(fileScan, id);
        hd.setFileScanPath(path); hd.setNgayKy(ngayKy); hd.setTrangThaiHopDong(HD_DA_KY);
        hopDongRepository.save(hd);
        DeTai saved = fsmService.transition(id, TopicEvent.HAI_BEN_KY_HD, nckhId);
        notifyOwner(saved, "RGMS - Äá» tÃ i Ä‘Ã£ kÃ½ há»£p Ä‘á»“ng",
                "ChÃºc má»«ng, Ä‘á» tÃ i Ä‘Ã£ kÃ½ HÄ vÃ  chuyá»ƒn sang giai Ä‘oáº¡n thá»±c hiá»‡n.");
        return deTaiMapper.toResponse(saved);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 17. GV RÃšT Äá»€ TÃ€I  [FSM: GV_RUT]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTaiResponse gvRutDeTai(Long id, Long gvId) {
        requireRole(loadUser(gvId), ROLE_GV);
        requireOwner(loadDeTai(id), gvId);
        DeTai saved = fsmService.transition(id, TopicEvent.GV_RUT, gvId);
        return deTaiMapper.toResponse(saved);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // 18. SCHEDULER: AUTO TREO  [FSM: HE_THONG_TREO]
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public DeTai autoTreoDeTai(Long id, Long systemActorId) {
        return fsmService.transition(id, TopicEvent.HE_THONG_TREO, systemActorId);
    }

    // â”€â”€ Private helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private String generateMaSo() {
        long seq = deTaiRepository.nextMaSoSequence();
        return "NCKH-" + Year.now().getValue() + "-" + String.format("%04d", seq);
    }

    private DeTai loadDeTai(Long id) {
        return deTaiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Äá» tÃ i", "id", id));
    }

    private NguoiDung loadUser(Long id) {
        return nguoiDungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng", "id", id));
    }

    private HopDong loadHopDong(Long deTaiId) {
        return hopDongRepository.findByDeTaiId(deTaiId)
                .orElseThrow(() -> new BusinessException("Äá» tÃ i chÆ°a cÃ³ há»£p Ä‘á»“ng.", HttpStatus.BAD_REQUEST));
    }

    private Feedback loadOpenFeedback(Long deTaiId, String loai) {
        return feedbackRepository
                .findFirstByDeTaiIdAndLoaiAndTrangThaiOrderByCreatedAtDesc(deTaiId, loai, FB_CHO_XL)
                .orElseThrow(() -> new BusinessException("KhÃ´ng tÃ¬m tháº¥y yÃªu cáº§u pháº£n há»“i Ä‘ang chá».", HttpStatus.BAD_REQUEST));
    }

    private void requireRole(NguoiDung actor, String role) {
        if (!role.equals(actor.getVaiTro()))
            throw new AccessDeniedException("Vai trÃ² khÃ´ng há»£p lá»‡ cho thao tÃ¡c nÃ y.");
    }

    private void requireOwner(DeTai dt, Long userId) {
        if (dt.getChuNhiem() == null || !dt.getChuNhiem().getId().equals(userId))
            throw new AccessDeniedException("Báº¡n khÃ´ng pháº£i chá»§ nhiá»‡m Ä‘á» tÃ i nÃ y.");
    }

    private void requireState(DeTai dt, TopicState expected) {
        if (dt.getStatus() != expected)
            throw new BusinessException("Tráº¡ng thÃ¡i hiá»‡n táº¡i [" + dt.getStatus()
                    + "] khÃ´ng há»£p lá»‡ cho thao tÃ¡c nÃ y.", HttpStatus.CONFLICT);
    }

    private void requireHopDongState(HopDong hd, String expected) {
        if (!expected.equals(hd.getTrangThaiHopDong()))
            throw new BusinessException("Tráº¡ng thÃ¡i há»£p Ä‘á»“ng [" + hd.getTrangThaiHopDong()
                    + "] khÃ´ng há»£p lá»‡.", HttpStatus.CONFLICT);
    }

    private void requireBeforeDeadline(LocalDateTime deadline, String msg) {
        if (deadline != null && LocalDateTime.now().isAfter(deadline))
            throw new BusinessException(msg, HttpStatus.BAD_REQUEST);
    }

    private void requireBeforeDeadline(LocalDate deadline, String msg) {
        if (deadline != null && LocalDate.now().isAfter(deadline))
            throw new BusinessException(msg, HttpStatus.BAD_REQUEST);
    }

    private String normalizeCode(String value, Set<String> allowed, String msg) {
        String v = requireText(value, msg).toUpperCase();
        if (!allowed.contains(v)) throw new BusinessException(msg, HttpStatus.BAD_REQUEST);
        return v;
    }

    private String requireText(String value, String msg) {
        if (!StringUtils.hasText(value)) throw new BusinessException(msg, HttpStatus.BAD_REQUEST);
        return value.trim();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX.withNano(0));
    }

    private void recordAudit(DeTai dt, String action, Long actorId,
                              String prevState, String nextState, String note) {
        AuditLog log = new AuditLog();
        log.setDeTai(dt);
        log.setHanhDong(action);
        NguoiDung actor = new NguoiDung(); actor.setId(actorId);
        log.setActor(actor);
        log.setTrangThaiTruoc(prevState);
        log.setTrangThaiSau(nextState);
        log.setMeta(note);
        auditLogRepository.save(log);
    }

    private Map<Long, String> loadActorNames(List<AuditLog> logs) {
        Set<Long> actorIds = logs.stream()
                .filter(l -> l.getActor() != null)
                .map(l -> l.getActor().getId())
                .collect(Collectors.toSet());
        if (actorIds.isEmpty()) return Collections.emptyMap();
        return nguoiDungRepository.findByIdIn(actorIds).stream()
                .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen));
    }

    private void notifyNckh(String subject, DeTai dt, String msg) {
        nguoiDungRepository.findByVaiTro(ROLE_NCK)
                .forEach(u -> notifyUser(u, dt, subject, msg));
    }

    private void notifyOwner(DeTai dt, String subject, String msg) {
        if (dt.getChuNhiem() != null) notifyUser(dt.getChuNhiem(), dt, subject, msg);
    }

    private void notifyUser(NguoiDung recipient, DeTai dt, String subject, String msg) {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("recipientName", recipient.getHoTen());
        vars.put("title", subject);
        vars.put("message", msg);
        vars.put("maSo", dt.getMaSo());
        vars.put("tenDeTai", dt.getTenDeTai());
        emailService.guiEmail(recipient.getEmail(), subject, "generic", vars);
    }

    private String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    private PhanBienDeXuatResponse toPhanBienDeXuatResponse(PhanBienDeXuat e) {
        return PhanBienDeXuatResponse.builder()
                .id(e.getId()).hoTen(e.getHoTen())
                .email(e.getEmail()).coQuan(e.getCoQuan()).build();
    }
}

