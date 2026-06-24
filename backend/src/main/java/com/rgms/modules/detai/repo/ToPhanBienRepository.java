package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.ToPhanBien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToPhanBienRepository extends JpaRepository<ToPhanBien, Long> {

    boolean existsByDeTaiId(Long deTaiId);

    Optional<ToPhanBien> findByDeTaiId(Long deTaiId);
}
