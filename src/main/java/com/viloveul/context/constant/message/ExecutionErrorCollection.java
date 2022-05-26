package com.viloveul.context.constant.message;

import com.viloveul.context.exception.message.GeneralErrorMessage;
import lombok.Getter;

@Getter
public enum ExecutionErrorCollection implements GeneralErrorMessage {

    ADMIN_RESTRICTION(1003, 406,"Administrator only"),
    TOKEN_IS_INVALID(1002, 406,"Token is invalid"),
    TOKEN_CANT_BE_GENERATED(1001, 424, "Generate Token Failed"),
    FILE_ERROR(1000, 424, "File error"),

    DEFAULT_MESSAGE(501, 500, "Execution failed");

    private final Integer code;

    private final Integer status;

    private final String message;

    ExecutionErrorCollection(Integer code, Integer status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public Throwable getCause() {
        return null;
    }
}
