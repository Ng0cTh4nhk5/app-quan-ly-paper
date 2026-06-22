package com.rgms.modules.detai.event;

import java.util.UUID;

/**
 * Domain event: GV đã gửi hồ sơ đến P.NCKH thành công.
 *
 * Được publish bởi ResearchTopicService sau khi FSM transition thành công.
 * Member B lắng nghe event này để gửi email thông báo cho P.NCKH (loose coupling).
 *
 * Tham chiếu: sop-member-a.md — ResearchTopicService.guiHoSo()
 */
public record HoSoGuiEvent(UUID deTaiId, UUID gvId) {}
