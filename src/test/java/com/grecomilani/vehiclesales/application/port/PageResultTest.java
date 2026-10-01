package com.grecomilani.vehiclesales.application.port;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResultTest {

    @Test
    void shouldCalculateTotalPages() {
        assertThat(new PageResult<>(List.of(), 0, 20, 0).totalPages()).isZero();
        assertThat(new PageResult<>(List.of(), 0, 20, 20).totalPages()).isEqualTo(1);
        assertThat(new PageResult<>(List.of(), 0, 20, 21).totalPages()).isEqualTo(2);
    }

    @Test
    void shouldMapContentKeepingPaginationData() {
        PageResult<Integer> page = new PageResult<>(List.of(1, 2), 3, 2, 8);

        PageResult<String> mapped = page.map(value -> "v" + value);

        assertThat(mapped.content()).containsExactly("v1", "v2");
        assertThat(mapped.page()).isEqualTo(3);
        assertThat(mapped.size()).isEqualTo(2);
        assertThat(mapped.totalElements()).isEqualTo(8);
        assertThat(mapped.totalPages()).isEqualTo(4);
    }

    @Test
    void contentShouldBeImmutableCopy() {
        List<String> source = new ArrayList<>(List.of("a"));
        PageResult<String> page = new PageResult<>(source, 0, 10, 1);

        source.add("b");

        assertThat(page.content()).containsExactly("a");
        assertThatThrownBy(() -> page.content().add("c")).isInstanceOf(UnsupportedOperationException.class);
    }
}
