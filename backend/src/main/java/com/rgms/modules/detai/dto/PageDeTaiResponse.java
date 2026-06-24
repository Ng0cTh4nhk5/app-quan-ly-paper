package com.rgms.modules.detai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDeTaiResponse {

    private List<DeTaiResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
}
