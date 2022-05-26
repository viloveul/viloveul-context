package com.viloveul.context.util.helper;

import lombok.SneakyThrows;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class FieldHelper {

    private FieldHelper() {
        // not for initialize
    }

    public static Collection<Field> getClassFields(Class<?> clazz) {
        Map<String, Field> collections = new HashMap<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                collections.putIfAbsent(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        return collections.values();
    }

    @SneakyThrows
    public static <T> T fieldValue(Object result, String field, Class<T> tClass) {
        String[] ks = field.split("\\.");
        for (String k : ks) {
            PropertyDescriptor property = new PropertyDescriptor(k, result.getClass());
            result = property.getReadMethod().invoke(result);
        }
        return tClass.cast(result);
    }

    public static <T> Expression<String> fillFieldSelector(Path<T> root, String name) {
        String[] ks = name.split("\\.");
        Path<String> x = root.get(ks[0]);
        for (int i = 1; i < ks.length; i++) {
            x = x.get(ks[i]);
        }
        return x;
    }
}
