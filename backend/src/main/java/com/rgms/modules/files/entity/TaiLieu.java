package com.rgms.modules.files.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tai_lieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "de_tai_id", nullable = false)
    private Long deTaiId;

    @Column(name = "ten_file", nullable = false)
    private String tenFile;

    @Column(name = "loai_file", nullable = false, length = 50)
    private String loaiFile;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Builder.Default
    @Column(name = "phien_ban", nullable = false)
    private Integer phienBan = 1;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        if (phienBan == null) {
            phienBan = 1;
        }
    }
}
