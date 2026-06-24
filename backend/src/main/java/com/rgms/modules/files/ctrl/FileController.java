package com.rgms.modules.files.ctrl;

import com.rgms.modules.files.dto.TaiLieuResponse;
import com.rgms.modules.files.mapper.TaiLieuMapper;
import com.rgms.modules.files.service.FileUploadService;
import com.rgms.shared.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "API upload và download tài liệu đính kèm của đề tài")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileUploadService fileUploadService;
    private final TaiLieuMapper taiLieuMapper;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GIANG_VIEN', 'NCKH')")
    @Operation(
            summary = "Upload file đính kèm",
            description = "Upload tài liệu cho một đề tài. API chấp nhận PDF, DOC, DOCX, JPEG, PNG với dung lượng tối đa 20MB, lưu file vào thư mục cấu hình theo dạng app.upload.dir/de-tai/{deTaiId}/ và tạo bản ghi tài liệu."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Upload file thành công",
                    content = @Content(schema = @Schema(implementation = TaiLieuResponse.class))),
            @ApiResponse(responseCode = "400", description = "File rỗng, quá dung lượng, sai MIME type hoặc loại tài liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Người dùng không có vai trò GIANG_VIEN hoặc NCKH"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề tài"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi lưu file")
    })
    public ResponseEntity<TaiLieuResponse> upload(
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "ID đề tài nhận tài liệu", required = true)
            @RequestParam Long deTaiId,
            @Parameter(description = "Loại tài liệu: THUYET_MINH, BIEU_MAU, KET_QUA_PB, HOP_DONG", required = true)
            @RequestParam String loai,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails currentUser) {
        var taiLieu = fileUploadService.save(file, deTaiId, loai, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(taiLieuMapper.toResponse(taiLieu));
    }

    @GetMapping("/download/{id}")
    @Operation(
            summary = "Download file đính kèm",
            description = "Tải file tài liệu theo ID. Response trả về stream nhị phân với Content-Type đọc từ file và Content-Disposition hỗ trợ tên file tiếng Việt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download file thành công",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập hoặc token không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi tài liệu hoặc file vật lý"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống khi đọc file")
    })
    public ResponseEntity<Resource> download(
            @Parameter(description = "ID tài liệu cần tải", required = true)
            @PathVariable Long id) {
        FileUploadService.DownloadFile downloadFile = fileUploadService.download(id);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadFile.getTaiLieu().getTenFile(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(downloadFile.getMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(downloadFile.getResource());
    }
}
