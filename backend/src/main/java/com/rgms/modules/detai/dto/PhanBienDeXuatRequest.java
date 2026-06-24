package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhanBienDeXuatRequest {

    @NotBlank(message = "Họ tên phản biện đề xuất là bắt buộc")
    private String hoTen;

    @Email(message = "Email phản biện đề xuất không hợp lệ")
    private String email;

    private String coQuan;
}
