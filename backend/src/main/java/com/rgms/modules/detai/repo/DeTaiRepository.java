package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.DeTai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeTaiRepository extends JpaRepository<DeTai, Long> {

    long countByMaSoStartingWith(String prefix);

    @EntityGraph(attributePaths = {"chuNhiem", "kyNckh"})
    Page<DeTai> findByTrangThai(String trangThai, Pageable pageable);

    @EntityGraph(attributePaths = {"chuNhiem", "kyNckh"})
    Page<DeTai> findByChuNhiem_Id(Long chuNhiemId, Pageable pageable);

    @EntityGraph(attributePaths = {"chuNhiem", "kyNckh"})
    Page<DeTai> findByChuNhiem_IdAndTrangThai(Long chuNhiemId, String trangThai, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"chuNhiem", "kyNckh"})
    Page<DeTai> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"chuNhiem", "kyNckh"})
    Optional<DeTai> findById(Long id);
}
