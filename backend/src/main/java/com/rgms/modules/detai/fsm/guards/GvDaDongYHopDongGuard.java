package com.rgms.modules.detai.fsm.guards;

import com.rgms.exception.BusinessException;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.shared.fsm.TransitionGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Guard cho event HAI_BEN_KY_HD (Ký hợp đồng hoàn tất).
 *
 * Điều kiện: GV phải đã đồng ý nội dung hợp đồng (gvDaDongYHopDong = true)
 * trước khi P.NCKH xác nhận ký.
 *
 * Tham chiếu: state-machine.md T10, sop-member-a.md Giai đoạn 3.
 */
@Component
@RequiredArgsConstructor
public class GvDaDongYHopDongGuard implements TransitionGuard {

    private final DeTaiRepository deTaiRepository;

    @Override
    public void check(Long deTaiId, Long actorId) {
        var deTai = deTaiRepository.findById(deTaiId)
                .orElseThrow(() -> new BusinessException("GUARD_NOT_FOUND", "Không tìm thấy đề tài."));

        if (Boolean.FALSE.equals(deTai.getGvDaDongYHopDong())) {
            throw new BusinessException("GUARD_GV_CHUA_DONG_Y_HD",
                    "Giảng viên chưa xác nhận đồng ý nội dung hợp đồng. " +
                    "Vui lòng đợi GV xem và đồng ý trước khi ký.");
        }
    }
}
