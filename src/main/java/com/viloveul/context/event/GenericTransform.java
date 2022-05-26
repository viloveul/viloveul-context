package com.viloveul.context.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class GenericTransform implements Serializable {

    private String target;

    private Serializable payload;

    public GenericTransform(String target, Serializable payload) {
        this.target = target;
        this.payload = payload;
    }
}
