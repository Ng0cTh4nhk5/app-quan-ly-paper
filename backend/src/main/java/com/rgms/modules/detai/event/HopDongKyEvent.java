package com.rgms.modules.detai.event;

import java.util.UUID;

/**
 * Domain event: Hợp đồng đã được ký hoàn tất.
 * Member B lắng nghe để gửi email xác nhận cho cả hai bên.
 */
public record HopDongKyEvent(UUID deTaiId, UUID nckhId) {}
