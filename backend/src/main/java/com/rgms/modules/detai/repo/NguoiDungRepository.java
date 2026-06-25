package com.rgms.modules.detai.repo;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository NguoiDung — dùng trong module detai (FsmService, ResearchTopicService).
 * Tham chiếu canonical entity từ nguoidung.entity.
 */
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    @EntityGraph(attributePaths = "donVi")
    Optional<NguoiDung> findByUsername(String username);

    @EntityGraph(attributePaths = "donVi")
    List<NguoiDung> findByVaiTro(String vaiTro);

    List<NguoiDung> findByIdIn(Collection<Long> ids);
}
