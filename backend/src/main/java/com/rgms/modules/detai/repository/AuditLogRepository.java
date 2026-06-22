package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository AuditLog — Append-Only (BR-15).
 * Chỉ có save() và findBy*() — không expose delete/update methods.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Lấy toàn bộ lịch sử thao tác của một đề tài, sắp xếp mới nhất trước.
     * Dùng cho timeline view (F-GV-05).
     */
    List<AuditLog> findByDeTaiIdOrderByThoiGianDesc(UUID deTaiId);
}
