package com.rgms.modules.detai.fsm.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.repository.ToPhanBienRepository;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Guard cho event PNCKH_LAP_TO_PB.
 *
 * Kiểm tra điều kiện trước khi P.NCKH lập tổ phản biện:
 *   1. Tổ phản biện phải có ít nhất 2 thành viên đã được thêm vào.
 *
 * Tham chiếu: er-diagram.md BR-07, gd-1-1-khoi-tao.md Q3
 */
@Component
@RequiredArgsConstructor
public class ToPhanBienHopLeGuard implements TransitionGuard {

    private final ToPhanBienRepository toPhanBienRepository;

    @Override
    public void check(UUID deTaiId, UUID actorId) {
        long soThanhVien = toPhanBienRepository.countThanhVienByDeTaiId(deTaiId);
        if (soThanhVien < 2) {
            throw new BusinessException("GUARD_THIEU_THANH_VIEN_PB",
                    "Tổ phản biện cần ít nhất 2 thành viên. Hiện có: " + soThanhVien + ".");
        }
    }
}
