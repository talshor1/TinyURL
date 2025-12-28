package org.example.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class RequestContext {

    private final String requestId;
    private final String ip;
    private final String userAgent;

    public RequestContext(HttpServletRequest req) {
        this.requestId = getOrCreateRequestId(req);   // <- important
        this.ip = firstIp(req);
        this.userAgent = req.getHeader("User-Agent");
    }

    public String requestId() { return requestId; }
    public String ip() { return ip; }
    public String userAgent() { return userAgent; }

    private static String getOrCreateRequestId(HttpServletRequest req) {
        String rid = req.getHeader("X-Request-Id");
        if (rid == null || rid.isBlank()) {
            rid = UUID.randomUUID().toString();
        }
        return rid;
    }

    private static String firstIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
