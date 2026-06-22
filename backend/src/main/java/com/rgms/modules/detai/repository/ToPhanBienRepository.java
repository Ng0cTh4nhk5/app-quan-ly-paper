package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.ToPhanBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToPhanBienRepository extends JpaRepository<ToPhanBien, UUID> {

    Optional<ToPhanBien> findByDeTaiId(UUID deTaiId);

    /**
     * Đếm số thành viên trong tổ phản biện của một đề tài.
     * Guard PNCKH_LAP_TO_PB: phải >= 2 (Q3 trong gd-1-1-khoi-tao.md).
     */
    @Query("SELECT COUNT(tv) FROM ThanhVienToPhanBien tv WHERE tv.toPhanBien.deTai.id = :deTaiId")
    long countThanhVienByDeTaiId(@Param("deTaiId") UUID deTaiId);

    /**
     * Kiểm tra tất cả PB trong tổ đã nộp kết quả chưa.
     * Guard PNCKH_ACCEPT_PB: tất cả phải isDaNop() == true.
     */
    @Query("SELECT COUNT(tv) FROM ThanhVienToPhanBien tv " +
           "WHERE tv.toPhanBien.deTai.id = :deTaiId AND tv.ketQua = 'CHUA_NOP'")
    long countPhanBienChuaNopByDeTaiId(@Param("deTaiId") UUID deTaiId);
}
