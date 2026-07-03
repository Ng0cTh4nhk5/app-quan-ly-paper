package com.rgms.modules.detai.fsm;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.entity.AuditLog;
import com.rgms.modules.detai.entity.DeTai;
import com.rgms.modules.detai.repo.AuditLogRepository;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.detai.repo.DetaiNguoiDungRepository;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.shared.enums.TopicEvent;
import com.rgms.shared.enums.TopicState;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FsmService — Bộ não State Machine của hệ thống RGMS.
 *
 * QUY TẮC BẤT BIẾN (KHÔNG ĐƯỢC VI PHẠM):
 *   1. Đây là ĐIỂM DUY NHẤT được phép thay đổi DeTai.status.
 *   2. Mọi thao tác khác (Controller, Service) KHÔNG được gọi deTai.setStatus() trực tiếp.
 *   3. Sau mỗi transition thành công, AuditLog PHẢI được ghi ngay lập tức (BR-15).
 *   4. Terminal state → không có outgoing transition → ném BusinessException.
 *
 * Luồng thực thi của transition():
 *   [Load DeTai] → [Check terminal] → [Run guards] → [Lookup nextState] → [Save] → [AuditLog]
 *
 * Tham chiếu:
 *   - sop-member-a.md Giai đoạn 2 — FsmService
 *   - state-machine.md Bảng 3. Bảng Mô Tả Transition
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FsmService {

    private final DeTaiRepository deTaiRepository;
    private final AuditLogRepository auditLogRepository;
    private final DetaiNguoiDungRepository nguoiDungRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Thực hiện một state transition.
     *
     * @param deTaiId  Long ID đề tài cần chuyển trạng thái
     * @param event    Sự kiện kích hoạt (TopicEvent)
     * @param actorId  Long ID người thực hiện (dùng để ghi AuditLog)
     * @return DeTai sau khi đã cập nhật trạng thái
     * @throws BusinessException nếu: đề tài không tồn tại, terminal state, event không hợp lệ
     */
    public DeTai transition(Long deTaiId, TopicEvent event, Long actorId) {

        // 1. Load đề tài
        DeTai deTai = deTaiRepository.findById(deTaiId)
                .orElseThrow(() -> new BusinessException("FSM_NOT_FOUND",
                        "Không tìm thấy đề tài với id: " + deTaiId));

        TopicState currentState = deTai.getStatus();

        // 2. Kiểm tra terminal state — không cho phép bất kỳ transition nào
        if (currentState.isTerminal()) {
            throw new BusinessException("FSM_TERMINAL_STATE",
                    "Đề tài đã kết thúc (trạng thái: " + currentState + "). Không thể thực hiện thêm thao tác.");
        }

        // 3. Tra bảng chuyển trạng thái (State × Event → NextState)
        TopicState nextState = resolveNextState(currentState, event);

        // 4. Cập nhật trạng thái — DUY NHẤT nơi được phép gọi setStatus()
        deTai.setStatus(nextState);
        DeTai saved = deTaiRepository.save(deTai);

        // 5. Ghi AuditLog (trong cùng transaction để đảm bảo consistency)
        NguoiDung actor = nguoiDungRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException("FSM_ACTOR_NOT_FOUND",
                        "Không tìm thấy người dùng với id: " + actorId));

        auditLogRepository.save(AuditLog.builder()
                .deTai(saved)
                .actor(actor)
                .hanhDong(event.name())
                .trangThaiTruoc(currentState.name())
                .trangThaiSau(nextState.name())
                .build());

        log.info("[FSM] DeTai={} | {} + {} → {} | Actor={}",
                deTaiId, currentState, event, nextState, actorId);

        return saved;
    }

    /**
     * Overload hỗ trợ guards — cho phép caller truyền thêm danh sách pre-condition.
     * Guards được chạy TRƯỚC khi tra bảng transition.
     */
    public DeTai transition(Long deTaiId, TopicEvent event, Long actorId, List<TransitionGuard> guards) {
        // Chạy tất cả guards theo thứ tự (fail-fast)
        for (TransitionGuard guard : guards) {
            guard.check(deTaiId, actorId);
        }
        return transition(deTaiId, event, actorId);
    }

    /**
     * Kiểm tra xem một event có hợp lệ với trạng thái hiện tại không — không throw exception.
     * Dùng để frontend hiển thị/ẩn các nút hành động.
     */
    @Transactional(readOnly = true)
    public boolean canTransition(Long deTaiId, TopicEvent event) {
        return deTaiRepository.findById(deTaiId)
                .map(deTai -> {
                    if (deTai.getStatus().isTerminal()) return false;
                    try {
                        resolveNextState(deTai.getStatus(), event);
                        return true;
                    } catch (BusinessException e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    // ── Bảng chuyển trạng thái (State × Event → NextState) ───────────────────

    /**
     * Tra bảng (State × Event) → NextState.
     * Khớp 100% với state-machine.md Bảng 3. Transition (Luồng 1 — Phase 1).
     *
     * @throws BusinessException nếu (state, event) không có trong bảng
     */
    private TopicState resolveNextState(TopicState current, TopicEvent event) {
        return switch (current) {

            // ── S0: DRAFT ────────────────────────────────────────────────────
            // T02: GV gửi hồ sơ → chờ PNCKH xem xét
            // E01: Hệ thống auto-treo khi Draft bỏ dở quá X ngày
            // E21: GV chủ động rút (chưa có tạm ứng → thẳng DA_RUT)
            case DRAFT -> switch (event) {
                case GV_GUI_HO_SO  -> TopicState.CHO_PNCKH_XEM_XET;
                case HE_THONG_TREO -> TopicState.BI_TREO;
                case GV_RUT        -> TopicState.DA_RUT;
                default -> invalidTransition(current, event);
            };

            // ── S1: CHO_PNCKH_XEM_XET ────────────────────────────────────────
            // T03: PNCKH tiếp nhận, bắt đầu xem xét
            case CHO_PNCKH_XEM_XET -> switch (event) {
                case PNCKH_TIEP_NHAN -> TopicState.DANG_XEM_XET_BOI_PNCKH;
                default -> invalidTransition(current, event);
            };

            // ── S2: DANG_XEM_XET_BOI_PNCKH ───────────────────────────────────
            // T04: PNCKH yêu cầu bổ sung → GV cần nộp lại trong deadline
            // T06: Hồ sơ hợp lệ → lập tổ phản biện
            // E03: PNCKH từ chối thẳng (hồ sơ quá sơ sài)
            // E21: GV rút trước khi ký HĐ
            case DANG_XEM_XET_BOI_PNCKH -> switch (event) {
                case PNCKH_YEU_CAU_BO_SUNG -> TopicState.CHO_BO_SUNG_HO_SO;
                case PNCKH_LAP_TO_PB       -> TopicState.DANG_PHAN_BIEN;
                case PNCKH_TU_CHOI         -> TopicState.BI_TU_CHOI;
                case GV_RUT                -> TopicState.DA_RUT;
                default -> invalidTransition(current, event);
            };

            // ── S3: CHO_BO_SUNG_HO_SO ────────────────────────────────────────
            // T05: GV nộp lại hồ sơ trong deadline → quay về xem xét
            // E02: Quá hạn bổ sung → auto-treo
            // E21: GV rút
            case CHO_BO_SUNG_HO_SO -> switch (event) {
                case GV_NOP_BO_SUNG -> TopicState.DANG_XEM_XET_BOI_PNCKH;
                case HE_THONG_TREO  -> TopicState.BI_TREO;
                case GV_RUT         -> TopicState.DA_RUT;
                default -> invalidTransition(current, event);
            };

            // ── S4: DANG_PHAN_BIEN ────────────────────────────────────────────
            // T07: PB yêu cầu sửa thuyết minh
            // T09: PB accept → lập hợp đồng
            // E05: Quá hạn chỉnh sửa thuyết minh → treo
            // E04: PB không chấp nhận → từ chối
            // E21: GV rút
            case DANG_PHAN_BIEN -> switch (event) {
                case PNCKH_YEU_CAU_SUA_TM -> TopicState.CHO_CHINH_SUA_THUYET_MINH;
                case PNCKH_ACCEPT_PB      -> TopicState.DANG_LAP_HOP_DONG;
                case PNCKH_TU_CHOI        -> TopicState.BI_TU_CHOI;
                case HE_THONG_TREO        -> TopicState.BI_TREO;
                case GV_RUT               -> TopicState.DA_RUT;
                default -> invalidTransition(current, event);
            };

            // ── S5: CHO_CHINH_SUA_THUYET_MINH ────────────────────────────────
            // T08: GV nộp lại thuyết minh → quay về phản biện
            // E06: Quá hạn → treo
            // E21: GV rút
            case CHO_CHINH_SUA_THUYET_MINH -> switch (event) {
                case GV_NOP_SUA_TM  -> TopicState.DANG_PHAN_BIEN;
                case HE_THONG_TREO  -> TopicState.BI_TREO;
                case GV_RUT         -> TopicState.DA_RUT;
                default -> invalidTransition(current, event);
            };

            // ── S6: DANG_LAP_HOP_DONG ────────────────────────────────────────
            // T10: Hai bên ký hợp đồng → bắt đầu thực hiện
            // E08: GV không phản hồi HĐ quá hạn → treo
            // E21: GV rút
            case DANG_LAP_HOP_DONG -> switch (event) {
                case HAI_BEN_KY_HD -> TopicState.DANG_THUC_HIEN;
                case HE_THONG_TREO -> TopicState.BI_TREO;
                case GV_RUT        -> TopicState.DA_RUT;
                default -> invalidTransition(current, event);
            };

            // ── S7: DANG_THUC_HIEN ───────────────────────────────────────────
            // Phase 2 sẽ thêm: GV_NOP_BAO_CAO → CHO_NGHIEM_THU
            case DANG_THUC_HIEN -> switch (event) {
                case HE_THONG_TREO -> TopicState.DA_HUY;   // E09: hủy khi không có tạm ứng
                case GV_RUT        -> TopicState.DA_RUT;   // E21: rút khi không có tạm ứng
                default -> invalidTransition(current, event);
            };

            // ── Terminal States — không có outgoing transition ─────────────────
            case BI_TREO, BI_TU_CHOI, DA_RUT, DA_HUY ->
                throw new BusinessException("FSM_TERMINAL_STATE",
                        "Trạng thái " + current + " là terminal — không có transition nào.");
        };
    }

    /**
     * Helper: throw lỗi nhất quán khi event không hợp lệ tại state hiện tại.
     */
    private TopicState invalidTransition(TopicState current, TopicEvent event) {
        throw new BusinessException("FSM_INVALID_TRANSITION",
                "Sự kiện [" + event + "] không hợp lệ khi đề tài đang ở trạng thái [" + current + "].");
    }
}
