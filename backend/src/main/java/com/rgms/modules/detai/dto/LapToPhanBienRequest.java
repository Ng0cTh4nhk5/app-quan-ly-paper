package com.rgms.modules.detai.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO cho lập tổ phản biện (F-PNCKH-03).
 */
@Getter
@Setter
public class LapToPhanBienRequest {

    private LocalDate deadlineNop;

    private List<UUID> danhSachPhanBienId;
}
