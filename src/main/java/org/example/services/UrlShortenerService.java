package org.example.services;

import org.example.exceptions.TinyUrlNotFoundException;
import org.example.model.entity.TinyUrlEntity;
import org.example.repository.TinyUrlRepository;
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

    private final SecureRandom random = new SecureRandom();
    private final TinyUrlRepository repository;

    public UrlShortenerService(CacheManager cacheManager, TinyUrlRepository repository) {
        cache = cacheManager.getCache("tinyurl-by-code");
        this.repository = repository;
    }

    public String createShortCode(String originalUrl) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = randomCode();
            if (!repository.existsByCode(code)) {
                repository.save(new TinyUrlEntity(code, originalUrl));
                if (cache != null) {
                    cache.put(code, originalUrl);
                }
                return code;
            } else {
                System.out.println("Code " + code + " already exists");
            }
        }

        throw new IllegalStateException("Failed to generate unique code");
    }

    public String resolve(String code) {
        if (cache != null && cache.get(code) != null) {
            System.out.println("Cache hit");
            return Objects.requireNonNull(cache.get(code)).toString();
        }
        String originalUrl = repository.findByCode(code)
                .map(TinyUrlEntity::getOriginalUrl)
                .orElseThrow(() -> new TinyUrlNotFoundException(code));
        assert cache != null;
        cache.put(code, originalUrl);
        return originalUrl;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(UrlShortenerService.CODE_LEN);
        for (int i = 0; i < UrlShortenerService.CODE_LEN; i++) {
            sb.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
