package com.viloveul.context.exception;

import com.viloveul.context.exception.message.ErrorMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.lang.NonNull;

public class FailureException extends RuntimeException implements ErrorMessage {

    @Getter
    @JsonIgnore
    private final Integer status;

    @Getter
    private final Integer code;

    FailureException(Integer status, Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }

    FailureException(Integer status, Integer code, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    FailureException(ErrorMessage error) {
        super(error.getMessage());
        this.code = error.getCode();
        this.status = error.getStatus();
    }

    FailureException(ErrorMessage error, @NonNull String message) {
        super(message);
        this.code = error.getCode();
        this.status = error.getStatus();
    }

    FailureException(ErrorMessage error, Throwable cause) {
        super(error.getMessage(), cause);
        this.code = error.getCode();
        this.status = error.getStatus();
    }

    FailureException(ErrorMessage error, @NonNull String message, Throwable cause) {
        super(message, cause);
        this.code = error.getCode();
        this.status = error.getStatus();
    }
}
