package org.example.model.dto;

import java.time.Instant;

public record AuditEvent(
        Instant ts,
        String eventType,   // e.g. TINYURL_CREATED / TINYURL_RESOLVED / TINYURL_NOT_FOUND / TINYURL_REDIRECTED
        String severity,    // INFO/WARN/ERROR
        String actor,       // optional for now ("anonymous" / userId)
        String ip,
        String userAgent,
        String requestId,
        String resourceType, // "TINYURL"
        String resourceId,   // code
        String outcome  // SUCCESS/FAILURE
) {}
