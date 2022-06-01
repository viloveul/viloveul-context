package com.viloveul.context.initial;

import com.viloveul.context.ApplicationContainer;
import com.viloveul.context.ViloveulConfiguration;
import com.viloveul.context.util.encryption.Tokenizer;
import com.viloveul.context.util.encryption.TokenizerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

@Configuration
@PropertySources({
    @PropertySource(value = "application.properties"),
    @PropertySource(value = "application-testing.properties", ignoreResourceNotFound = true)
})
@Import(ViloveulConfiguration.class)
public class TestConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    public void appContext(ApplicationContext context) {
        ApplicationContainer.init(context);
    }

    @Bean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    public Tokenizer tokenizer() {
        return new TokenizerImpl();
    }
}
