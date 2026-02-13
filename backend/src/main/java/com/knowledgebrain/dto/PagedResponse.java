package com.knowledgebrain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <E, T> PagedResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return PagedResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public static <T> PagedResponse<T> empty() {
        return PagedResponse.<T>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(0)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();
    }
}