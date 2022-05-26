package com.viloveul.context.util.validation;

import lombok.NoArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// https://www.baeldung.com/spring-mvc-custom-validator

@NoArgsConstructor
public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String field;

    private String match;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.match = constraintAnnotation.match();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(value).getPropertyValue(match);

        if (fieldValue != null) {
            return fieldValue.equals(fieldMatchValue);
        } else {
            return fieldMatchValue == null;
        }
    }
}
