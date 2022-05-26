package com.viloveul.context.constant.message;

import com.viloveul.context.exception.message.SystemErrorMessage;
import lombok.Getter;

@Getter
public enum SystemErrorCollection implements SystemErrorMessage {

    SECURITY_KEY_NOT_EXISTS(1000,"Either private or public key not exists");

    private final Integer code;

    private final String message;

    SystemErrorCollection(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getStatus() {
        return 500;
    }

    @Override
    public Throwable getCause() {
        return null;
    }
}
