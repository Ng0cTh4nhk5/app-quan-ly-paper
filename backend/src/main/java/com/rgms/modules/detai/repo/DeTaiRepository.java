package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.DeTai;
import com.rgms.shared.enums.TopicState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeTaiRepository extends JpaRepository<DeTai, Long> {

    @Query(value = "SELECT nextval('de_tai_ma_so_seq')", nativeQuery = true)
    Long nextMaSoSequence();

    // ── Cho Scheduler (auto-treo) ────────────────────────────────────────────

    List<DeTai> findAllByStatus(TopicState status);

    // ── Cho ResearchTopicService.danhSach() — lọc theo trang_thai String ────
    // Spring Data map `trangThai` → field `status` (column `trang_thai`) qua @Enumerated
    // Cần dùng status (TopicState) thay vì String để type-safe.

    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByStatus(TopicState status, Pageable pageable);

    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByChuNhiem_Id(Long chuNhiemId, Pageable pageable);

    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByChuNhiem_IdAndStatus(Long chuNhiemId, TopicState status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Optional<DeTai> findById(Long id);

    /**
     * Tương thích ngược với code ResearchTopicService dùng String status.
     * @deprecated Dùng findByStatus(TopicState) thay thế.
     */
    @Deprecated
    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByTrangThai(String trangThai, Pageable pageable);

    /**
     * Tương thích ngược.
     * @deprecated Dùng findByChuNhiem_IdAndStatus thay thế.
     */
    @Deprecated
    @EntityGraph(attributePaths = {"chuNhiem", "chuNhiem.donVi", "kyNckh", "donVi"})
    Page<DeTai> findByChuNhiem_IdAndTrangThai(Long chuNhiemId, String trangThai, Pageable pageable);
}
