package com.stayhub.backend.Common.DTO.Response;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponse<T>(
        int pageNo,
        int pageSize,
        int totalPage,
        long totalElements,
        List<T> items
) {
}
