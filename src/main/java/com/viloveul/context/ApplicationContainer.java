package com.viloveul.context;

import com.viloveul.context.auth.dto.DetailAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ApplicationContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContainer.class);

    private static ApplicationContext context;

    public static void init(ApplicationContext applicationContext) {
        context = applicationContext;
        LOGGER.info("VILOVEUL CONTAINER INITIALIZED.");
    }

    public static void setup(ExtendedSpringApplication application, String[] args) {
        init(application.run(args));
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return context.getBean(beanName, clazz);
    }

    public static Authentication getContextAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static DetailAuthentication getDetailAuthentication() {
        return getDetailAuthentication(DetailAuthentication.class);
    }

    public static <T> T getDetailAuthentication(Class<T> clzz) {
        Authentication authentication = getContextAuthentication();
        if (
            authentication != null &&
            authentication.getDetails() != null &&
            authentication.getDetails().getClass().equals(clzz)
        ) {
            return clzz.cast(authentication.getDetails());
        }
        return null;
    }

    public static String getUsernameAuthentication() {
        Authentication authentication = getContextAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    public interface ExtendedSpringApplication {
        ConfigurableApplicationContext run(String... args);
    }
}
