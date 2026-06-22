package com.rgms.modules.detai.fsm.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.repository.DeTaiRepository;
import com.rgms.modules.detai.repository.PhanBienDeXuatRepository;
import com.rgms.modules.detai.repository.TaiLieuRepository;
import com.rgms.shared.enums.TopicState;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Guard cho event GV_GUI_HO_SO — kiểm tra hồ sơ hợp lệ trước khi GV gửi đến P.NCKH.
 *
 * Bốn điều kiện (theo gd-1-1-khoi-tao.md F-GV-03):
 *   [1] Đề tài phải ở trạng thái DRAFT.
 *   [2] Người gửi phải là chủ nhiệm đề tài (IDOR protection — F-GV-04 GUARD-1).
 *   [3] Phải có ít nhất 1 file thuyết minh (loai = THUYET_MINH).
 *   [4] Phải có ít nhất 2 người phản biện đề xuất (Q3 trong gd-1-1-khoi-tao.md).
 */
@Component
@RequiredArgsConstructor
public class HoSoHopLeGuard implements TransitionGuard {

    private final DeTaiRepository deTaiRepository;
    private final TaiLieuRepository taiLieuRepository;
    private final PhanBienDeXuatRepository phanBienDeXuatRepository;

    @Override
    public void check(UUID deTaiId, UUID actorId) {

        var deTai = deTaiRepository.findByIdWithChuNhiem(deTaiId)
                .orElseThrow(() -> new BusinessException("GUARD_NOT_FOUND",
                        "Không tìm thấy đề tài."));

        // [1] Đề tài phải ở DRAFT
        if (deTai.getStatus() != TopicState.DRAFT) {
            throw new BusinessException("GUARD_WRONG_STATUS",
                    "Chỉ được gửi hồ sơ khi đề tài đang ở trạng thái DRAFT. " +
                    "Trạng thái hiện tại: " + deTai.getStatus());
        }

        // [2] Người gửi phải là chủ nhiệm (IDOR)
        if (!deTai.getChuNhiem().getId().equals(actorId)) {
            throw new BusinessException("GUARD_IDOR",
                    "Bạn không có quyền gửi hồ sơ cho đề tài này. " +
                    "Chỉ chủ nhiệm đề tài mới được thực hiện.");
        }

        // [3] Phải có ít nhất 1 file thuyết minh
        boolean hasThuyetMinh = taiLieuRepository.existsByDeTaiIdAndLoai(deTaiId, "THUYET_MINH");
        if (!hasThuyetMinh) {
            throw new BusinessException("GUARD_THIEU_THUYET_MINH",
                    "Hồ sơ chưa có file thuyết minh. Vui lòng upload thuyết minh trước khi gửi.");
        }

        // [4] Phải có ít nhất 2 người phản biện đề xuất
        long soPhanBienDeXuat = phanBienDeXuatRepository.countByDeTaiId(deTaiId);
        if (soPhanBienDeXuat < 2) {
            throw new BusinessException("GUARD_THIEU_PHAN_BIEN",
                    "Cần ít nhất 2 người phản biện đề xuất. Hiện có: " + soPhanBienDeXuat + ".");
        }
    }
}
