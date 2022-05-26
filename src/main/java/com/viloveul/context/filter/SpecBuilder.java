package com.viloveul.context.filter;

import com.viloveul.context.util.helper.FieldHelper;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class SpecBuilder {

    private SpecBuilder() {
        throw new IllegalStateException("SpecBuilder class");
    }

    public static <T> Specification<T> byField(String field, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(FieldHelper.fillFieldSelector(root, field), value);
    }

    public static <T> Specification<T> byId(String value) {
        return byField("id", value);
    }

    public static <T> Specification<T> inValue(String field, Collection<Object> values) {
        return (root, criteriaQuery, criteriaBuilder) -> values.isEmpty() ? criteriaBuilder.disjunction() : FieldHelper.fillFieldSelector(root, field).in(values);
    }

}
