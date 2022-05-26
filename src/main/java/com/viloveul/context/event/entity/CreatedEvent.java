package com.viloveul.context.event.entity;

import java.util.Collection;

public class CreatedEvent extends SavedEvent {
    public CreatedEvent(Object entity) {
        super(entity, "CREATED");
    }

    public CreatedEvent(Object entity, Collection<String> properties, Collection<Object> states) {
        this(entity);
        this.setProperties(properties);
        this.setStates(states);
    }
}
