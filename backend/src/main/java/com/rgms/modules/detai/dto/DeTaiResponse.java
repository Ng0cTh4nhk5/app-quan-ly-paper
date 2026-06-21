package com.rgms.modules.detai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeTaiResponse {

    private Long id;
    private String maSo;
    private String tenDeTai;
    private String trangThai;
    private String chuNhiem;
    private LocalDateTime updatedAt;
}
