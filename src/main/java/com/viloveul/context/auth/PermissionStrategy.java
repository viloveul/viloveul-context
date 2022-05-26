package com.viloveul.context.auth;

import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface PermissionStrategy {

    Boolean checkAccess(Authentication authentication, String resource, String operation, String object);

}
