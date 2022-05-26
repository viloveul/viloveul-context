package com.viloveul.context.auth;

import com.viloveul.context.auth.dto.DetailAuthentication;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface AccessControlCustomizer {

    Access registerAccessCustomizer();

    interface Access extends Serializable {

        String getResource();

        <T> Executor<T> getExecutor();

    }

    interface Executor<T> extends Serializable {

        Specification<T> specification(DetailAuthentication authentication, String operation);

        boolean checker(DetailAuthentication authentication, AccessEvaluator evaluator);

    }

    interface Handler<T> extends Serializable {

        Specification<T> specification();

        AccessEvaluator evaluator();

    }

    @Getter
    class DefaultAccess<T> implements Access {

        private final String resource;

        private final Executor<T> executor;

        public DefaultAccess(String resource, BiFunction<DetailAuthentication, String, Specification<T>> specification, Predicate<Handler<T>> checker) {
            this.resource = resource;
            this.executor = new Executor<T>() {
                @Override
                public Specification<T> specification(DetailAuthentication authentication, String operation) {
                    return specification.apply(authentication, operation);
                }

                @Override
                public boolean checker(DetailAuthentication authentication, AccessEvaluator evaluator) {
                    return checker.test(new Handler<T>() {
                        @Override
                        public Specification<T> specification() {
                            return specification.apply(authentication, evaluator.getOperation());
                        }

                        @Override
                        public AccessEvaluator evaluator() {
                            return evaluator;
                        }
                    });
                }
            };
        }
    }
}
