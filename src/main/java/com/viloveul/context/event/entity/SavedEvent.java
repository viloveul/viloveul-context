package com.viloveul.context.event.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

@Getter
public class SavedEvent {

    private final Object entity;

    private final String action;

    @Setter
    protected Collection<Object> origins = new ArrayList<>();

    @Setter
    protected Collection<Object> states = new ArrayList<>();

    @Setter
    protected Collection<String> properties = new ArrayList<>();

    public SavedEvent(Object entity, String action) {
        this.entity = entity;
        this.action = action.toUpperCase(Locale.ROOT);
    }
}
