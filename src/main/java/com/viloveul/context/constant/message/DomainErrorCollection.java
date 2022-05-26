package com.viloveul.context.constant.message;

import com.viloveul.context.exception.message.GeneralErrorMessage;
import lombok.Getter;

@Getter
public enum DomainErrorCollection implements GeneralErrorMessage {

    INFINITELY_RECURSIVE(5008, 422, "Infinitely Recursive"),
    PARENT_INACTIVE(5007, 422, "Parent is Not Available or Inactive"),

    DATA_ALREADY_EXISTS(5006, 400, "Data already exists"),
    DATA_IS_NOT_EXISTS(1103, 404,"Data Not Exists"),
    DATA_CANT_BE_DELETED(1102, 422,"Delete data Failed"),
    DATA_CANT_BE_UPDATED(1101, 422,"Update data Failed"),
    DATA_CANT_BE_CREATED(1100, 422, "Create data Failed"),
    DATA_CANT_BE_ACTIVATED(1100, 422, "Activation data Failed"),

    DEFAULT_MESSAGE(1001, 428, "Something Wrong with Entity");

    private final Integer code;

    private final Integer status;

    private final String message;

    DomainErrorCollection(Integer code, Integer status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public Throwable getCause() {
        return null;
    }
}
