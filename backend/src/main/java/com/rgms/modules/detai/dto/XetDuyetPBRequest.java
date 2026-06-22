package com.rgms.modules.detai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO cho PNCKH xét duyệt kết quả phản biện.
 */
@Getter
@Setter
public class XetDuyetPBRequest {

    /** CHAP_NHAN | CAN_SUA | TU_CHOI */
    private String quyetDinh;

    private String ghiChu;
}
