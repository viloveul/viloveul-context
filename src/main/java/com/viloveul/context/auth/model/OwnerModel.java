package com.viloveul.context.auth.model;

import javax.persistence.Entity;

//@Entity(name = "Owner")
public interface OwnerModel {
    String getId();
    String getUser();
    String getGroup();
    String getResource();
    String getObject();
    Boolean getStatus();
}
