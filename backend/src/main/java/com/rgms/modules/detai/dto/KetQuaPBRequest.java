package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KetQuaPBRequest {

    @NotBlank(message = "Kết quả phản biện là bắt buộc")
    private String ketQua;

    @NotBlank(message = "Nhận xét phản biện là bắt buộc")
    private String nhanXet;
}
