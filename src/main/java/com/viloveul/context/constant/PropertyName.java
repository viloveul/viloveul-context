package com.viloveul.context.constant;

import lombok.Getter;

public enum PropertyName {

    RABBIT_ADDRESS("viloveul.rabbit.address", "localhost:5672"),
    RABBIT_USERNAME("viloveul.rabbit.username", "guest"),
    RABBIT_PASSWORD("viloveul.rabbit.password", "guest"),
    RABBIT_VHOST("viloveul.rabbit.vhost", "/"),

    MAIL_HOST("viloveul.mail.host", "smtp.mailserver.com"),
    MAIL_PORT("viloveul.mail.port", 587),
    MAIL_USERNAME("viloveul.mail.username", "admin@mailserver.com"),
    MAIL_PASSWORD("viloveul.mail.password", "M4ailPassword"),
    MAIL_PROTOCOL("viloveul.mail.protocol", "smtp"),
    MAIL_AUTH("viloveul.mail.auth", Boolean.TRUE),
    MAIL_FROM("viloveul.mail.from"),
    MAIL_STARTTLS("viloveul.mail.starttls", Boolean.TRUE),

    FORMAT_CURRENCY_SYMBOL("viloveul.currency-symbol", "Rp."),
    FORMAT_CURRENCY_DECIMAL_SEP("viloveul.currency-decimal-sep", ','),
    FORMAT_CURRENCY_GROUP_SEP("viloveul.currency-group-sep", '.'),
    FORMAT_DATE("viloveul.format.date", "yyyy-MM-dd"),
    FORMAT_TIME("viloveul.format.time", "HH:mm:ss"),
    FORMAT_DATETIME("viloveul.format.datetime", "yyyy-MM-dd HH:mm:ss"),

    USER_DSBEAN("viloveul.user.dsbean", "dataSource"),
    USER_EXTRA("viloveul.user.extra"),
    USER_RELATION("viloveul.user.relation"),

    PATH_UPLOAD("viloveul.path.uploads","/var/www/uploads/application"),

    SECURITY_TOKEN_PRIKEY("viloveul.encryption.prikey"),
    SECURITY_TOKEN_PUBKEY("viloveul.encryption.pubkey"),
    SECURITY_TOKEN_RSA("viloveul.encryption.rsa", Boolean.FALSE),
    SECURITY_TOKEN_SECRET("viloveul.encryption.secret", "something"),
    SECURITY_TOKEN_HEADER("viloveul.security.token-header", "Authorization"),
    SECURITY_TOKEN_QUERY("viloveul.security.token-query", "token"),
    SECURITY_TOKEN_PREFIX("viloveul.security.token-prefix", "Bearer"),

    SECURITY_GLOBAL_PASSWORD("viloveul.auth.global-password"),

    RUN_AS_ADMINISTRATOR("viloveul.auth.as-administrator", Boolean.FALSE),
    RUN_DEFAULT_ROLE("viloveul.auth.default-role", "DEFAULT"),

    CONTEXT("viloveul.context", "APP");

    @Getter
    private final String key;

    private Object value;

    PropertyName(String key) {
        this.key = key;
    }

    PropertyName(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public <T> T getDefault(Class<T> cls) {
        return cls.cast(this.value);
    }

    public <T> T getDefault(Object def, Class<T> cls) {
        return cls.cast(this.value == null ? def : this.value);
    }
}
