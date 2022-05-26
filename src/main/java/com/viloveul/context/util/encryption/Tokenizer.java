package com.viloveul.context.util.encryption;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public interface Tokenizer extends InitializingBean {

    Integer USE_JWT = 1;

    Integer USE_NON_JWT = 2;

    String generate(Object object);

    String generate(Object object, String secret);

    String generate(Object object, Key key);

    String generate(Object object, Integer type);

    String generate(Object object, Integer type, String secret);

    String generate(Object object, Integer type, Key key);

    <T> T parse(String token, Class<T> tClass);

    <T> T parse(String token, Class<T> tClass, String secret);

    <T> T parse(String token, Class<T> tClass, Key key);

    <T> T parse(String token, Class<T> tClass, Integer type);

    <T> T parse(String token, Class<T> tClass, Integer type, String secret);

    <T> T parse(String token, Class<T> tClass, Integer type, Key key);

}
