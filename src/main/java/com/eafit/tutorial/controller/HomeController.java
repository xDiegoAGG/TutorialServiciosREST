package com.eafit.tutorial.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public RedirectView home() {
        return new RedirectView("/swagger-ui.html");
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Products API");
        info.put("version", "2.0");
        info.put("description", "API REST para gestión de productos");
        info.put("documentation", "/swagger-ui.html");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("products", "/api/v1/products");
        endpoints.put("swagger", "/swagger-ui.html");
        endpoints.put("api-docs", "/v3/api-docs");
        endpoints.put("h2-console", "/h2-console");
        
        info.put("endpoints", endpoints);
        
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "Products API");
        return ResponseEntity.ok(health);
    }
}
