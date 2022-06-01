package com.viloveul.context;

import com.viloveul.context.initial.TestConfiguration;
import com.viloveul.context.util.encryption.Tokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        TestConfiguration.class
    }
)
class TokenizerTests {

    @Autowired
    private Tokenizer tokenizer;

    @Test
    void testEncryptDecrypt() {
        String token = this.tokenizer.generate("user");
        String jos = this.tokenizer.parse(token, String.class);
        Assertions.assertEquals("user", jos);
    }
}
