package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.NguoiDung;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    @EntityGraph(attributePaths = "donVi")
    Optional<NguoiDung> findByUsername(String username);

    List<NguoiDung> findByIdIn(Collection<Long> ids);
}
