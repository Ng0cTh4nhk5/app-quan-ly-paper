package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.DeTai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DeTaiRepository extends JpaRepository<DeTai, Long> {

    @Query(value = "SELECT nextval('de_tai_ma_so_seq')", nativeQuery = true)
    Long nextMaSoSequence();

    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByTrangThai(String trangThai, Pageable pageable);

    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByChuNhiem_Id(Long chuNhiemId, Pageable pageable);

    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByChuNhiem_IdAndTrangThai(Long chuNhiemId, String trangThai, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Optional<DeTai> findById(Long id);
}
