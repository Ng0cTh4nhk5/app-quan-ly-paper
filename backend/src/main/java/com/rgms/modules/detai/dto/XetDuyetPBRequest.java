package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class XetDuyetPBRequest {

    @NotBlank(message = "Quyết định xét duyệt là bắt buộc")
    private String quyetDinh;

    private String ghiChu;

    private String noiDungYeuCauSua;

    @FutureOrPresent(message = "Deadline nộp lại không được nằm trong quá khứ")
    private LocalDate deadlineNopLai;
}
