package com.viloveul.context.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

public abstract class AbstractComponent {

    @Autowired
    protected Environment environment;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;
}
