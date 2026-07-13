package com.rgms.modules.detai.repo;

import com.rgms.modules.detai.entity.KyNCKH;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KyNCKHRepository extends JpaRepository<KyNCKH, Long> {
    /**
     * Tìm các kỳ NCKH theo trạng thái.
     * @param trangThai trạng thái kỳ NCKH (VD: "DANG_MO", "DA_DONG")
     * @return danh sách kỳ NCKH có trạng thái tương ứng
     */
    List<KyNCKH> findByTrangThai(String trangThai);
}
