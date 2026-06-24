package com.rgms.modules.detai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

    private String action;
    private String actor;
    private String tuTrangThai;
    private String sangTrangThai;
    private String severity;
    private String ghiChu;
    private LocalDateTime createdAt;
}
