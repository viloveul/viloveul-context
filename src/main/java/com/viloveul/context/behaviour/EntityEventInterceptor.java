package com.viloveul.context.behaviour;

import com.viloveul.context.event.entity.CreatedEvent;
import com.viloveul.context.event.entity.DeletedEvent;
import com.viloveul.context.event.entity.UpdatedEvent;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.context.ApplicationEventPublisher;

import java.io.Serializable;
import java.util.Arrays;

public class EntityEventInterceptor extends EmptyInterceptor {

    private final transient ApplicationEventPublisher publisher;

    private final transient Interceptor interceptor;

    public EntityEventInterceptor(ApplicationEventPublisher publisher, Interceptor interceptor) {
        this.publisher = publisher;
        this.interceptor = interceptor;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        this.publisher.publishEvent(new CreatedEvent(entity, Arrays.asList(propertyNames), Arrays.asList(state)));
        return this.interceptor.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        this.publisher.publishEvent(new DeletedEvent(entity, Arrays.asList(propertyNames), Arrays.asList(state)));
        this.interceptor.onDelete(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        this.publisher.publishEvent(new UpdatedEvent(entity, Arrays.asList(propertyNames), Arrays.asList(currentState), Arrays.asList(previousState)));
        return this.interceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }
}
