package com.viloveul.context.exception;

import com.viloveul.context.exception.message.SystemErrorMessage;
import org.springframework.lang.NonNull;

public class SystemFailureException extends FailureException {

    public SystemFailureException(SystemErrorMessage error) {
        super(error);
    }

    public SystemFailureException(SystemErrorMessage error, Throwable cause) {
        super(error, cause);
    }

    public SystemFailureException(SystemErrorMessage error, @NonNull String message) {
        super(error, message);
    }

    public SystemFailureException(SystemErrorMessage error, @NonNull String message, Throwable cause) {
        super(error, message, cause);
    }

}
