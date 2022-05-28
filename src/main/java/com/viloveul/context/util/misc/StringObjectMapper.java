package com.viloveul.context.util.misc;

import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
public class StringObjectMapper extends HashMap<String, Object> {

    public StringObjectMapper(String key, Object object) {
        this.put(key, object);
    }

    public StringObjectMapper(String[] keys, Object[] objects) {
        for (int i = 0; i < keys.length; i ++) {
            this.put(keys[i], objects[i]);
        }
    }

}
