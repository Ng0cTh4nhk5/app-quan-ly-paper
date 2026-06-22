package com.rgms.modules.detai.fsm.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.repository.DeTaiRepository;
import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.nguoidung.model.Role;
import com.rgms.modules.nguoidung.repository.NguoiDungRepository;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
    public void check(UUID deTaiId, UUID actorId) {
        var deTai = deTaiRepository.findByIdWithChuNhiem(deTaiId)
                .orElseThrow(() -> new BusinessException("GUARD_NOT_FOUND", "Không tìm thấy đề tài."));

        if (!deTai.getChuNhiem().getId().equals(actorId)) {
            throw new BusinessException("GUARD_IDOR",
                    "Bạn không có quyền thực hiện thao tác này. Chỉ chủ nhiệm đề tài mới được phép.");
        }
    }
}
