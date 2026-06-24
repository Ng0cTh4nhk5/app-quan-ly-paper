package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findFirstByDeTaiIdAndLoaiAndTrangThaiOrderByCreatedAtDesc(
            Long deTaiId,
            String loai,
            String trangThai);
}
