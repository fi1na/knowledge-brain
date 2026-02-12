package com.knowledgebrain.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Getter
public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public <E> PagedResponse(Page<E> page, Function<E, T> mapper) {
        this.content = page.getContent().stream().map(mapper).toList();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }

    private PagedResponse() {
        this.content = Collections.emptyList();
        this.page = 0;
        this.size = 0;
        this.totalElements = 0;
        this.totalPages = 0;
        this.first = true;
        this.last = true;
    }

    @SuppressWarnings("unchecked")
    public static <T> PagedResponse<T> empty() {
        return new PagedResponse<>();
    }
}