package com.rgms.modules.detai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhanBienDeXuatResponse {

    private Long id;
    private String hoTen;
    private String email;
    private String coQuan;
}
