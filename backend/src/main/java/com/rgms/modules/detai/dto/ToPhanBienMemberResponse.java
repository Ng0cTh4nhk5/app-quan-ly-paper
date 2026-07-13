package com.rgms.modules.detai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thành viên tổ phản biện cho FE guard logic.
 * FE sopDGuards.canActionsFor() cần: id, ketQua, nhanXet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToPhanBienMemberResponse {

    /** NguoiDung.id (thành viên PB) */
    private Long id;

    private String hoTen;

    /** CHUA_NOP | CHAP_NHAN | CAN_SUA | TU_CHOI */
    private String ketQua;

    private String nhanXet;
}
