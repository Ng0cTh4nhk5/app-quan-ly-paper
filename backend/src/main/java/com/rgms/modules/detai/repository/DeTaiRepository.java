package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.DeTai;
import com.rgms.shared.enums.TopicState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository DeTai — phần Member A chỉ định nghĩa query cần cho FSM và guards.
 * API endpoint queries sẽ được Member B bổ sung.
 */
@Repository
public interface DeTaiRepository extends JpaRepository<DeTai, UUID> {

    /**
     * Dùng trong FsmService — load kèm chuNhiem để tránh LazyInitializationException khi check IDOR.
     */
    @Query("SELECT d FROM DeTai d JOIN FETCH d.chuNhiem WHERE d.id = :id")
    Optional<DeTai> findByIdWithChuNhiem(@Param("id") UUID id);

    /**
     * Lấy tất cả đề tài đang ở trạng thái cụ thể — dùng cho scheduler auto-treo (E1, E2, E6, E8).
     */
    List<DeTai> findAllByStatus(TopicState status);

    /**
     * Kiểm tra GV đã có đề tài ở trạng thái DRAFT với cùng kỳ NCKH chưa.
     * (Business Rule Q2: GV có thể tạo nhiều đề tài — không giới hạn, nhưng cần thống kê)
     */
    long countByChuNhiemIdAndKyNckhId(UUID chuNhiemId, UUID kyNckhId);

    /**
     * Sinh số thứ tự mã đề tài trong năm — tránh race condition bằng PostgreSQL SEQUENCE.
     * Xem: V2__add_ma_so_sequence.sql
     */
    @Query(value = "SELECT nextval('de_tai_ma_so_seq')", nativeQuery = true)
    long nextMaSoSequence();
}
