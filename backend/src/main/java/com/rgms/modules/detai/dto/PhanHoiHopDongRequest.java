package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhanHoiHopDongRequest {

    @NotNull(message = "Trạng thái đồng ý là bắt buộc")
    private Boolean dongY;

    private String noiDungGhiChu;
}
