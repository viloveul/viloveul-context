package com.viloveul.context.event.entity;

import java.util.Collection;

public class DeletedEvent extends SavedEvent {
    public DeletedEvent(Object entity) {
        super(entity, "DELETED");
    }

    public DeletedEvent(Object entity, Collection<String> properties, Collection<Object> origins) {
        this(entity);
        this.setProperties(properties);
        this.setOrigins(origins);
    }
}
