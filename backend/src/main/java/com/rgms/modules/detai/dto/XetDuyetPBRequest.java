package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO cho PNCKH xét duyệt kết quả phản biện.
 */
@Data
public class XetDuyetPBRequest {

    /** CHAP_NHAN | CAN_SUA | TU_CHOI */
    @NotBlank(message = "Quyết định xét duyệt là bắt buộc")
    private String quyetDinh;

    private String ghiChu;

    /** Nội dung yêu cầu sửa (chỉ bắt buộc khi quyetDinh = CAN_SUA). */
    private String noiDungYeuCauSua;

    @FutureOrPresent(message = "Deadline nộp lại không được nằm trong quá khứ")
    private LocalDate deadlineNopLai;
}
