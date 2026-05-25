package com.ageulin.oauth2_resource_server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("me")
public class MeController {
    @GetMapping
    public ResponseEntity<Map<String, String>> getMe() {
        final Map<String, String> body = new HashMap<>();
        body.put("message", "Hello, me!");

        return ResponseEntity.ok(body);
    }
}
