package com.viloveul.context.auth;

import com.viloveul.context.auth.dto.DetailAuthentication;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

public interface AccessControlCustomizer {

    Access registerAccessCustomizer();

    interface Access extends Serializable {

        String getResource();

        <T> Executor<T> getExecutor();

    }

    interface Executor<T> extends Serializable {

        Specification<T> specification(DetailAuthentication authentication);

        boolean checker(DetailAuthentication authentication, String object);

    }

    interface Handler<T> extends Serializable {

        Specification<T> specification();

        String object();

    }

    @Getter
    class DefaultAccess<T> implements Access {

        private final String resource;

        private final Executor<T> executor;

        public DefaultAccess(String resource, Function<DetailAuthentication, Specification<T>> specification, Predicate<Handler<T>> checker) {
            this.resource = resource;
            this.executor = new Executor<T>() {
                @Override
                public Specification<T> specification(DetailAuthentication authentication) {
                    return specification.apply(authentication);
                }

                @Override
                public boolean checker(DetailAuthentication authentication, String object) {
                    return checker.test(new Handler<T>() {
                        @Override
                        public Specification<T> specification() {
                            return specification.apply(authentication);
                        }

                        @Override
                        public String object() {
                            return object;
                        }
                    });
                }
            };
        }
    }
}
