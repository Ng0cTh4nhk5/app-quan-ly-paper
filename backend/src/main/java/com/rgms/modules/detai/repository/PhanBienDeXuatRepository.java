package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.PhanBienDeXuat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository PhanBienDeXuat — danh sách phản biện GV đề xuất.
 * HoSoHopLeGuard dùng countByDeTaiId() để kiểm tra >= 2 người đề xuất.
 */
@Repository
public interface PhanBienDeXuatRepository extends JpaRepository<PhanBienDeXuat, UUID> {

    List<PhanBienDeXuat> findByDeTaiId(UUID deTaiId);

    long countByDeTaiId(UUID deTaiId);
}
