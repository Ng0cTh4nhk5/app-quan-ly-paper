package com.rgms.modules.detai.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO cho PNCKH yêu cầu bổ sung hồ sơ.
 */
@Getter
@Setter
public class YeuCauBoSungRequest {

    private String noiDungFeedback;

    private java.time.LocalDate deadline;
}
