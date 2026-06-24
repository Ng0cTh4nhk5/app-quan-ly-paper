package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThanhVienToPhanBienRepository extends JpaRepository<ThanhVienToPhanBien, Long> {

    @EntityGraph(attributePaths = {"toPhanBien", "nguoiDung"})
    Optional<ThanhVienToPhanBien> findByToPhanBien_DeTaiIdAndNguoiDung_Id(Long deTaiId, Long nguoiDungId);

    @EntityGraph(attributePaths = {"toPhanBien", "nguoiDung"})
    List<ThanhVienToPhanBien> findByToPhanBien_Id(Long toPhanBienId);
}
