package com.pigeonkart.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple single-admin auth: one username/password pair from env vars, and
 * opaque bearer tokens kept in memory (not a JWT — deliberately minimal since
 * there's exactly one admin account for now). Tokens expire after 8 hours or
 * on app restart. Swap for Spring Security + a real user table if/when you
 * add multiple admin accounts.
 */
@Service
public class AdminAuthService {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private final Map<String, Instant> activeTokens = new ConcurrentHashMap<>();
    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    public String login(String username, String password) {
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new SecurityException("Invalid admin credentials");
        }
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, Instant.now().plus(TOKEN_TTL));
        System.out.println("Token" + token);
        return token;
    }

    public boolean isValid(String token) {
        if (token == null) return false;
        Instant expiry = activeTokens.get(token);
        if (expiry == null) return false;
        if (Instant.now().isAfter(expiry)) {
            activeTokens.remove(token);
            return false;
        }
        return true;
    }
}
