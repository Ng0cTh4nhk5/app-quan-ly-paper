package com.rgms.modules.detai.repo;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository NguoiDung used by the research-topic module.
 * The explicit bean name avoids colliding with the canonical auth repository.
 */
@Repository("detaiNguoiDungRepository")
public interface DetaiNguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    @EntityGraph(attributePaths = "donVi")
    Optional<NguoiDung> findByUsername(String username);

    @EntityGraph(attributePaths = "donVi")
    List<NguoiDung> findByVaiTro(String vaiTro);

    List<NguoiDung> findByIdIn(Collection<Long> ids);
}
