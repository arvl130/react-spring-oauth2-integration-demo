package com.ageulin.oauth2_resource_server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("me")
public class MeController {
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMe(Authentication authentication) {
        final Map<String, String> data = new HashMap<>();
        data.put("id", authentication.getName());

        final Map<String, Object> body = new HashMap<>();

        body.put("message", "Hello, me!");
        body.put("data", data);

        return ResponseEntity.ok(body);
    }
}
