package org.example.services;

import org.example.exceptions.TinyUrlNotFoundException;
import org.example.model.RequestContext;
import org.example.model.entity.TinyUrlEntity;
import org.example.repository.TinyUrlRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Objects;

@Service
public class UrlShortenerService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LEN = 7;
    private static final int MAX_ATTEMPTS = 100;
    private final Cache cache;
    private final AuditEventService audit;
    private final ObjectProvider<RequestContext> requestContextProvider;

    private final SecureRandom random = new SecureRandom();
    private final TinyUrlRepository repository;

    public UrlShortenerService(CacheManager cacheManager, TinyUrlRepository repository,
                               AuditEventService audit, ObjectProvider<RequestContext> requestContextProvider) {
        cache = cacheManager.getCache("tinyurl-by-code");
        this.repository = repository;
        this.requestContextProvider = requestContextProvider;
        this.audit = audit;
    }

    public String createShortCode(String originalUrl) {
        String code = "";
        RequestContext ctx = requestContextProvider.getObject();
        System.out.println("thread=" + Thread.currentThread().getName() + " requestId=" + ctx.requestId());
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            code = randomCode();
            if (!repository.existsByCode(code)) {
                repository.save(new TinyUrlEntity(code, originalUrl));
                if (cache != null) {
                    cache.put(code, originalUrl);
                }
                break;
            }
        }
        if (code.isEmpty()) {
            audit.emit(AuditEventService.now(
                    "TINYURL_CREATE", "INFO", "anonymous", requestContextProvider.getObject(),
                    "TINYURL", code, "FAILED"
            ));
            throw new IllegalStateException("Failed to generate unique code");
        }
        audit.emit(AuditEventService.now(
                "TINYURL_CREATE", "INFO", "anonymous", requestContextProvider.getObject(),
                "TINYURL", code, "SUCCESS"
        ));
        return code;
    }

    public String resolve(String code) {
        String url;
        if (cache != null && cache.get(code) != null) {
            System.out.println("Cache hit");
            url = Objects.requireNonNull(cache.get(code)).toString();
        } else {
            url = repository.findByCode(code)
                    .map(TinyUrlEntity::getOriginalUrl)
                    .orElseThrow(() -> new TinyUrlNotFoundException(code));
            assert cache != null;
            cache.put(code, url);
        }
        audit.emit(AuditEventService.now(
                "TINYURL_RESOLVED", "INFO", "anonymous", requestContextProvider.getObject(),
                "TINYURL", code, "SUCCESS"
        ));
        return url;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(UrlShortenerService.CODE_LEN);
        for (int i = 0; i < UrlShortenerService.CODE_LEN; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
