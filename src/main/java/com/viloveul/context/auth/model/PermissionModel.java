package com.viloveul.context.auth.model;

public interface PermissionModel {
    String getId();
    String getUser();
    String getResource();
    String getOperation();
    String getObject();
    Boolean getStatus();
}
