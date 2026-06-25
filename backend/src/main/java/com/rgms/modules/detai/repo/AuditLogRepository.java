package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // AuditLog.deTai là ManyToOne entity, Spring Data sẽ tự join deTai.id
    // AuditLog.thoiGian (không phải createdAt)
    List<AuditLog> findByDeTai_IdOrderByThoiGianAsc(Long deTaiId);
}
