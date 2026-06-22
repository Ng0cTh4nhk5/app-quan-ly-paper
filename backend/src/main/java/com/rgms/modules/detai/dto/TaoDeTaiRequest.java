package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Request DTO cho tạo đề tài mới (F-GV-01).
 */
@Getter
@Setter
public class TaoDeTaiRequest {

    @NotBlank(message = "Tên đề tài không được rỗng.")
    private String tenDeTai;

    private String moTa;

    private String linhVuc;

    @NotNull(message = "Kỳ NCKH không được rỗng.")
    private UUID kyNckhId;
}
