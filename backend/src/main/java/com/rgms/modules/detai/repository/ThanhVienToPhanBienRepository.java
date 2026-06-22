package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.ThanhVienToPhanBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThanhVienToPhanBienRepository extends JpaRepository<ThanhVienToPhanBien, UUID> {

    List<ThanhVienToPhanBien> findByToPhanBienId(UUID toPhanBienId);

    /**
     * Kiểm tra một người dùng có phải thành viên của tổ PB thuộc đề tài này không.
     * Guard PB_NOP_KET_QUA: pbId phải thuộc tổ PB của deTaiId.
     */
    Optional<ThanhVienToPhanBien> findByToPhanBienDeTaiIdAndNguoiDungId(UUID deTaiId, UUID nguoiDungId);
}
