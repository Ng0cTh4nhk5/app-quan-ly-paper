package com.rgms.modules.detai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin kỳ NCKH cho dropdown chọn kỳ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KyNCKHResponse {

    private Long id;

    /** tenKy entity → ten DTO (FE dùng k.ten) */
    private String ten;

    private String trangThai;
}
