package com.stayhub.backend.Common.Util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static Pageable getPageable(int page, int size, String sortBy, String sortDir, String defaultSortBy) {
        int pageNumber = (page > 0) ? page - 1 : 0;

        String sortField = (sortBy == null || sortBy.trim().isEmpty()) ? defaultSortBy : sortBy;
        String direction = (sortDir == null || sortDir.trim().isEmpty()) ? "desc" : sortDir;

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        return PageRequest.of(pageNumber, size, sort);
    }

    public static Pageable getPageable(int page, int size, String sortBy, String sortDir) {
        return getPageable(page, size, sortBy, sortDir, "id");
    }

    public static Pageable getPageable(int page, int size, String defaultSortBy) {
        return getPageable(page, size, null, null, defaultSortBy);
    }

    public static Pageable getPageable(int page, int size) {
        return getPageable(page, size, "id");
    }
}
