package com.viloveul.context.auth.model;

import javax.persistence.Entity;

//@Entity(name = "Permission")
public interface PermissionModel {
    String getId();
    String getUser();
    String getResource();
    String getOperation();
    String getObject();
    Boolean getStatus();
}
