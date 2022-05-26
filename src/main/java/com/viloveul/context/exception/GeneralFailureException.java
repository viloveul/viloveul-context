package com.viloveul.context.exception;

import com.viloveul.context.exception.message.GeneralErrorMessage;
import org.springframework.lang.NonNull;

public class GeneralFailureException extends FailureException {

    public GeneralFailureException(GeneralErrorMessage message) {
        super(message);
    }

    public GeneralFailureException(GeneralErrorMessage message, Throwable cause) {
        super(message, cause);
    }

    public GeneralFailureException(GeneralErrorMessage error, @NonNull String message) {
        super(error, message);
    }

    public GeneralFailureException(GeneralErrorMessage error, @NonNull String message, Throwable cause) {
        super(error, message, cause);
    }
}
