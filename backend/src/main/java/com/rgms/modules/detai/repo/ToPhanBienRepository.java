package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.ToPhanBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ToPhanBienRepository extends JpaRepository<ToPhanBien, Long> {

    boolean existsByDeTaiId(Long deTaiId);

    Optional<ToPhanBien> findByDeTaiId(Long deTaiId);

    /**
     * Đếm số thành viên trong tổ phản biện của đề tài — dùng cho ToPhanBienHopLeGuard.
     */
    @Query("SELECT COUNT(tv) FROM ThanhVienToPhanBien tv WHERE tv.toPhanBien.deTai.id = :deTaiId")
    long countThanhVienByDeTaiId(@Param("deTaiId") Long deTaiId);

    /**
     * Đếm số thành viên chưa nộp kết quả — dùng cho TatCaPBDaNopGuard.
     */
    @Query("SELECT COUNT(tv) FROM ThanhVienToPhanBien tv " +
           "WHERE tv.toPhanBien.deTai.id = :deTaiId AND tv.ketQua = 'CHUA_NOP'")
    long countPhanBienChuaNopByDeTaiId(@Param("deTaiId") Long deTaiId);
}
