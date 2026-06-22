package com.rgms.modules.detai.event;

import java.util.UUID;

/**
 * Domain event: P.NCKH đã tiếp nhận hồ sơ.
 * Member B lắng nghe để gửi email xác nhận cho GV.
 */
public record HoSoTiepNhanEvent(UUID deTaiId, UUID nckhId) {}
