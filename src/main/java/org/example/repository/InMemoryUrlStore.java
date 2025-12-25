package org.example.repository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUrlStore {
    private final ConcurrentHashMap<String, String> codeToUrl = new ConcurrentHashMap<>();

    public void put(String code, String url) {
        codeToUrl.put(code, url);
    }

    public Optional<String> getUrl(String code) {
        return Optional.ofNullable(codeToUrl.get(code));
    }

    public boolean containsCode(String code) {
        return codeToUrl.containsKey(code);
    }
}
