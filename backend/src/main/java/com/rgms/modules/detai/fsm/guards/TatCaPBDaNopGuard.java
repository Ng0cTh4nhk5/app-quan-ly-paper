package com.rgms.modules.detai.fsm.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.repo.ToPhanBienRepository;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Guard cho event PNCKH_ACCEPT_PB (P.NCKH chấp nhận kết quả phản biện).
 *
 * Điều kiện: Tất cả thành viên trong tổ phản biện phải đã nộp kết quả.
 * Nếu còn ai chưa nộp (ketQua = CHUA_NOP), không cho phép xét duyệt.
 *
 * FIX (edge case): nếu tổ phản biện chưa có ai (tongSo = 0),
 * guard phải báo lỗi thay vì pass (0 < 0 = false là sai về nghiệp vụ).
 *
 * Tham chiếu: er-diagram.md BR-07, state-machine.md T09.
 */
@Component
@RequiredArgsConstructor
public class TatCaPBDaNopGuard implements TransitionGuard {

    private final ToPhanBienRepository toPhanBienRepository;

    @Override
    public void check(Long deTaiId, Long actorId) {
        long tongSo   = toPhanBienRepository.countByDeTaiId(deTaiId);
        long soChuaNop = toPhanBienRepository.countPhanBienChuaNopByDeTaiId(deTaiId);

        // Edge case: tổ phản biện rỗng → không được phép xét duyệt
        if (tongSo == 0) {
            throw new BusinessException("GUARD_CHUA_CO_TO_PB",
                    "Chưa có tổ phản biện nào được lập. Không thể xét duyệt kết quả phản biện.");
        }

        // Guard chính: còn thành viên chưa nộp
        if (soChuaNop > 0) {
            throw new BusinessException("GUARD_PB_CHUA_NOP",
                    "Còn " + soChuaNop + " thành viên trong tổ phản biện chưa nộp kết quả. " +
                    "Vui lòng đợi đủ kết quả trước khi xét duyệt.");
        }
    }
}
