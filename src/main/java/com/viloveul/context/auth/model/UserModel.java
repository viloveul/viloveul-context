package com.viloveul.context.auth.model;

import com.viloveul.context.type.SignerType;

import java.util.Date;

public interface UserModel {

    String getId();

    String getUsername();

    String getEmail();

    String getFullname();

    SignerType getType();

    Boolean getStatus();

    Date getStartDate();

    Date getEndDate();

    GroupModel getGroup();

}
