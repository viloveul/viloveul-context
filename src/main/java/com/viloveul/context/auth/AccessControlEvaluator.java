package com.viloveul.context.auth;

import com.viloveul.context.type.SignerType;
import com.viloveul.context.auth.dto.DetailAuthentication;
import com.viloveul.context.util.helper.FieldHelper;
import lombok.SneakyThrows;
import org.springframework.cache.Cache;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public class AccessControlEvaluator implements PermissionEvaluator {

    private static final List<SignerType> UTYPES = Arrays.asList(SignerType.ADMIN, SignerType.SYSTEM);

    private static final Locale locale = Locale.getDefault();

    private final PermissionStrategy strategy;

    private final Cache cache;

    public AccessControlEvaluator(
        Cache cache,
        PermissionStrategy strategy
    ) {
        this.cache = cache;
        this.strategy = strategy;
    }

    @SneakyThrows
    @Override
    public boolean hasPermission(Authentication authentication, Object domain, Object action) {
        if ((authentication == null) || (domain == null) || !(action instanceof String)){
            return false;
        }
        boolean result = this.isAdmin(authentication.getDetails());

        if (!result) {

            String operation = action.toString().toUpperCase(locale);

            if (domain instanceof String) {
                result = this.cacheIfNotCached(authentication, new CacheProperties((String) domain, operation, "__OBJECTID"), () -> this.isGrantedByUser(authentication, (String) domain, operation));
            } else {
                if (domain.getClass().isAnnotationPresent(AccessControl.class)) {
                    result = this.isGranted(authentication, domain, operation);
                }
            }
        }

        return result;
    }

    @SneakyThrows
    @Override
    public boolean hasPermission(
        Authentication authentication,
        Serializable object,
        String resource,
        Object operation
    ) {
        return this.cacheIfNotCached(authentication, new CacheProperties(resource, operation.toString(), object.toString()), () -> {
            if (this.isAdmin(authentication.getDetails())) {
                return true;
            } else {

                if (
                    AccessControlCollection.hasConfiguration(resource) &&
                    this.isGrantedByUser(authentication, resource, operation.toString()) &&
                    AccessControlCollection.validate(resource, (DetailAuthentication) authentication.getDetails(), new AccessEvaluator(operation.toString(), object.toString()))
                ) {
                    return true;
                }
                return this.strategy.checkAccess(
                    authentication,
                    resource,
                    operation.toString(),
                    object.toString()
                );
            }
        });
    }

    private boolean isAdmin(Object detail) {
        return detail instanceof DetailAuthentication && UTYPES.contains(((DetailAuthentication) detail).getType());
    }

    @SneakyThrows
    private boolean isGranted(Authentication authentication, Object domain, String operation) {
        AccessControl permissionChecker = domain.getClass().getAnnotation(AccessControl.class);
        String resource = permissionChecker.resource();
        String id = FieldHelper.fieldValue(domain, permissionChecker.identity(), String.class);
        return this.hasPermission(authentication, id, resource, operation);
    }

    @SneakyThrows
    protected boolean isGrantedByUser(Authentication authentication, String resource, String operation) {
        if (authentication.getDetails() instanceof DetailAuthentication) {
            String authority = operation.concat("-").concat(resource);
            DetailAuthentication userAuthentication = (DetailAuthentication) authentication.getDetails();
            if (userAuthentication.getAccessors().contains(authority)) {
                return true;
            } else {
                Collection<DetailAuthentication.GroupMapper> groups = userAuthentication.getAbilities();
                for (DetailAuthentication.GroupMapper group : groups) {
                    if (group.getAuthorities().contains(authority)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SneakyThrows
    protected boolean cacheIfNotCached(Authentication authentication, CacheProperties properties, Callable<Boolean> result) {
        if (authentication.getDetails() instanceof DetailAuthentication) {
            Boolean v = null;
            DetailAuthentication userAuthentication = (DetailAuthentication) authentication.getDetails();
            String k = userAuthentication.getId().concat(properties.resource).concat(properties.operation).concat(properties.object);
            if (this.cache != null) {
                v = this.cache.get(k, Boolean.class);
            }
            if (v == null) {
                v = result.call();
                if (this.cache != null) {
                    this.cache.put(k, v);
                }
            }
            return v;
        }
        return false;
    }

    private static class CacheProperties {
        private final String resource;
        private final String operation;
        private final String object;
        public CacheProperties(String resource, String operation, String object) {
            this.resource = resource;
            this.operation = operation;
            this.object = object;
        }
    }
}
