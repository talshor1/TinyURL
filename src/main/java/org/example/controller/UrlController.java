package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.exceptions.TinyUrlNotFoundException;
import org.example.model.dto.CreateUrlRequest;
import org.example.model.dto.CreateUrlResponse;
import org.example.model.dto.ResolveUrlResponse;
import org.example.services.UrlShortenerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class UrlController {

    private final UrlShortenerService service;

    public UrlController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<CreateUrlResponse> create(@RequestBody CreateUrlRequest req,
                                                    HttpServletRequest servletRequest) {
        if (req == null || req.url() == null || req.url().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String code = service.createShortCode(req.url());
        String baseUrl = servletRequest.getScheme() + "://" + servletRequest.getServerName() + ":" + servletRequest.getServerPort();
        String tinyUrl = baseUrl + "/t/" + code;

        return ResponseEntity.ok(new CreateUrlResponse(tinyUrl, code));
    }

    @GetMapping("/t/{code}")
    public String resolve(@PathVariable String code) {
        try {
            return service.resolve(code);
        } catch (TinyUrlNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, e.getMessage());
        }
    }

     @GetMapping("/r/{code}")
     public ResponseEntity<Void> redirect(@PathVariable String code) {
         String url = service.resolve(code);
         return ResponseEntity.status(302).location(URI.create(url)).build();
     }
}
