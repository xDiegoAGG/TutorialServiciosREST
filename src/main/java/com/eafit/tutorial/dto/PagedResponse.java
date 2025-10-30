package com.eafit.tutorial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Respuesta paginada con metadatos")
public class PagedResponse<T> {

    @Schema(description = "Lista de elementos de la página actual")
    private List<T> content;

    @Schema(description = "Metadatos de paginación")
    private PageMetadata page;

    public PagedResponse() {}

    public PagedResponse(List<T> content, PageMetadata page) {
        this.content = content;
        this.page = page;
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        PageMetadata metadata = new PageMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PagedResponse<>(page.getContent(), metadata);
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public PageMetadata getPage() { return page; }
    public void setPage(PageMetadata page) { this.page = page; }

    @Schema(description = "Información de paginación")
    public static class PageMetadata {

        @Schema(description = "Número de página actual (base 0)", example = "0")
        private int number;

        @Schema(description = "Tamaño de página", example = "20")
        private int size;

        @Schema(description = "Total de elementos", example = "150")
        private long totalElements;

        @Schema(description = "Total de páginas", example = "8")
        private int totalPages;

        @Schema(description = "Es la primera página", example = "true")
        private boolean first;

        @Schema(description = "Es la última página", example = "false")
        private boolean last;

        @Schema(description = "Tiene página siguiente", example = "true")
        private boolean hasNext;

        @Schema(description = "Tiene página anterior", example = "false")
        private boolean hasPrevious;

        public PageMetadata() {}

        public PageMetadata(int number, int size, long totalElements, int totalPages,
                           boolean first, boolean last, boolean hasNext, boolean hasPrevious) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }

        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

        public boolean isHasPrevious() { return hasPrevious; }
        public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    }
}
