package com.rgms.modules.detai.event;

/**
 * Domain event: P.NCKH đã lập tổ phản biện.
 * Member B lắng nghe để gửi email mời phản biện.
 */
public record ToPhanBienLapEvent(Long deTaiId, Long nckhId) {}
