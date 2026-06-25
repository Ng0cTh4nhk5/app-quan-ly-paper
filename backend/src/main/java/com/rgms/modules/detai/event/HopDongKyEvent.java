package com.rgms.modules.detai.event;

/**
 * Domain event: Hợp đồng đã được ký hoàn tất.
 * Member B lắng nghe để gửi email xác nhận cho cả hai bên.
 */
public record HopDongKyEvent(Long deTaiId, Long nckhId) {}
