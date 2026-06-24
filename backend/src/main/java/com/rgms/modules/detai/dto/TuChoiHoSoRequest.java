package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TuChoiHoSoRequest {

    @NotBlank(message = "Lý do từ chối là bắt buộc")
    private String lyDo;
}
