package com.viloveul.context.util.misc;

@FunctionalInterface
public interface DataPopulator {
    <T> T populate(T object);
}
