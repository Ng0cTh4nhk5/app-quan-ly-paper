package com.rgms.modules.nguoidung.repository;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository canonical cho NguoiDung — dùng trong module Auth/Security.
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    Optional<NguoiDung> findByUsername(String username);

    boolean existsByEmail(String email);
}
