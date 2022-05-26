package com.viloveul.context.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchTarget {

    String[] path();

    Condition condition() default Condition.EQUAL;

    Option[] option() default {};

    enum Condition {
        LIKE, LIST, SIZE, NULL, EQUAL
    }

    enum Option {
        SENSITIVE, NEGATION
    }
}
