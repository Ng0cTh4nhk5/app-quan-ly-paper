package com.rgms.modules.detai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO cho PB nộp kết quả phản biện.
 */
@Getter
@Setter
public class PbNopKetQuaRequest {

    /** CHAP_NHAN | SUA_NHO | SUA_LON | TU_CHOI */
    private String ketQua;

    private String nhanXet;
}
