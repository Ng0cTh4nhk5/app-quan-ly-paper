package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class YeuCauBoSungRequest {

    @NotBlank(message = "Nội dung yêu cầu bổ sung là bắt buộc")
    private String noiDung;

    @NotNull(message = "Deadline phản hồi là bắt buộc")
    @FutureOrPresent(message = "Deadline phản hồi không được nằm trong quá khứ")
    private LocalDate deadlinePhanHoi;
}
