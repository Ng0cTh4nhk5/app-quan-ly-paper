package com.rgms.modules.detai.event;

/**
 * Domain event: P.NCKH đã tiếp nhận hồ sơ.
 * Member B lắng nghe để gửi email xác nhận cho GV.
 */
public record HoSoTiepNhanEvent(Long deTaiId, Long nckhId) {}
