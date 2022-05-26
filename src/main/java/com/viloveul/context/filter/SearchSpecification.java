package com.viloveul.context.filter;

import com.viloveul.context.util.helper.FieldHelper;
import com.viloveul.context.util.helper.GeneralHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class SearchSpecification<T> implements Specification<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchSpecification.class);

    @JsonIgnore
    protected Configuration configuration;

    @JsonIgnore
    protected transient Collection<Predicate> predicates = new ArrayList<>();

    @JsonIgnore
    protected transient Collection<Field> fields = new ArrayList<>();

    @JsonIgnore
    private final transient Collection<Specification<T>> specifications = new ArrayList<>();

    protected SearchSpecification() {
        this(
            new Configuration(
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            )
        );
    }

    protected SearchSpecification(Configuration configuration) {
        this.configuration = configuration;
        Collection<Field> tmps = FieldHelper.getClassFields(this.getClass());
        for (Field field : tmps) {
            if (field.isAnnotationPresent(SearchTarget.class)) {
                this.fields.add(field);
            }
        }
    }

    public void joinSpecification(Specification<T> specification) {
        this.specifications.add(specification);
    }

    @SneakyThrows
    @Override
    public final Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        for (Field field : this.fields) {
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), this.getClass());
                Object v = propertyDescriptor.getReadMethod().invoke(this);
                if (v != null) {
                    SearchTarget target = field.getAnnotation(SearchTarget.class);
                    this.parsePredicate(root, cb, target, field.getType(), v);
                }
            } catch (IllegalAccessException | ParseException e) {
                LOGGER.warn(e.getMessage(), e.getCause());
            }
        }

        Collection<Predicate> innerPredicates = new ArrayList<>();
        innerPredicates.add(cb.and(this.predicates.toArray(new Predicate[0])));
        for (Specification<T> specification : this.specifications) {
            innerPredicates.add(specification.toPredicate(root, query, cb));
        }
        return cb.and(innerPredicates.toArray(new Predicate[0]));
    }

    protected Predicate newLikePredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object v,
        Boolean sensitive,
        Boolean negation
    ) {
        Collection<Predicate> predicateCollection = new ArrayList<>();
        String value = Boolean.TRUE.equals(sensitive) ?
            String.valueOf(v) :
                String.valueOf(v).toLowerCase(this.configuration.locale);
        String[] values = value.split("%");
        for (Expression<String> expression : expressions) {
            Collection<Predicate> orCollection = new ArrayList<>();
            for (String val : values) {
                orCollection.add(
                    Boolean.TRUE.equals(negation) ?
                    cb.notLike(expression.as(String.class), "%" + val + "%") :
                    cb.like(expression.as(String.class), "%" + val + "%")
                );
            }
            Predicate predicate = cb.and(orCollection.toArray(new Predicate[0]));
            predicateCollection.add(predicate);
        }
        return cb.or(predicateCollection.toArray(new Predicate[0]));
    }

    protected Predicate newEqualPredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object v,
        Boolean sensitive,
        Boolean negation
    ) {
        Collection<Predicate> predicateCollection = new ArrayList<>();
        Object value = Boolean.FALSE.equals(sensitive) ?
            String.valueOf(v).toLowerCase(this.configuration.locale) :
                v;
        for (Expression<String> expression : expressions) {
            predicateCollection.add(
                Boolean.TRUE.equals(negation) ? cb.notEqual(expression, value) : cb.equal(expression, value)
            );
        }
        return cb.or(predicateCollection.toArray(new Predicate[0]));
    }

    protected Predicate newNullPredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object v
    ) {
        Collection<Predicate> predicateCollection = new ArrayList<>();
        for (Expression<String> expression : expressions) {
            predicateCollection.add(Boolean.TRUE.equals(v) ? cb.isNull(expression) : cb.isNotNull(expression));
        }
        return cb.or(predicateCollection.toArray(new Predicate[0]));
    }

    protected Predicate newSizePredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object value,
        Boolean negation,
        Class<?> type
    ) throws ParseException {
        return Boolean.FALSE.equals(negation) ?
            this.newMaxPredicate(cb, expressions, value, type) :
                this.newMinPredicate(cb, expressions, value, type);
    }

    protected Predicate newMaxPredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object value,
        Class<?> type
    ) throws ParseException {
        Collection<Predicate> predicateCollection = new ArrayList<>();
        for (Expression<String> expression : expressions) {
            if (type.equals(Date.class)) {
                predicateCollection.add(
                    cb.lessThanOrEqualTo(
                        expression.as(Date.class),
                        this.configuration.newFormat.parse(this.configuration.baseFormat.format((Date) value) + " 23:59:59")
                    )
                );
            } else if (type.equals(BigDecimal.class)) {
                predicateCollection.add(
                    cb.lessThanOrEqualTo(expression.as(BigDecimal.class), (BigDecimal) value)
                );
            } else if (type.equals(Double.class)) {
                predicateCollection.add(
                    cb.lessThanOrEqualTo(expression.as(Double.class), (Double) value)
                );
            } else if (type.equals(Float.class)) {
                predicateCollection.add(
                    cb.lessThanOrEqualTo(expression.as(Float.class), (Float) value)
                );
            } else if (type.equals(Long.class)) {
                predicateCollection.add(
                    cb.lessThanOrEqualTo(expression.as(Long.class), (Long) value)
                );
            } else {
                predicateCollection.add(
                    cb.lessThanOrEqualTo(expression.as(Integer.class), (Integer) value)
                );
            }
        }
        return cb.or(predicateCollection.toArray(new Predicate[0]));
    }

    protected Predicate newMinPredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object value,
        Class<?> type
    ) throws ParseException {
        Collection<Predicate> predicateCollection = new ArrayList<>();
        for (Expression<String> expression : expressions) {
            if (type.equals(Date.class)) {
                predicateCollection.add(
                    cb.greaterThanOrEqualTo(
                        expression.as(Date.class),
                        this.configuration.newFormat.parse(this.configuration.baseFormat.format((Date) value) + " 23:59:59")
                    )
                );
            } else if (type.equals(BigDecimal.class)) {
                predicateCollection.add(
                    cb.greaterThanOrEqualTo(expression.as(BigDecimal.class), (BigDecimal) value)
                );
            } else if (type.equals(Double.class)) {
                predicateCollection.add(
                    cb.greaterThanOrEqualTo(expression.as(Double.class), (Double) value)
                );
            } else if (type.equals(Float.class)) {
                predicateCollection.add(
                    cb.greaterThanOrEqualTo(expression.as(Float.class), (Float) value)
                );
            } else if (type.equals(Long.class)) {
                predicateCollection.add(
                    cb.greaterThanOrEqualTo(expression.as(Long.class), (Long) value)
                );
            } else {
                predicateCollection.add(
                    cb.greaterThanOrEqualTo(expression.as(Integer.class), (Integer) value)
                );
            }
        }
        return cb.or(predicateCollection.toArray(new Predicate[0]));
    }

    protected Predicate newListPredicate(
        CriteriaBuilder cb,
        Collection<Expression<String>> expressions,
        Object value,
        Boolean sensitive,
        Boolean negation
    ) {
        List<Predicate> predicateCollection = new ArrayList<>();
        List<List<Object>> collections = this.buildListOfCollection(value, sensitive);
        for (Expression<String> expression : expressions) {
            List<Predicate> innerCollection = new ArrayList<>();
            for (List<Object> values : collections) {
                Predicate predicate = expression.in(values);
                innerCollection.add(Boolean.TRUE.equals(negation) ? predicate.not() : predicate);
            }
            predicateCollection.add(
                Boolean.TRUE.equals(negation) ?
                cb.and(innerCollection.toArray(new Predicate[0])) :
                cb.or(innerCollection.toArray(new Predicate[0]))
            );
        }
        return cb.or(predicateCollection.toArray(new Predicate[0]));
    }

    private List<List<Object>> buildListOfCollection(Object value, Boolean sensitive) {
        List<Object> collections;
        if (value instanceof List) {
            collections = ((List<?>) value).stream().map((Object x) -> {
                if (Boolean.FALSE.equals(sensitive) && x.getClass().equals(String.class)) {
                    return ((String) x).toLowerCase(this.configuration.locale);
                } else {
                    return x;
                }
            }).collect(Collectors.toList());
        } else {
            collections = Collections.singletonList(
                Boolean.FALSE.equals(sensitive) && value.getClass().equals(String.class) ?
                ((String) value).toLowerCase(this.configuration.locale) :
                value
            );
        }
        return GeneralHelper.chunk(collections);
    }

    private void parsePredicate(
        Root<T> root,
        CriteriaBuilder cb,
        @NonNull SearchTarget target,
        @NonNull Class<?> type,
        @NonNull Object value
    ) throws ParseException {
        Collection<Predicate> predicateCollection = new ArrayList<>();
        Collection<Expression<String>> expressions = new ArrayList<>();
        List<SearchTarget.Option> options = Arrays.asList(target.option());
        boolean sensitive = options.contains(SearchTarget.Option.SENSITIVE);
        boolean negation = options.contains(SearchTarget.Option.NEGATION);
        for (String tmp : target.path()) {
            expressions.add(
                sensitive ?
                FieldHelper.fillFieldSelector(root, tmp) :
                cb.lower(FieldHelper.fillFieldSelector(root, tmp).as(String.class))
            );
        }

        switch (target.condition()) {
            case LIKE:
                // when negation == true then query using not like, else using like
                predicateCollection.add(
                    this.newLikePredicate(cb, expressions, value, sensitive, negation)
                );
                break;
            case LIST:
                // when negation == true then query using not in, else using in
                predicateCollection.add(
                    this.newListPredicate(cb, expressions, value, sensitive, negation)
                );
                break;
            case SIZE:
                // when negation == true then query using max, else using min
                predicateCollection.add(
                    this.newSizePredicate(cb, expressions, value, negation, type)
                );
                break;
            case NULL:
                // when negation == true then query using is not null, else using is null
                predicateCollection.add(this.newNullPredicate(cb, expressions, value));
                break;
            case EQUAL:
                // when negation == true then query using not equal, else using equal
                predicateCollection.add(
                    this.newEqualPredicate(cb, expressions, value, sensitive, negation)
                );
                break;
            default:
                // ignore
                break;
        }
        this.predicates.add(cb.or(predicateCollection.toArray(new Predicate[0])));
    }

    protected static class Configuration implements Serializable {

        protected Locale locale = Locale.getDefault();

        protected transient DateFormat baseFormat;

        protected transient DateFormat newFormat;

        protected Configuration(DateFormat baseFormat, DateFormat newFormat) {
            this.baseFormat = baseFormat;
            this.newFormat = newFormat;
        }
    }
}

