package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaoDeTaiRequest {

    @NotBlank(message = "Tên đề tài không được để trống")
    @Size(max = 500, message = "Tên đề tài không được vượt quá 500 ký tự")
    private String tenDeTai;

    private String moTa;

    @Size(max = 255, message = "Lĩnh vực không được vượt quá 255 ký tự")
    private String linhVuc;

    @NotNull(message = "Kỳ NCKH không được để trống")
    private Long kyNckhId;
}
