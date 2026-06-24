package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO cho lập tổ phản biện (F-PNCKH-03).
 */
@Data
public class LapToPhanBienRequest {

    @NotEmpty(message = "Danh sách thành viên phản biện không được rỗng")
    private List<Long> thanhVienIds;

    @NotNull(message = "Deadline nộp phản biện là bắt buộc")
    @FutureOrPresent(message = "Deadline nộp phản biện không được nằm trong quá khứ")
    private LocalDate deadlineNop;
}
