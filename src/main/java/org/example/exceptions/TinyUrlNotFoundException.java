package org.example.exceptions;

public class TinyUrlNotFoundException extends RuntimeException {
    public TinyUrlNotFoundException(String code) {
        super("Tiny URL not found: " + code);
    }
}

