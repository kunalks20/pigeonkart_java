package com.pigeonkart.api.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthService adminAuthService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("X-Admin-Token");

        // CORS preflight requests don't carry the admin token,
        // so they must bypass authentication.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!adminAuthService.isValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid admin token");
            return false;
        }
        return true;
    }
}
