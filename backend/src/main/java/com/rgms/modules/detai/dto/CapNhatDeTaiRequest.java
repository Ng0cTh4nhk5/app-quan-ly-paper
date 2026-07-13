package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO cho cập nhật thông tin cơ bản đề tài (F-GV-03).
 * Chỉ cho phép khi đề tài ở trạng thái DRAFT.
 */
@Data
public class CapNhatDeTaiRequest {

    @NotBlank(message = "Tên đề tài không được để trống")
    @Size(max = 500, message = "Tên đề tài không được vượt quá 500 ký tự")
    private String tenDeTai;

    private String moTa;

    @Size(max = 255, message = "Lĩnh vực không được vượt quá 255 ký tự")
    private String linhVuc;
}
