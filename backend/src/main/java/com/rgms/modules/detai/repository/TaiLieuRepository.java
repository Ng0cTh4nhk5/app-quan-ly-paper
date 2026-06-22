package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.TaiLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository TaiLieu — file đính kèm đề tài.
 * HoSoHopLeGuard dùng existsByDeTaiIdAndLoai() để kiểm tra có thuyết minh chưa.
 */
@Repository
public interface TaiLieuRepository extends JpaRepository<TaiLieu, UUID> {

    List<TaiLieu> findByDeTaiIdOrderByPhienBanDesc(UUID deTaiId);

    boolean existsByDeTaiIdAndLoai(UUID deTaiId, String loai);

    /**
     * Lấy phiên bản mới nhất của một loại file trong đề tài.
     * Dùng để tính phienBan tiếp theo khi upload (BR-14).
     */
    java.util.Optional<TaiLieu> findTopByDeTaiIdAndLoaiOrderByPhienBanDesc(UUID deTaiId, String loai);
}
