package com.rgms.modules.detai.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SoanHopDongRequest {

    @NotNull(message = "Kinh phí là bắt buộc")
    @Positive(message = "Kinh phí phải lớn hơn 0")
    private BigDecimal kinhPhi;

    @NotNull(message = "Ngày bắt đầu là bắt buộc")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc là bắt buộc")
    private LocalDate ngayKetThuc;

    @NotNull(message = "Tỷ lệ tạm ứng là bắt buộc")
    @DecimalMin(value = "0.0", message = "Tỷ lệ tạm ứng phải từ 0 đến 1")
    @DecimalMax(value = "1.0", message = "Tỷ lệ tạm ứng phải từ 0 đến 1")
    private BigDecimal tyLeTamUng;
}
