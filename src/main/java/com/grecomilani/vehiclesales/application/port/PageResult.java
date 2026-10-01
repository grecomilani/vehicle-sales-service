package com.grecomilani.vehiclesales.application.port;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados, independente do framework de persistência.
 *
 * @param content       itens da página
 * @param page          número da página (começando em 0)
 * @param size          tamanho máximo da página
 * @param totalElements total de itens em todas as páginas
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
