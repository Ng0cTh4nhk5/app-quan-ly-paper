package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.PhanBienDeXuat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhanBienDeXuatRepository extends JpaRepository<PhanBienDeXuat, Long> {

    long countByDeTaiId(Long deTaiId);
}
