package com.viloveul.context.util.misc;

import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
public class StringObjectMapper extends HashMap<String, Object> {

    public StringObjectMapper(String key, String object) {
        this.put(key, object);
    }

}
