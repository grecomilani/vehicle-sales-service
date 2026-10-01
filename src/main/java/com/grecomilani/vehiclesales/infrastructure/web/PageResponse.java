package com.grecomilani.vehiclesales.infrastructure.web;

import com.grecomilani.vehiclesales.application.port.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <S, T> PageResponse<T> from(PageResult<S> result, Function<S, T> mapper) {
        PageResult<T> mapped = result.map(mapper);
        return new PageResponse<>(mapped.content(), mapped.page(), mapped.size(),
                mapped.totalElements(), mapped.totalPages());
    }
}
