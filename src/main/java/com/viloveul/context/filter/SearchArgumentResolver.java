package com.viloveul.context.filter;

import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.function.Function;

public class SearchArgumentResolver<T> implements HandlerMethodArgumentResolver {

    private final HandlerMethodArgumentResolver resolver;

    private final Function<SearchProperties, Specification<T>> handler;

    public SearchArgumentResolver(HandlerMethodArgumentResolver resolver, Function<SearchProperties, Specification<T>> handler) {
        this.resolver = resolver;
        this.handler = handler;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        boolean type = SearchSpecification.class.equals(methodParameter.getParameterType().getSuperclass());
        boolean annotation = methodParameter.hasMethodAnnotation(SearchProperties.class);
        return type && annotation && this.resolver.supportsParameter(methodParameter);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter methodParameter, @Nullable ModelAndViewContainer modelAndViewContainer, @NonNull NativeWebRequest nativeWebRequest, @Nullable WebDataBinderFactory webDataBinderFactory) throws Exception {
        Object attribute = this.resolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        if (attribute != null && methodParameter.hasMethodAnnotation(SearchProperties.class)) {
            SearchProperties injector = methodParameter.getMethodAnnotation(SearchProperties.class);
            if (injector != null) {
                @SuppressWarnings("unchecked")
                SearchSpecification<T> specification = (SearchSpecification<T>) attribute;
                specification.joinSpecification(this.handler.apply(injector));
                return specification;
            }
        }
        return attribute;
    }
}
