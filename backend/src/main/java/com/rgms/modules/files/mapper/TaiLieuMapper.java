package com.rgms.modules.files.mapper;

import com.rgms.modules.files.dto.TaiLieuResponse;
import com.rgms.modules.files.entity.TaiLieu;
import org.springframework.stereotype.Component;

@Component
public class TaiLieuMapper {

    public TaiLieuResponse toResponse(TaiLieu taiLieu) {
        return TaiLieuResponse.builder()
                .id(taiLieu.getId())
                .tenFile(taiLieu.getTenFile())
                .loai(taiLieu.getLoaiFile())
                .downloadUrl("/api/v1/files/download/" + taiLieu.getId())
                .phienBan(taiLieu.getPhienBan())
                .uploadedAt(taiLieu.getUploadedAt())
                .build();
    }
}
