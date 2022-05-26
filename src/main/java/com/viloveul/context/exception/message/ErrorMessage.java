package com.viloveul.context.exception.message;

public interface ErrorMessage {

    Integer getCode();

    Integer getStatus();

    String getMessage();

    Throwable getCause();

}
