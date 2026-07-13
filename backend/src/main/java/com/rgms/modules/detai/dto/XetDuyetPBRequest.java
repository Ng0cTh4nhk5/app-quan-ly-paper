package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO cho PNCKH xét duyệt kết quả phản biện.
 *
 * Lưu ý phân biệt:
 * - "CAN_SUA": là kết quả do thành viên tổ phản biện đánh giá (result code)
 * - "YEU_CAU_SUA": là quyết định của PNCKH sau khi xét duyệt (decision code)
 */
@Data
public class XetDuyetPBRequest {

    /**
     * Quyết định của PNCKH: CHAP_NHAN | YEU_CAU_SUA | TU_CHOI
     * (Không phải "CAN_SUA" - đó là result code của phản biện viên)
     */
    @NotBlank(message = "Quyết định xét duyệt là bắt buộc")
    private String quyetDinh;

    private String ghiChu;

    /** Nội dung yêu cầu sửa (chỉ bắt buộc khi quyetDinh = YEU_CAU_SUA). */
    private String noiDungYeuCauSua;

    @FutureOrPresent(message = "Deadline nộp lại không được nằm trong quá khứ")
    private LocalDate deadlineNopLai;
}
