package com.viloveul.context.request;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// https://www.baeldung.com/spring-reading-httpservletrequest-multiple-times

public class AdvancedRequestWrapper extends ContentCachingRequestWrapper {

    private byte[] cachedContentStream;

    public AdvancedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String contentType = this.getContentType();
        if (contentType != null && contentType.contains("application/json") && !HttpMethod.GET.matches(this.getMethod())) {
            this.cachedContentStream = StreamUtils.copyToByteArray(request.getInputStream());
        }
    }

    @Override
    @NonNull
    public ServletInputStream getInputStream() {
        return new AdvancedInputStream(this.cachedContentStream);
    }

    @Override
    @NonNull
    public BufferedReader getReader() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedContentStream);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    public static class AdvancedInputStream extends ServletInputStream {

        private final InputStream cachedContentInputStream;

        public AdvancedInputStream(byte[] cachedContent) {
            this.cachedContentInputStream = new ByteArrayInputStream(cachedContent);
        }

        @Override
        public boolean isFinished() {
            try {
                return cachedContentInputStream.available() == 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return cachedContentInputStream.read();
        }
    }
}
