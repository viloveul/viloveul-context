package com.viloveul.context.event.entity;

import java.util.Collection;

public class UpdatedEvent extends SavedEvent {
    public UpdatedEvent(Object entity) {
        super(entity, "UPDATED");
    }

    public UpdatedEvent(Object entity, Collection<String> properties, Collection<Object> states, Collection<Object> origins) {
        this(entity);
        this.setOrigins(origins);
        this.setStates(states);
        this.setProperties(properties);
    }
}
