package com.viloveul.context.util.encryption;

import com.viloveul.context.constant.message.ExecutionErrorCollection;
import com.viloveul.context.constant.message.SystemErrorCollection;
import com.viloveul.context.util.misc.StringObjectMapper;
import com.viloveul.context.exception.GeneralFailureException;
import com.viloveul.context.exception.SystemFailureException;
import com.viloveul.context.util.helper.DateHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;

@Service
public class TokenizerImpl implements Tokenizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenizerImpl.class);

    private static final int IV_LENGTH = 12;

    private static final int TAG_LENGTH = 128;

    private static final int SALT_LENGTH = 16;

    private static final int MAX_RSAENC_LENGTH = 117;

    private static final int MAX_RSADEC_LENGTH = 128;

    @Autowired
    protected ObjectMapper objectMapper;

    protected Key prikey;

    protected Key pubkey;

    @Value("${viloveul.encryption.prikey}")
    protected String prikeyProperty;

    @Value("${viloveul.encryption.pubkey}")
    protected String pubkeyProperty;

    @Value("${viloveul.encryption.rsa:false}")
    protected Boolean rsaProperty;

    @Value("${viloveul.encryption.secret:something}")
    protected String secretProperty;

    @Override
    public String generate(Object object) {
        return Boolean.TRUE.equals(this.rsaProperty) ?
            this.generate(object, USE_NON_JWT, this.prikey) :
                this.generate(object, USE_NON_JWT, this.secretProperty);
    }

    @Override
    public String generate(Object object, String secret) {
        return this.generate(object, USE_NON_JWT, secret);
    }

    @Override
    public String generate(Object object, Key key) {
        return this.generate(object, USE_NON_JWT, key);
    }

    @Override
    public String generate(Object object, Integer type) {
        return Boolean.TRUE.equals(this.rsaProperty) ?
            this.generate(object, type, this.prikey) :
                this.generate(object, type, this.secretProperty);
    }

    @Override
    public String generate(Object object, Integer type, String secret) {
        String token;
        try {
            if (USE_JWT.equals(type)) {
                JwtBuilder builder = Jwts.builder();
                if (object instanceof String) {
                    builder.setSubject((String) object);
                } else {
                    builder.setClaims(this.objectMapper.convertValue(object, StringObjectMapper.class));
                }
                builder.signWith(SignatureAlgorithm.HS256, secret);
                builder.setExpiration(DateHelper.with(Calendar.SECOND,86400).getTime());
                token = builder.compact();
            } else {
                byte[] salt = randomNonce(SALT_LENGTH);
                byte[] iv = randomNonce(IV_LENGTH);
                Cipher cipher = this.newCipher(Cipher.ENCRYPT_MODE, secret, salt, iv);
                byte[] encrypted = cipher.doFinal(this.objectToByteArray(object));
                byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + encrypted.length)
                    .put(iv)
                    .put(salt)
                    .put(encrypted)
                    .array();
                token = Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
            }
        } catch (
            NoSuchAlgorithmException |
            InvalidKeySpecException |
            NoSuchPaddingException |
            InvalidAlgorithmParameterException |
            InvalidKeyException |
            IOException |
            IllegalBlockSizeException |
            BadPaddingException e
        ) {
            throw new GeneralFailureException(ExecutionErrorCollection.TOKEN_CANT_BE_GENERATED, e);
        }
        return token;
    }

    @Override
    public String generate(Object object, Integer type, Key key) {
        String token;
        try {
            if (USE_JWT.equals(type)) {
                JwtBuilder builder = Jwts.builder();
                if (object instanceof String) {
                    builder.setSubject((String) object);
                } else {
                    builder.setClaims(this.objectMapper.convertValue(object, StringObjectMapper.class));
                }
                builder.signWith(SignatureAlgorithm.RS256, key);
                builder.setExpiration(DateHelper.with(Calendar.SECOND,86400).getTime());
                token = builder.compact();
            } else {
                Cipher cipher = this.newCipher(Cipher.ENCRYPT_MODE, key);
                byte[] data = (object instanceof String) ?
                    ((String) object).getBytes() :
                        this.objectToByteArray(object);
                int len = data.length;
                int offSet = 0;
                byte[] resultBytes = {};
                byte[] cache;
                while (len - offSet > 0) {
                    if (len - offSet > MAX_RSAENC_LENGTH) {
                        cache = cipher.doFinal(data, offSet, MAX_RSAENC_LENGTH);
                        offSet += MAX_RSAENC_LENGTH;
                    } else {
                        cache = cipher.doFinal(data, offSet, len - offSet);
                        offSet = len;
                    }
                    resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                    System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
                }
                token = Base64.getEncoder().encodeToString(resultBytes);
            }
        } catch (
            NoSuchPaddingException |
            NoSuchAlgorithmException |
            InvalidKeyException |
            IOException |
            IllegalBlockSizeException |
            BadPaddingException e
        ) {
            throw new GeneralFailureException(ExecutionErrorCollection.TOKEN_CANT_BE_GENERATED, e);
        }
        return token;
    }

    @Override
    public <T> T parse(String token, Class<T> tClass) {
        return Boolean.TRUE.equals(this.rsaProperty) ?
            this.parse(token, tClass, USE_NON_JWT, this.pubkey) :
                this.parse(token, tClass, USE_NON_JWT, this.secretProperty);
    }

    @Override
    public <T> T parse(String token, Class<T> tClass, String secret) {
        return this.parse(token, tClass, USE_NON_JWT, secret);
    }

    @Override
    public <T> T parse(String token, Class<T> tClass, Key key) {
        return this.parse(token, tClass, USE_NON_JWT, key);
    }

    @Override
    public <T> T parse(String token, Class<T> tClass, Integer type) {
        return Boolean.TRUE.equals(this.rsaProperty) ?
            this.parse(token, tClass, type, this.pubkey) :
                this.parse(token, tClass, type, this.secretProperty);
    }

    @Override
    public <T> T parse(String token, Class<T> tClass, Integer type, String secret) {
        T result;
        try {
            if (USE_JWT.equals(type)) {
                JwtParser parser = Jwts.parser();
                parser.setSigningKey(secret);
                Jws<Claims> claims = parser.parseClaimsJws(token);
                result = this.objectMapper.convertValue(claims.getBody(), tClass);
            } else {
                byte[] data = Base64.getDecoder().decode(token.getBytes(StandardCharsets.UTF_8));
                ByteBuffer bb = ByteBuffer.wrap(data);
                byte[] iv = new byte[IV_LENGTH];
                bb.get(iv);
                byte[] salt = new byte[SALT_LENGTH];
                bb.get(salt);
                byte[] cipherText = new byte[bb.remaining()];
                bb.get(cipherText);
                Cipher cipher = this.newCipher(Cipher.DECRYPT_MODE, secret, salt, iv);
                byte[] plainMessage = cipher.doFinal(cipherText);
                result = tClass.cast(this.byteArrayToObject(plainMessage));
            }
        } catch (
            NoSuchAlgorithmException |
            InvalidKeySpecException |
            NoSuchPaddingException |
            InvalidAlgorithmParameterException |
            InvalidKeyException |
            IllegalBlockSizeException |
            BadPaddingException |
            IOException |
            ClassNotFoundException e
        ) {
            throw new GeneralFailureException(ExecutionErrorCollection.TOKEN_IS_INVALID, e);
        }
        return result;
    }

    @Override
    public <T> T parse(String token, Class<T> tClass, Integer type, Key key) {
        T result;
        try {
            if (USE_JWT.equals(type)) {
                JwtParser parser = Jwts.parser();
                parser.setSigningKey(key);
                Jws<Claims> claims = parser.parseClaimsJws(token);
                result = this.objectMapper.convertValue(claims.getBody(), tClass);
            } else {
                Cipher cipher = this.newCipher(Cipher.DECRYPT_MODE, key);
                byte[] data = Base64.getDecoder().decode(token.getBytes(StandardCharsets.UTF_8));
                int len = data.length;
                int offSet = 0;
                byte[] resultBytes = {};
                byte[] cache;
                while (len - offSet > 0) {
                    if (len - offSet > MAX_RSADEC_LENGTH) {
                        cache = cipher.doFinal(data, offSet, MAX_RSADEC_LENGTH);
                        offSet += MAX_RSADEC_LENGTH;
                    } else {
                        cache = cipher.doFinal(data, offSet, len - offSet);
                        offSet = len;
                    }
                    resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                    System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
                }
                result = tClass.cast(this.byteArrayToObject(resultBytes));
            }
        } catch (
            IllegalBlockSizeException |
            BadPaddingException |
            IOException |
            ClassNotFoundException |
            NoSuchPaddingException |
            NoSuchAlgorithmException |
            InvalidKeyException e
        ) {
            throw new GeneralFailureException(ExecutionErrorCollection.TOKEN_IS_INVALID, e);
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Boolean.TRUE.equals(this.rsaProperty)) {
            if (!this.prikeyProperty.isEmpty() || !this.pubkeyProperty.isEmpty()) {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                if (!this.prikeyProperty.isEmpty()) {
                    this.prikey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(this.loadKey(this.prikeyProperty)));
                }
                if (!this.pubkeyProperty.isEmpty()) {
                    this.pubkey = keyFactory.generatePublic(new X509EncodedKeySpec(this.loadKey(this.pubkeyProperty)));
                }
            } else {
                throw new SystemFailureException(SystemErrorCollection.SECURITY_KEY_NOT_EXISTS);
            }
        }
    }

    protected Cipher newCipher(
        int mode,
        String secret,
        byte[] salt,
        byte[] iv
    ) throws
        NoSuchAlgorithmException,
        InvalidKeySpecException,
        NoSuchPaddingException,
        InvalidAlgorithmParameterException,
        InvalidKeyException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 65536, 256);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, key, new GCMParameterSpec(TAG_LENGTH, iv));
        return cipher;
    }

    protected Cipher newCipher(
        int mode,
        Key secret
    ) throws
        NoSuchPaddingException,
        NoSuchAlgorithmException,
        InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); //NOSONAR
        cipher.init(mode, secret);
        return cipher;
    }

    protected Object byteArrayToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            return objectInputStream.readObject();
        }
    }

    protected byte[] objectToByteArray(Object object) throws IOException {
        try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return outputStream.toByteArray();
        }
    }

    protected byte[] loadKey(String path) {
        File keyFile = new File(path);
        byte[] keyBytes = null;
        try (
            FileInputStream keyFileInput = new FileInputStream(keyFile);
            DataInputStream keyDataInput = new DataInputStream(keyFileInput)
        ) {
            keyBytes = new byte[(int) keyFile.length()];
            keyDataInput.readFully(keyBytes);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e.getCause());
        }
        return keyBytes;
    }

    private static byte[] randomNonce(int len) {
        byte[] nonce = new byte[len];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }
}
