package org.example.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newCreateBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(400)
                .refillGreedy(400, Duration.ofSeconds(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket newResolveBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(400)
                .refillGreedy(400, Duration.ofSeconds(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        RateLimitedRoute route = classify(method, path);
        if (route == RateLimitedRoute.NONE) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = clientIp(request);
        String bucketKey = route.name() + ":" + clientKey;

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> switch (route) {
            case CREATE -> newCreateBucket();
            case RESOLVE -> newResolveBucket();
            default -> throw new IllegalStateException("Unexpected route: " + route);
        });

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        // rate-limited
        long waitNanos = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = Math.max(1, Duration.ofNanos(waitNanos).toSeconds());

        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.getWriter().write("""
                {"error":"Too Many Requests"}
                """);
    }

    private RateLimitedRoute classify(String method, String path) {
        if (HttpMethod.POST.matches(method) && path.equals("/api/urls")) {
            return RateLimitedRoute.CREATE;
        }

        if (HttpMethod.GET.matches(method) && (path.startsWith("/t/") || path.startsWith("/r/"))) {
            return RateLimitedRoute.RESOLVE;
        }

        return RateLimitedRoute.NONE;
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    enum RateLimitedRoute {
        CREATE,
        RESOLVE,
        NONE
    }
}
