package com.rgms.modules.detai.event;

import java.util.UUID;

/**
 * Domain event: P.NCKH đã lập tổ phản biện.
 * Member B lắng nghe để gửi email mời phản biện.
 */
public record ToPhanBienLapEvent(UUID deTaiId, UUID nckhId) {}
