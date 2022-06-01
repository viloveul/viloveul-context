package com.viloveul.context;

import com.viloveul.context.initial.TestConfiguration;
import com.viloveul.context.util.helper.DateHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.DateFormat;
import java.util.Date;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        TestConfiguration.class
    }
)
class FormatterTests {

    @Autowired
    private DateFormat dateFormat;

    @Autowired
    private DateFormat timeFormat;

    @Autowired
    private DateFormat dateTimeFormat;

    @Autowired
    @Qualifier("currencyConverter")
    private DecimalFormat currencyConverter;

    @Test
    void testNumberToCurrency() {
        Double annual = 10000D;
        String result = this.currencyConverter.format(annual);
        Assertions.assertEquals("Rp.10.000,00", result);
    }

    @Test
    void testDateFormat() throws ParseException {
        String date = "2021-05-06";
        String result = this.dateFormat.format(this.dateFormat.parse(date));
        Assertions.assertEquals(date, result);
    }

    @Test
    void testTimeFormat() throws ParseException {
        String time = "22:50:00";
        String result = this.timeFormat.format(this.timeFormat.parse(time));
        Assertions.assertEquals(time, result);
    }

    @Test
    void testDateTimeFormat() throws ParseException {
        String dateTime = "2021-05-06 22:50:00";
        String result = this.dateTimeFormat.format(this.dateTimeFormat.parse(dateTime));
        Assertions.assertEquals(dateTime, result);
    }

    @Test
    void testConvertDateToStartTime() {
        Date result = DateHelper.startTime(new Date()).getTime();
        Assertions.assertEquals("00:00:00", this.timeFormat.format(result));
    }

    @Test
    void testConvertDateToLastime() {
        Date result = DateHelper.lastTime(new Date()).getTime();
        Assertions.assertEquals("23:59:59", this.timeFormat.format(result));
    }
}
