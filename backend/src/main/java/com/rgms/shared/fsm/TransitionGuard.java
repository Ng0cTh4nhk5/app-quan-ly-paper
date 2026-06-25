package com.rgms.shared.fsm;

import com.rgms.exception.BusinessException;

/**
 * Contract cho tất cả guard condition trong FSM.
 *
 * Mỗi guard kiểm tra một điều kiện nghiệp vụ trước khi FsmService
 * cho phép chuyển trạng thái. Vi phạm → ném BusinessException ngay lập tức.
 *
 * Thiết kế:
 *   - Guard chỉ đọc (read-only query), không được ghi DB.
 *   - Thứ tự check do FsmService.transition() quyết định (thứ tự trong List<TransitionGuard>).
 *   - Guard không biết về event hay nextState — chỉ kiểm tra pre-condition.
 */
public interface TransitionGuard {

    /**
     * Kiểm tra điều kiện tiên quyết trước khi transition.
     *
     * @param deTaiId  Long ID đề tài đang được thao tác
     * @param actorId  Long ID người thực hiện hành động (dùng để check IDOR, role...)
     * @throws BusinessException nếu vi phạm guard — message phải rõ ràng cho frontend
     */
    void check(Long deTaiId, Long actorId);
}
