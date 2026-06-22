package com.rgms.modules.detai.repository;

import com.rgms.modules.detai.entity.KyNckh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository KyNckh — kỳ nghiên cứu khoa học.
 * Guard F-GV-01 GUARD-3: KyNCKH phải đang mở (trangThai = DANG_MO).
 */
@Repository
public interface KyNckhRepository extends JpaRepository<KyNckh, UUID> {

    List<KyNckh> findByTrangThai(String trangThai);

    List<KyNckh> findAllByOrderByNgayBatDauDangKyDesc();
}
