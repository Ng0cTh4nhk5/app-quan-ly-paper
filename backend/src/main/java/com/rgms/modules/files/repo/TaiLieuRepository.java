package com.rgms.modules.files.repo;

import com.rgms.modules.files.entity.TaiLieu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaiLieuRepository extends JpaRepository<TaiLieu, Long> {

    List<TaiLieu> findByDeTaiIdOrderByUploadedAtDesc(Long deTaiId);

    Optional<TaiLieu> findFirstByDeTaiIdAndLoaiFileOrderByPhienBanDesc(Long deTaiId, String loaiFile);

    boolean existsByDeTaiIdAndLoaiFile(Long deTaiId, String loaiFile);

    boolean existsByDeTaiIdAndLoaiFileAndUploadedAtAfter(Long deTaiId, String loaiFile, LocalDateTime uploadedAt);
}
