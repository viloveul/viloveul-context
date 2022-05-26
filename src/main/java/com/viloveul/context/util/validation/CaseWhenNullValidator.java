package com.viloveul.context.util.validation;

import lombok.NoArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@NoArgsConstructor
public class CaseWhenNullValidator implements ConstraintValidator<CaseWhenNull, Object> {

    private String caseField;

    private String whenMatch;

    private String thenUse;

    private String orElse;

    @Override
    public void initialize(CaseWhenNull constraintAnnotation) {
        this.caseField = constraintAnnotation.caseField();
        this.whenMatch = constraintAnnotation.whenMatch();
        this.thenUse = constraintAnnotation.thenUse();
        this.orElse = constraintAnnotation.orElse();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        Object caseFieldValue = new BeanWrapperImpl(value).getPropertyValue(this.caseField);

        if (this.whenMatch.equals(caseFieldValue)) {
            return (new BeanWrapperImpl(value).getPropertyValue(this.thenUse)) != null;
        } else {
            return (new BeanWrapperImpl(value).getPropertyValue(this.orElse)) != null;
        }
    }
}
