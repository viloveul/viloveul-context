package com.viloveul.context.filter;

import lombok.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SearchProperties {

    @NonNull
    String resource();

    @NonNull
    String operation();

    Allow[] allows() default {};

    boolean customizer() default false;

    @interface Allow {

        Option option();

        String field();
    }

    enum Option {
        USER, GROUP
    }
}
