package com.rgms.modules.files.service;

import com.rgms.exception.BusinessException;
import com.rgms.exception.ResourceNotFoundException;
import com.rgms.modules.detai.repo.DeTaiRepository;
import com.rgms.modules.files.entity.TaiLieu;
import com.rgms.modules.files.repo.TaiLieuRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final long MAX_SIZE = 20 * 1024 * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png"
    );
    private static final Set<String> ALLOWED_LOAI = Set.of(
            "THUYET_MINH",
            "BIEU_MAU",
            "KET_QUA_PB",
            "HOP_DONG"
    );

    private final TaiLieuRepository taiLieuRepository;
    private final DeTaiRepository deTaiRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public TaiLieu save(MultipartFile file, Long deTaiId, String loai, Long uploaderId) {
        validateUpload(file, loai);
        if (!deTaiRepository.existsById(deTaiId)) {
            throw new ResourceNotFoundException("Đề tài", "id", deTaiId);
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path folderPath = uploadPath.resolve("de-tai").resolve(String.valueOf(deTaiId)).normalize();
        Path targetPath = folderPath.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(folderPath)) {
            throw new BusinessException("Đường dẫn file không hợp lệ.", HttpStatus.BAD_REQUEST);
        }

        try {
            Files.createDirectories(folderPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Lỗi khi lưu file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        int nextVersion = taiLieuRepository.findFirstByDeTaiIdAndLoaiFileOrderByPhienBanDesc(deTaiId, loai)
                .map(TaiLieu::getPhienBan)
                .map(current -> current + 1)
                .orElse(1);

        TaiLieu taiLieu = TaiLieu.builder()
                .deTaiId(deTaiId)
                .tenFile(originalFilename)
                .loaiFile(loai)
                .filePath(targetPath.toString())
                .phienBan(nextVersion)
                .uploadedBy(uploaderId)
                .build();

        return taiLieuRepository.save(taiLieu);
    }

    @Transactional(readOnly = true)
    public DownloadFile download(Long id) {
        TaiLieu taiLieu = taiLieuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu", "id", id));
        Path filePath = Paths.get(taiLieu.getFilePath()).toAbsolutePath().normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File", "path", taiLieu.getFilePath());
            }

            String probedType = Files.probeContentType(filePath);
            MediaType mediaType = StringUtils.hasText(probedType)
                    ? MediaType.parseMediaType(probedType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return new DownloadFile(taiLieu, resource, mediaType);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "path", taiLieu.getFilePath());
        } catch (IOException e) {
            throw new BusinessException("Lỗi khi đọc file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateUpload(MultipartFile file, String loai) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file tải lên.", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("File vượt quá giới hạn 20MB.", HttpStatus.BAD_REQUEST);
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Định dạng file không được hỗ trợ.", HttpStatus.BAD_REQUEST);
        }
        if (!ALLOWED_LOAI.contains(loai)) {
            throw new BusinessException("Loại tài liệu không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
    }

    private String sanitizeFilename(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "file" : originalFilename);
        cleaned = cleaned.replace("\\", "_").replace("/", "_");
        if (!StringUtils.hasText(cleaned) || cleaned.contains("..")) {
            throw new BusinessException("Tên file không hợp lệ.", HttpStatus.BAD_REQUEST);
        }
        return cleaned;
    }

    @Getter
    @RequiredArgsConstructor
    public static class DownloadFile {
        private final TaiLieu taiLieu;
        private final Resource resource;
        private final MediaType mediaType;
    }
}
