package com.rgms.modules.files.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaiLieuResponse {

    private Long id;
    private String tenFile;
    private String loai;
    private String downloadUrl;
    private Integer phienBan;
    private LocalDateTime uploadedAt;
}
