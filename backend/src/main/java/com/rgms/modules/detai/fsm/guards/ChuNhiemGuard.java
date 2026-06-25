package com.rgms.modules.detai.fsm.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Guard kiểm tra IDOR — người thực hiện phải là chủ nhiệm của đề tài.
 *
 * Áp dụng cho: upload file, chỉnh sửa thông tin đề tài, nộp lại hồ sơ bổ sung,
 * nộp lại thuyết minh, đồng ý hợp đồng.
 *
 * Tham chiếu: gd-1-1-khoi-tao.md F-GV-03 GUARD-2, F-GV-04 GUARD-1
 */
@Component
@RequiredArgsConstructor
public class ChuNhiemGuard implements TransitionGuard {

    private final DeTaiRepository deTaiRepository;

    @Override
    public void check(Long deTaiId, Long actorId) {
        var deTai = deTaiRepository.findById(deTaiId)
                .orElseThrow(() -> new BusinessException("GUARD_NOT_FOUND", "Không tìm thấy đề tài."));

        if (!deTai.getChuNhiem().getId().equals(actorId)) {
            throw new BusinessException("GUARD_IDOR",
                    "Bạn không có quyền thực hiện thao tác này. Chỉ chủ nhiệm đề tài mới được phép.");
        }
    }
}
