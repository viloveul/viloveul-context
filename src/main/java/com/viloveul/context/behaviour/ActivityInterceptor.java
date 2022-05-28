package com.viloveul.context.behaviour;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viloveul.context.util.misc.ActivityRecord;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ActivityInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityInterceptor.class);

    private final Function<Map<String, String>, Boolean> handler;

    private final ObjectMapper mapper;

    public ActivityInterceptor(Function<Map<String, String>, Boolean> handler, ObjectMapper mapper) {
        this.handler = handler;
        this.mapper = mapper;
    }

    @Override
    public void afterCompletion(
        HttpServletRequest request,
        @Nullable HttpServletResponse response,
        @Nullable Object handler,
        @Nullable Exception exception
    ) throws Exception {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            try {
                HandlerMethod hm = (HandlerMethod) handler;
                Method method = Objects.requireNonNull(hm).getMethod();
                String action = request.getMethod();
                boolean hasRecord = true;
                if (method.isAnnotationPresent(ActivityRecord.class)) {
                    ActivityRecord activityRecord = method.getAnnotation(ActivityRecord.class);
                    if (!"__method".equalsIgnoreCase(activityRecord.action())) {
                        action = activityRecord.action();
                    }
                    hasRecord = activityRecord.payload();
                }
                Map<String, String> map = new HashMap<>();
                map.put("url", request.getRequestURL().toString());
                map.put("user_agent", request.getHeader("User-Agent"));
                map.put("ip_address", this.extractIpAddress(request));
                map.put("reference", request.getHeader("X-Requested-With"));
                map.put("action", action.toUpperCase(Locale.ROOT));
                map.put("payload", hasRecord ? this.extractPayload(request) : null);
                this.handler.apply(map);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @SneakyThrows
    private String extractPayload(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            return new String(StreamUtils.copyToByteArray(request.getInputStream()));
        } else {
            return this.mapper.writeValueAsString(request.getParameterMap());
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String[] ips = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        for (String ipKey : ips) {
            String ipCandidate = request.getHeader(ipKey);
            if (ipCandidate != null && ipCandidate.length() != 0 && !"unknown".equalsIgnoreCase(ipCandidate)) {
                ipAddress = ipCandidate;
                break;
            }
        }
        return ipAddress;
    }
}
