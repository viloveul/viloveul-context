package com.viloveul.context.util.misc;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collection;

public class PageableResult<T extends Serializable> implements Serializable {

    @Getter
    protected final transient Collection<T> items;

    @Getter
    protected final Long total;

    @Getter
    protected final Integer pages;

    @Getter
    protected final Integer page;

    public PageableResult(Page<T> result) {
        this.items = result.getContent();
        this.pages = result.getTotalPages();
        this.total = result.getTotalElements();
        this.page = result.getNumber() + 1;
    }

    public PageableResult(Collection<T> items) {
        this.items = items;
        this.pages = 1;
        this.total = (long) items.size();
        this.page = 1;
    }

}
