package com.rgms.modules.nguoidung.repository;

import com.rgms.modules.nguoidung.entity.NguoiDung;
import com.rgms.modules.nguoidung.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, UUID> {

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByEmail(String email);
}
