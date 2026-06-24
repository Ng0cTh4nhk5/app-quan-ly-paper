package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.HopDong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HopDongRepository extends JpaRepository<HopDong, Long> {

    boolean existsByDeTaiId(Long deTaiId);

    Optional<HopDong> findByDeTaiId(Long deTaiId);
}
