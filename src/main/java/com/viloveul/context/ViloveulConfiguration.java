package com.viloveul.context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.viloveul.context.auth.AccessControlCollection;
import com.viloveul.context.auth.AccessControlCustomizer;
import com.viloveul.context.constant.message.ExecutionErrorCollection;
import com.viloveul.context.exception.GeneralFailureException;
import com.viloveul.context.util.encryption.Tokenizer;
import com.viloveul.context.util.encryption.TokenizerImpl;
import com.viloveul.context.util.misc.MediaStorage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.text.NumberFormat.getCurrencyInstance;

@Role(2)
public class ViloveulConfiguration {

    @Bean(autowireCandidate = false)
    public BeanPostProcessor beanPostProcessor() {
        return new ViloveulBeanPostProcessor();
    }

    @Bean
    public ConversionService conversionService() {
        return DefaultConversionService.getSharedInstance();
    }

    @Bean("dateFormat")
    public DateFormat dateFormat(@Value("${viloveul.format.date:yyyy-MM-dd}") String format) {
        return new SimpleDateFormat(format);
    }

    @Bean("timeFormat")
    public DateFormat timeFormat(@Value("${viloveul.format.time:HH:mm:ss}") String format) {
        return new SimpleDateFormat(format);
    }

    @Bean("dateTimeFormat")
    public DateFormat dateTimeFormat(@Value("${viloveul.format.datetime:yyyy-MM-dd HH:mm:ss}") String format) {
        return new SimpleDateFormat(format);
    }

    @Bean("currencyConverter")
    public DecimalFormat currencyConverter(
        @Value("${viloveul.currency.symbol:Rp.}") String symbol,
        @Value("${viloveul.currency.decimal-sep:,}") Character decimal,
        @Value("${viloveul.currency.group-sep:.}") Character group
    ) {
        DecimalFormat decimalFormat = (DecimalFormat) getCurrencyInstance();
        DecimalFormatSymbols rupiahSymbols = DecimalFormatSymbols.getInstance();
        rupiahSymbols.setCurrencySymbol(symbol);
        rupiahSymbols.setMonetaryDecimalSeparator(decimal);
        rupiahSymbols.setGroupingSeparator(group);
        decimalFormat.setDecimalFormatSymbols(rupiahSymbols);
        return decimalFormat;
    }

    @Bean
    public ObjectMapper objectMapper(@Autowired @Qualifier("dateTimeFormat") DateFormat dateTimeFormat) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(dateTimeFormat);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper;
    }

    @Bean
    public MediaStorage mediaStorage(@Value("${viloveul.path.uploads:/tmp/www-uploads}") String base) {
        return new MediaStorage() {

            @Override
            public String write(String fname, InputStream input) throws IOException {
                String name = StringUtils.cleanPath(fname);
                if (name.contains("..")) {
                    throw new GeneralFailureException(ExecutionErrorCollection.FILE_ERROR);
                }
                Calendar calendar = Calendar.getInstance();
                String ymp = File.separator +
                    calendar.get(Calendar.YEAR) +
                    File.separator +
                    (String.format("%1$2s", calendar.get(Calendar.MONTH)).replace(' ', '0'));
                String pathname = ymp + File.separator + name;
                Files.createDirectories(Paths.get(base + ymp));
                Files.copy(input, Paths.get(base + pathname), StandardCopyOption.REPLACE_EXISTING);
                return pathname;
            }

            @Override
            public File load(String path) {
                return new File(base + path);
            }
        };
    }

    @Bean
    public Tokenizer tokenizer() {
        return new TokenizerImpl();
    }

    private static class ViloveulBeanPostProcessor implements BeanPostProcessor {

        @Nullable
        @Override
        public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
            if (bean instanceof AccessControlCustomizer) {
                AccessControlCollection.registerControl(((AccessControlCustomizer) bean).registerAccessCustomizer());
            }
            return bean;
        }
    }
}
