package com.viloveul.context.auth;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class AccessEvaluator implements Serializable {

    private final String operation;

    private final String object;

    public AccessEvaluator(String operation, String object) {
        this.operation = operation;
        this.object = object;
    }

}
