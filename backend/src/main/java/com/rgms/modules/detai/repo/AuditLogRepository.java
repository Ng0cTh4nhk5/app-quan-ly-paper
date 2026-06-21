package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByDeTaiIdOrderByCreatedAtAsc(Long deTaiId);
}
