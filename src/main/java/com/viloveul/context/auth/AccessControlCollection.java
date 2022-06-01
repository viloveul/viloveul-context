package com.viloveul.context.auth;

import com.viloveul.context.auth.dto.DetailAuthentication;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

public class AccessControlCollection {

    private static final Map<String, AccessControlCustomizer.Executor<?>> controls = new HashMap<>();

    private AccessControlCollection() {
        throw new IllegalStateException("AccessControlCollection class");
    }

    public static void registerControl(AccessControlCustomizer.Access access) {
        controls.put(access.getResource(), access.getExecutor());
    }

    @SuppressWarnings("unchecked")
    public static <T> Specification<T> fetchSpecification(String resource, DetailAuthentication authentication) {
        return (Specification<T>) controls.get(resource).specification(authentication);
    }

    public static boolean validate(String resource, DetailAuthentication authentication, String object) {
        return controls.get(resource).checker(authentication, object);
    }

    public static boolean hasConfiguration(String resource) {
        return controls.containsKey(resource);
    }

}
