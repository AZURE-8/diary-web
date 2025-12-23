package com.diaryweb.demo.dto;

import org.springframework.data.domain.Page;
import java.util.List;

//分页响应dto
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> p) {
        PageResponse<T> r = new PageResponse<>();
        r.content = p.getContent();
        r.page = p.getNumber();
        r.size = p.getSize();
        r.totalElements = p.getTotalElements();
        r.totalPages = p.getTotalPages();
        r.last = p.isLast(); // 是否是最后一页
        return r;
    }

    // getters
    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}
