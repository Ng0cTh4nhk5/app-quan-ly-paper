package com.rgms.modules.detai.service;

import com.rgms.exception.BusinessException;
import com.rgms.exception.ResourceNotFoundException;
import com.rgms.modules.detai.dto.AuditLogEntry;
import com.rgms.modules.detai.dto.DeTaiDetailResponse;
import com.rgms.modules.detai.dto.DeTaiResponse;
import com.rgms.modules.detai.dto.KetQuaPBRequest;
import com.rgms.modules.detai.dto.LapToPhanBienRequest;
import com.rgms.modules.detai.dto.PageDeTaiResponse;
import com.rgms.modules.detai.dto.PhanBienDeXuatRequest;
import com.rgms.modules.detai.dto.PhanBienDeXuatResponse;
import com.rgms.modules.detai.dto.PhanHoiHopDongRequest;
import com.rgms.modules.detai.dto.SoanHopDongRequest;
import com.rgms.modules.detai.dto.TaoDeTaiRequest;
import com.rgms.modules.detai.dto.TuChoiHoSoRequest;
import com.rgms.modules.detai.dto.XetDuyetPBRequest;
import com.rgms.modules.detai.dto.YeuCauBoSungRequest;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.entity.Feedback;
import com.rgms.modules.detai.entity.HopDong;
import com.rgms.modules.detai.entity.KyNCKH;
import com.rgms.modules.detai.entity.NguoiDung;
import com.rgms.modules.detai.entity.PhanBienDeXuat;
import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import com.rgms.modules.detai.entity.ToPhanBien;
import com.rgms.modules.detai.mapper.DeTaiMapper;
import com.rgms.modules.detai.repo.AuditLogRepository;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.repo.FeedbackRepository;
import com.rgms.modules.detai.repo.HopDongRepository;
import com.rgms.modules.detai.repo.KyNCKHRepository;
import com.rgms.modules.detai.repo.NguoiDungRepository;
import com.rgms.modules.detai.repo.PhanBienDeXuatRepository;
import com.rgms.modules.detai.repo.ThanhVienToPhanBienRepository;
import com.rgms.modules.detai.repo.ToPhanBienRepository;
import com.rgms.modules.email.EmailService;
import com.rgms.modules.files.mapper.TaiLieuMapper;
import com.rgms.modules.files.repo.TaiLieuRepository;
import com.rgms.modules.files.service.FileUploadService;
import com.rgms.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResearchTopicService {

    private static final String ROLE_GIANG_VIEN = "GIANG_VIEN";
    private static final String ROLE_NCKH = "NCKH";
    private static final String ROLE_TO_PHAN_BIEN = "TO_PHAN_BIEN";

    private static final String KY_DANG_MO = "DANG_MO";
    private static final String DRAFT = "DRAFT";
    private static final String CHO_PNCKH_XEM_XET = "CHO_PNCKH_XEM_XET";
    private static final String DANG_XEM_XET_BOI_PNCKH = "DANG_XEM_XET_BOI_PNCKH";
    private static final String CHO_BO_SUNG_HO_SO = "CHO_BO_SUNG_HO_SO";
    private static final String DANG_PHAN_BIEN = "DANG_PHAN_BIEN";
    private static final String CHO_CHINH_SUA_THUYET_MINH = "CHO_CHINH_SUA_THUYET_MINH";
    private static final String DANG_LAP_HOP_DONG = "DANG_LAP_HOP_DONG";
    private static final String DANG_THUC_HIEN = "DANG_THUC_HIEN";
    private static final String BI_TU_CHOI = "BI_TU_CHOI";

    private static final String LOAI_THUYET_MINH = "THUYET_MINH";
    private static final String FEEDBACK_BO_SUNG_HO_SO = "BO_SUNG_HO_SO";
    private static final String FEEDBACK_KET_QUA_PB = "KET_QUA_PB";
    private static final String FEEDBACK_HOP_DONG = "HOP_DONG";
    private static final String FEEDBACK_CHO_XU_LY = "CHO_XU_LY";
    private static final String FEEDBACK_DA_XU_LY = "DA_XU_LY";

    private static final String KQ_CHUA_NOP = "CHUA_NOP";
    private static final String KQ_CHAP_NHAN = "CHAP_NHAN";
    private static final String KQ_CAN_SUA = "CAN_SUA";
    private static final String KQ_TU_CHOI = "TU_CHOI";
    private static final String QD_YEU_CAU_SUA = "YEU_CAU_SUA";

    private static final String HD_CHO_PHAN_HOI = "CHO_PHAN_HOI";
    private static final String HD_CHO_KY = "CHO_KY";
    private static final String HD_DA_KY = "DA_KY";
    private static final String HD_YEU_CAU_SUA = "YEU_CAU_SUA";

    private final DeTaiRepository deTaiRepository;
    private final KyNCKHRepository kyNCKHRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final TaiLieuRepository taiLieuRepository;
    private final AuditLogRepository auditLogRepository;
    private final PhanBienDeXuatRepository phanBienDeXuatRepository;
    private final FeedbackRepository feedbackRepository;
    private final ToPhanBienRepository toPhanBienRepository;
    private final ThanhVienToPhanBienRepository thanhVienToPhanBienRepository;
    private final HopDongRepository hopDongRepository;
    private final DeTaiMapper deTaiMapper;
    private final TaiLieuMapper taiLieuMapper;
    private final FileUploadService fileUploadService;
    private final EmailService emailService;

    @Transactional
    public DeTaiResponse taoDeTai(TaoDeTaiRequest request, Long gvId) {
        NguoiDung chuNhiem = loadUser(gvId);
        requireRole(chuNhiem, ROLE_GIANG_VIEN);

        KyNCKH kyNCKH = kyNCKHRepository.findById(request.getKyNckhId())
                .orElseThrow(() -> new BusinessException("Kỳ NCKH không tồn tại.", HttpStatus.BAD_REQUEST));
        if (!KY_DANG_MO.equals(kyNCKH.getTrangThai())) {
            throw new BusinessException("Kỳ NCKH đã đóng, không thể tạo đề tài.", HttpStatus.BAD_REQUEST);
        }

        DeTai deTai = DeTai.builder()
                .maSo(generateMaSo())
                .tenDeTai(request.getTenDeTai().trim())
                .moTa(request.getMoTa())
                .linhVuc(request.getLinhVuc())
                .trangThai(DRAFT)
                .chuNhiem(chuNhiem)
                .kyNckh(kyNCKH)
                .donVi(chuNhiem.getDonVi())
                .build();
        DeTai saved = deTaiRepository.save(deTai);

        recordAudit(saved.getId(), "TAO_DE_TAI", gvId, null, DRAFT, "INFO", "Khởi tạo đề tài nháp");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageDeTaiResponse danhSach(String trangThai, Pageable pageable, CustomUserDetails currentUser) {
        String normalizedTrangThai = StringUtils.hasText(trangThai) ? trangThai.trim() : null;
        Page<DeTai> pageData;

        if (ROLE_GIANG_VIEN.equals(currentUser.getRole())) {
            pageData = normalizedTrangThai == null
                    ? deTaiRepository.findByChuNhiem_Id(currentUser.getId(), pageable)
                    : deTaiRepository.findByChuNhiem_IdAndTrangThai(currentUser.getId(), normalizedTrangThai, pageable);
        } else {
            pageData = normalizedTrangThai == null
                    ? deTaiRepository.findAll(pageable)
                    : deTaiRepository.findByTrangThai(normalizedTrangThai, pageable);
        }

        return PageDeTaiResponse.builder()
                .content(pageData.getContent().stream().map(deTaiMapper::toResponse).toList())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .page(pageData.getNumber())
                .build();
    }

    @Transactional(readOnly = true)
    public DeTaiDetailResponse layChiTiet(Long id, CustomUserDetails currentUser) {
        DeTai deTai = loadDeTai(id);

        if (ROLE_GIANG_VIEN.equals(currentUser.getRole())
                && (deTai.getChuNhiem() == null || !deTai.getChuNhiem().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Bạn không có quyền xem đề tài này.");
        }

        var taiLieu = taiLieuRepository.findByDeTaiIdOrderByUploadedAtDesc(id)
                .stream()
                .map(taiLieuMapper::toResponse)
                .toList();

        var auditLogs = auditLogRepository.findByDeTaiIdOrderByCreatedAtAsc(id);
        Map<Long, String> actorNames = loadActorNames(auditLogs);
        List<AuditLogEntry> auditLogEntries = auditLogs.stream()
                .map(auditLog -> deTaiMapper.toAuditLogEntry(auditLog, actorNames))
                .toList();

        return deTaiMapper.toDetailResponse(deTai, taiLieu, auditLogEntries);
    }

    @Transactional
    public PhanBienDeXuatResponse themPhanBienDeXuat(Long id, PhanBienDeXuatRequest request, Long gvId) {
        NguoiDung actor = loadUser(gvId);
        requireRole(actor, ROLE_GIANG_VIEN);
        DeTai deTai = loadDeTai(id);
        requireOwner(deTai, gvId);
        requireState(deTai, DRAFT);

        PhanBienDeXuat saved = phanBienDeXuatRepository.save(PhanBienDeXuat.builder()
                .deTaiId(id)
                .hoTen(request.getHoTen().trim())
                .email(trimToNull(request.getEmail()))
                .coQuan(trimToNull(request.getCoQuan()))
                .build());

        recordAudit(id, "THEM_PHAN_BIEN_DE_XUAT", gvId, DRAFT, DRAFT, "INFO",
                "Thêm phản biện đề xuất: " + saved.getHoTen());
        return toPhanBienDeXuatResponse(saved);
    }

    @Transactional
    public DeTaiResponse guiHoSo(Long id, Long gvId) {
        NguoiDung actor = loadUser(gvId);
        requireRole(actor, ROLE_GIANG_VIEN);
        DeTai deTai = loadDeTai(id);
        requireOwner(deTai, gvId);
        requireState(deTai, DRAFT);

        if (!taiLieuRepository.existsByDeTaiIdAndLoaiFile(id, LOAI_THUYET_MINH)) {
            throw new BusinessException("Hồ sơ phải có ít nhất một file thuyết minh.", HttpStatus.BAD_REQUEST);
        }
        if (phanBienDeXuatRepository.countByDeTaiId(id) < 1) {
            throw new BusinessException("Vui lòng đề xuất ít nhất một phản biện trước khi gửi hồ sơ.", HttpStatus.BAD_REQUEST);
        }

        DeTai saved = transition(deTai, "GUI_HO_SO", gvId, CHO_PNCKH_XEM_XET, "INFO",
                "Giảng viên gửi hồ sơ đến Phòng NCKH");
        notifyNckh("RGMS - Hồ sơ đề tài mới chờ xem xét", saved,
                "Một hồ sơ đề tài mới đã được gửi và đang chờ Phòng NCKH xem xét.");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse tiepNhan(Long id, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, CHO_PNCKH_XEM_XET);

        DeTai saved = transition(deTai, "TIEP_NHAN_HO_SO", nckhId, DANG_XEM_XET_BOI_PNCKH, "INFO",
                "Phòng NCKH tiếp nhận hồ sơ sơ thẩm");
        notifyOwner(saved, "RGMS - Hồ sơ đã được tiếp nhận",
                "Hồ sơ đề tài của thầy/cô đã được Phòng NCKH tiếp nhận để sơ thẩm.");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse yeuCauBoSung(Long id, YeuCauBoSungRequest request, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_XEM_XET_BOI_PNCKH);

        feedbackRepository.save(Feedback.builder()
                .deTaiId(id)
                .loai(FEEDBACK_BO_SUNG_HO_SO)
                .noiDung(request.getNoiDung().trim())
                .deadlinePhanHoi(toEndOfDay(request.getDeadlinePhanHoi()))
                .trangThai(FEEDBACK_CHO_XU_LY)
                .nguoiTaoId(nckhId)
                .build());

        DeTai saved = transition(deTai, "YEU_CAU_BO_SUNG_HO_SO", nckhId, CHO_BO_SUNG_HO_SO, "INFO",
                request.getNoiDung().trim());
        notifyOwner(saved, "RGMS - Yêu cầu bổ sung hồ sơ",
                "Phòng NCKH yêu cầu bổ sung hồ sơ đề tài. Hạn phản hồi: " + request.getDeadlinePhanHoi() + ".");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse gvNopBoSung(Long id, Long gvId) {
        NguoiDung actor = loadUser(gvId);
        requireRole(actor, ROLE_GIANG_VIEN);
        DeTai deTai = loadDeTai(id);
        requireOwner(deTai, gvId);
        requireState(deTai, CHO_BO_SUNG_HO_SO);

        Feedback feedback = loadOpenFeedback(id, FEEDBACK_BO_SUNG_HO_SO);
        requireBeforeDeadline(feedback.getDeadlinePhanHoi(), "Đã quá hạn nộp bổ sung hồ sơ.");
        feedback.setTrangThai(FEEDBACK_DA_XU_LY);
        feedbackRepository.save(feedback);

        DeTai saved = transition(deTai, "GV_NOP_BO_SUNG_HO_SO", gvId, DANG_XEM_XET_BOI_PNCKH, "INFO",
                "Giảng viên đã nộp bổ sung hồ sơ");
        notifyNckh("RGMS - GV đã nộp bổ sung hồ sơ", saved,
                "Giảng viên đã nộp bổ sung hồ sơ theo yêu cầu sơ thẩm.");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse tuChoiHoSo(Long id, TuChoiHoSoRequest request, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_XEM_XET_BOI_PNCKH);

        DeTai saved = transition(deTai, "TU_CHOI_HO_SO", nckhId, BI_TU_CHOI, "WARNING", request.getLyDo().trim());
        notifyOwner(saved, "RGMS - Hồ sơ đề tài bị từ chối",
                "Hồ sơ đề tài bị từ chối ở bước sơ thẩm. Lý do: " + request.getLyDo().trim());
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse lapToPhanBien(Long id, LapToPhanBienRequest request, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_XEM_XET_BOI_PNCKH);

        Set<Long> uniqueMemberIds = new HashSet<>(request.getThanhVienIds());
        if (uniqueMemberIds.isEmpty()) {
            throw new BusinessException("Danh sách thành viên phản biện không được rỗng.", HttpStatus.BAD_REQUEST);
        }
        if (toPhanBienRepository.existsByDeTaiId(id)) {
            throw BusinessException.conflict("Đề tài đã được lập tổ phản biện.");
        }

        List<NguoiDung> members = nguoiDungRepository.findByIdIn(uniqueMemberIds);
        if (members.size() != uniqueMemberIds.size()) {
            throw new BusinessException("Danh sách thành viên phản biện có người dùng không tồn tại.", HttpStatus.BAD_REQUEST);
        }
        if (members.stream().anyMatch(member -> !ROLE_TO_PHAN_BIEN.equals(member.getVaiTro()))) {
            throw new BusinessException("Tất cả thành viên phản biện phải có vai trò TO_PHAN_BIEN.", HttpStatus.BAD_REQUEST);
        }

        ToPhanBien toPhanBien = toPhanBienRepository.save(ToPhanBien.builder()
                .deTaiId(id)
                .deadlineNop(toEndOfDay(request.getDeadlineNop()))
                .ketQuaTongHop("CHUA_CO")
                .build());

        members.forEach(member -> thanhVienToPhanBienRepository.save(ThanhVienToPhanBien.builder()
                .toPhanBien(toPhanBien)
                .nguoiDung(member)
                .ketQua(KQ_CHUA_NOP)
                .build()));

        DeTai saved = transition(deTai, "LAP_TO_PHAN_BIEN", nckhId, DANG_PHAN_BIEN, "INFO",
                "Lập tổ phản biện gồm " + members.size() + " thành viên");
        members.forEach(member -> notifyUser(member, saved, "RGMS - Mời phản biện đề tài",
                "Thầy/cô được phân công phản biện đề tài. Hạn nộp kết quả: " + request.getDeadlineNop() + "."));
        notifyOwner(saved, "RGMS - Đề tài chuyển sang phản biện",
                "Đề tài của thầy/cô đã được chuyển sang giai đoạn phản biện.");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse pbNopKetQua(Long id, KetQuaPBRequest request, Long pbId) {
        NguoiDung actor = loadUser(pbId);
        requireRole(actor, ROLE_TO_PHAN_BIEN);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_PHAN_BIEN);

        String ketQua = normalizeRequiredCode(request.getKetQua(), Set.of(KQ_CHAP_NHAN, KQ_CAN_SUA, KQ_TU_CHOI),
                "Kết quả phản biện không hợp lệ.");
        ThanhVienToPhanBien member = thanhVienToPhanBienRepository
                .findByToPhanBien_DeTaiIdAndNguoiDung_Id(id, pbId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc tổ phản biện của đề tài này."));

        if (!KQ_CHUA_NOP.equals(member.getKetQua())) {
            throw BusinessException.conflict("Thành viên phản biện đã nộp kết quả trước đó.");
        }
        requireBeforeDeadline(member.getToPhanBien().getDeadlineNop(), "Đã quá hạn nộp kết quả phản biện.");

        member.setKetQua(ketQua);
        member.setNhanXet(request.getNhanXet().trim());
        member.setNopLuc(LocalDateTime.now());
        thanhVienToPhanBienRepository.save(member);

        recordAudit(id, "PB_NOP_KET_QUA", pbId, DANG_PHAN_BIEN, DANG_PHAN_BIEN, "INFO",
                "Thành viên phản biện nộp kết quả: " + ketQua);
        notifyNckh("RGMS - Thành viên phản biện đã nộp kết quả", deTai,
                "Một thành viên tổ phản biện đã nộp kết quả đánh giá đề tài.");
        return deTaiMapper.toResponse(deTai);
    }

    @Transactional
    public DeTaiResponse xetDuyetPB(Long id, XetDuyetPBRequest request, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_PHAN_BIEN);
        ToPhanBien toPhanBien = toPhanBienRepository.findByDeTaiId(id)
                .orElseThrow(() -> new BusinessException("Đề tài chưa có tổ phản biện.", HttpStatus.BAD_REQUEST));

        String quyetDinh = normalizeRequiredCode(request.getQuyetDinh(), Set.of(KQ_CHAP_NHAN, QD_YEU_CAU_SUA, KQ_TU_CHOI),
                "Quyết định xét duyệt phản biện không hợp lệ.");
        if (KQ_CHAP_NHAN.equals(quyetDinh)) {
            toPhanBien.setKetQuaTongHop(KQ_CHAP_NHAN);
            toPhanBienRepository.save(toPhanBien);
            DeTai saved = transition(deTai, "XET_DUYET_PB_CHAP_NHAN", nckhId, DANG_LAP_HOP_DONG, "INFO",
                    trimToNull(request.getGhiChu()));
            notifyOwner(saved, "RGMS - Kết quả phản biện được chấp nhận",
                    "Kết quả phản biện đã được chấp nhận. Đề tài chuyển sang giai đoạn lập hợp đồng.");
            return deTaiMapper.toResponse(saved);
        }

        if (QD_YEU_CAU_SUA.equals(quyetDinh)) {
            String noiDung = requireText(request.getNoiDungYeuCauSua(), "Nội dung yêu cầu sửa là bắt buộc.");
            if (request.getDeadlineNopLai() == null) {
                throw new BusinessException("Deadline nộp lại là bắt buộc khi yêu cầu sửa.", HttpStatus.BAD_REQUEST);
            }

            toPhanBien.setKetQuaTongHop(KQ_CAN_SUA);
            toPhanBienRepository.save(toPhanBien);
            feedbackRepository.save(Feedback.builder()
                    .deTaiId(id)
                    .loai(FEEDBACK_KET_QUA_PB)
                    .noiDung(noiDung)
                    .deadlinePhanHoi(toEndOfDay(request.getDeadlineNopLai()))
                    .trangThai(FEEDBACK_CHO_XU_LY)
                    .nguoiTaoId(nckhId)
                    .build());

            DeTai saved = transition(deTai, "XET_DUYET_PB_YEU_CAU_SUA", nckhId, CHO_CHINH_SUA_THUYET_MINH, "INFO",
                    noiDung);
            notifyOwner(saved, "RGMS - Yêu cầu chỉnh sửa thuyết minh",
                    "Hội đồng yêu cầu chỉnh sửa thuyết minh. Hạn nộp lại: " + request.getDeadlineNopLai() + ".");
            return deTaiMapper.toResponse(saved);
        }

        toPhanBien.setKetQuaTongHop(KQ_TU_CHOI);
        toPhanBienRepository.save(toPhanBien);
        DeTai saved = transition(deTai, "XET_DUYET_PB_TU_CHOI", nckhId, BI_TU_CHOI, "WARNING",
                trimToNull(request.getGhiChu()));
        notifyOwner(saved, "RGMS - Đề tài bị từ chối sau phản biện",
                "Đề tài bị từ chối sau bước xét duyệt kết quả phản biện.");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse gvNopLaiThuyetMinh(Long id, Long gvId) {
        NguoiDung actor = loadUser(gvId);
        requireRole(actor, ROLE_GIANG_VIEN);
        DeTai deTai = loadDeTai(id);
        requireOwner(deTai, gvId);
        requireState(deTai, CHO_CHINH_SUA_THUYET_MINH);

        Feedback feedback = loadOpenFeedback(id, FEEDBACK_KET_QUA_PB);
        requireBeforeDeadline(feedback.getDeadlinePhanHoi(), "Đã quá hạn nộp lại thuyết minh chỉnh sửa.");
        if (!taiLieuRepository.existsByDeTaiIdAndLoaiFileAndUploadedAtAfter(id, LOAI_THUYET_MINH, feedback.getCreatedAt())) {
            throw new BusinessException("Vui lòng upload thuyết minh đã chỉnh sửa trước khi nộp lại.", HttpStatus.BAD_REQUEST);
        }

        feedback.setTrangThai(FEEDBACK_DA_XU_LY);
        feedbackRepository.save(feedback);

        DeTai saved = transition(deTai, "GV_NOP_LAI_THUYET_MINH", gvId, DANG_PHAN_BIEN, "INFO",
                "Giảng viên nộp lại thuyết minh đã chỉnh sửa");
        notifyNckh("RGMS - GV đã nộp lại thuyết minh chỉnh sửa", saved,
                "Giảng viên đã nộp lại thuyết minh sau phản biện.");
        return deTaiMapper.toResponse(saved);
    }

    @Transactional
    public DeTaiResponse soanHopDong(Long id, SoanHopDongRequest request, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_LAP_HOP_DONG);
        if (request.getNgayKetThuc().isBefore(request.getNgayBatDau())) {
            throw new BusinessException("Ngày kết thúc hợp đồng không được trước ngày bắt đầu.", HttpStatus.BAD_REQUEST);
        }
        if (hopDongRepository.existsByDeTaiId(id)) {
            throw BusinessException.conflict("Đề tài đã có hợp đồng.");
        }

        hopDongRepository.save(HopDong.builder()
                .deTaiId(id)
                .kinhPhi(request.getKinhPhi())
                .ngayBatDau(request.getNgayBatDau())
                .ngayKetThuc(request.getNgayKetThuc())
                .tyLeTamUng(request.getTyLeTamUng())
                .trangThaiHopDong(HD_CHO_PHAN_HOI)
                .build());

        recordAudit(id, "SOAN_HOP_DONG", nckhId, DANG_LAP_HOP_DONG, DANG_LAP_HOP_DONG, "INFO",
                "Phòng NCKH soạn dự thảo hợp đồng");
        notifyOwner(deTai, "RGMS - Có dự thảo hợp đồng cần phản hồi",
                "Phòng NCKH đã soạn dự thảo hợp đồng, thầy/cô vui lòng xem xét và phản hồi.");
        return deTaiMapper.toResponse(deTai);
    }

    @Transactional
    public DeTaiResponse phanHoiHopDong(Long id, PhanHoiHopDongRequest request, Long gvId) {
        NguoiDung actor = loadUser(gvId);
        requireRole(actor, ROLE_GIANG_VIEN);
        DeTai deTai = loadDeTai(id);
        requireOwner(deTai, gvId);
        requireState(deTai, DANG_LAP_HOP_DONG);

        HopDong hopDong = loadHopDong(id);
        requireHopDongState(hopDong, HD_CHO_PHAN_HOI);

        if (Boolean.TRUE.equals(request.getDongY())) {
            hopDong.setTrangThaiHopDong(HD_CHO_KY);
            hopDongRepository.save(hopDong);
            recordAudit(id, "GV_DONG_Y_HOP_DONG", gvId, DANG_LAP_HOP_DONG, DANG_LAP_HOP_DONG, "INFO",
                    "Giảng viên đồng ý dự thảo hợp đồng");
            notifyNckh("RGMS - GV đã đồng ý dự thảo hợp đồng", deTai,
                    "Giảng viên đã đồng ý dự thảo hợp đồng, có thể tiến hành ký hợp đồng.");
            return deTaiMapper.toResponse(deTai);
        }

        String ghiChu = requireText(request.getNoiDungGhiChu(), "Nội dung góp ý hợp đồng là bắt buộc khi không đồng ý.");
        hopDong.setTrangThaiHopDong(HD_YEU_CAU_SUA);
        hopDongRepository.save(hopDong);
        feedbackRepository.save(Feedback.builder()
                .deTaiId(id)
                .loai(FEEDBACK_HOP_DONG)
                .noiDung(ghiChu)
                .trangThai(FEEDBACK_CHO_XU_LY)
                .nguoiTaoId(gvId)
                .build());

        recordAudit(id, "GV_PHAN_HOI_HOP_DONG", gvId, DANG_LAP_HOP_DONG, DANG_LAP_HOP_DONG, "INFO", ghiChu);
        notifyNckh("RGMS - GV có góp ý dự thảo hợp đồng", deTai,
                "Giảng viên có góp ý về dự thảo hợp đồng. Nội dung: " + ghiChu);
        return deTaiMapper.toResponse(deTai);
    }

    @Transactional
    public DeTaiResponse kyHopDong(Long id, MultipartFile fileScan, LocalDate ngayKy, Long nckhId) {
        NguoiDung actor = loadUser(nckhId);
        requireRole(actor, ROLE_NCKH);
        DeTai deTai = loadDeTai(id);
        requireState(deTai, DANG_LAP_HOP_DONG);

        HopDong hopDong = loadHopDong(id);
        requireHopDongState(hopDong, HD_CHO_KY);

        String filePath = fileUploadService.saveContractScan(fileScan, id);
        hopDong.setFileScanPath(filePath);
        hopDong.setNgayKy(ngayKy);
        hopDong.setTrangThaiHopDong(HD_DA_KY);
        hopDongRepository.save(hopDong);

        DeTai saved = transition(deTai, "KY_HOP_DONG", nckhId, DANG_THUC_HIEN, "INFO",
                "Hợp đồng đã ký ngày " + ngayKy);
        notifyOwner(saved, "RGMS - Đề tài đã ký hợp đồng",
                "Chúc mừng thầy/cô, đề tài đã ký hợp đồng và chuyển sang giai đoạn thực hiện.");
        return deTaiMapper.toResponse(saved);
    }

    private String generateMaSo() {
        int year = Year.now().getValue();
        String prefix = "NCKH-" + year + "-";
        long nextNumber = deTaiRepository.nextMaSoSequence();
        return prefix + String.format("%04d", nextNumber);
    }

    private DeTai loadDeTai(Long id) {
        return deTaiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đề tài", "id", id));
    }

    private NguoiDung loadUser(Long id) {
        return nguoiDungRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", id));
    }

    private HopDong loadHopDong(Long deTaiId) {
        return hopDongRepository.findByDeTaiId(deTaiId)
                .orElseThrow(() -> new BusinessException("Đề tài chưa có hợp đồng.", HttpStatus.BAD_REQUEST));
    }

    private Feedback loadOpenFeedback(Long deTaiId, String loai) {
        return feedbackRepository.findFirstByDeTaiIdAndLoaiAndTrangThaiOrderByCreatedAtDesc(
                        deTaiId,
                        loai,
                        FEEDBACK_CHO_XU_LY)
                .orElseThrow(() -> new BusinessException("Không tìm thấy yêu cầu phản hồi đang chờ xử lý.", HttpStatus.BAD_REQUEST));
    }

    private void requireRole(NguoiDung actor, String role) {
        if (!role.equals(actor.getVaiTro())) {
            throw new AccessDeniedException("Vai trò không hợp lệ cho thao tác này.");
        }
    }

    private void requireOwner(DeTai deTai, Long userId) {
        if (deTai.getChuNhiem() == null || !deTai.getChuNhiem().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không phải chủ nhiệm đề tài này.");
        }
    }

    private void requireState(DeTai deTai, String expectedState) {
        if (!expectedState.equals(deTai.getTrangThai())) {
            throw BusinessException.conflict(
                    "Trạng thái đề tài hiện tại là " + deTai.getTrangThai()
                            + ", không thể thực hiện thao tác này.");
        }
    }

    private void requireHopDongState(HopDong hopDong, String expectedState) {
        if (!expectedState.equals(hopDong.getTrangThaiHopDong())) {
            throw BusinessException.conflict(
                    "Trạng thái hợp đồng hiện tại là " + hopDong.getTrangThaiHopDong()
                            + ", không thể thực hiện thao tác này.");
        }
    }

    private void requireBeforeDeadline(LocalDateTime deadline, String message) {
        if (deadline != null && LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException(message, HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeRequiredCode(String value, Set<String> allowedValues, String message) {
        String normalized = requireText(value, message).toUpperCase();
        if (!allowedValues.contains(normalized)) {
            throw new BusinessException(message, HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message, HttpStatus.BAD_REQUEST);
        }
        return value.trim();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX.withNano(0));
    }

    private DeTai transition(DeTai deTai,
                             String action,
                             Long actorId,
                             String nextState,
                             String severity,
                             String ghiChu) {
        String previousState = deTai.getTrangThai();
        deTai.setTrangThai(nextState);
        DeTai saved = deTaiRepository.save(deTai);
        recordAudit(saved.getId(), action, actorId, previousState, nextState, severity, ghiChu);
        return saved;
    }

    private void recordAudit(Long deTaiId,
                             String action,
                             Long actorId,
                             String previousState,
                             String nextState,
                             String severity,
                             String ghiChu) {
        auditLogRepository.save(AuditLog.builder()
                .deTaiId(deTaiId)
                .hanhDong(action)
                .actorId(actorId)
                .tuTrangThai(previousState)
                .sangTrangThai(nextState)
                .severity(StringUtils.hasText(severity) ? severity : "INFO")
                .ghiChu(ghiChu)
                .build());
    }

    private Map<Long, String> loadActorNames(List<AuditLog> auditLogs) {
        Set<Long> actorIds = auditLogs.stream()
                .map(AuditLog::getActorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (actorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return nguoiDungRepository.findByIdIn(actorIds)
                .stream()
                .collect(Collectors.toMap(NguoiDung::getId, NguoiDung::getHoTen));
    }

    private void notifyNckh(String subject, DeTai deTai, String message) {
        nguoiDungRepository.findByVaiTro(ROLE_NCKH)
                .forEach(user -> notifyUser(user, deTai, subject, message));
    }

    private void notifyOwner(DeTai deTai, String subject, String message) {
        if (deTai.getChuNhiem() != null) {
            notifyUser(deTai.getChuNhiem(), deTai, subject, message);
        }
    }

    private void notifyUser(NguoiDung recipient, DeTai deTai, String subject, String message) {
        emailService.guiEmail(recipient.getEmail(), subject, "generic", emailVariables(recipient, deTai, subject, message));
    }

    private Map<String, Object> emailVariables(NguoiDung recipient, DeTai deTai, String title, String message) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("recipientName", recipient.getHoTen());
        variables.put("title", title);
        variables.put("message", message);
        variables.put("maSo", deTai.getMaSo());
        variables.put("tenDeTai", deTai.getTenDeTai());
        return variables;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private PhanBienDeXuatResponse toPhanBienDeXuatResponse(PhanBienDeXuat entity) {
        return PhanBienDeXuatResponse.builder()
                .id(entity.getId())
                .hoTen(entity.getHoTen())
                .email(entity.getEmail())
                .coQuan(entity.getCoQuan())
                .build();
    }
}
