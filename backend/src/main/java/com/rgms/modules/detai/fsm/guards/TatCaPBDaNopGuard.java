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
 * Tham chiếu: er-diagram.md BR-07, state-machine.md T09.
 */
@Component
@RequiredArgsConstructor
public class TatCaPBDaNopGuard implements TransitionGuard {

    private final ToPhanBienRepository toPhanBienRepository;

    @Override
    public void check(Long deTaiId, Long actorId) {
        long soChuaNop = toPhanBienRepository.countPhanBienChuaNopByDeTaiId(deTaiId);
        if (soChuaNop > 0) {
            throw new BusinessException("GUARD_PB_CHUA_NOP",
                    "Còn " + soChuaNop + " thành viên trong tổ phản biện chưa nộp kết quả. " +
                    "Vui lòng đợi đủ kết quả trước khi xét duyệt.");
        }
    }
}
