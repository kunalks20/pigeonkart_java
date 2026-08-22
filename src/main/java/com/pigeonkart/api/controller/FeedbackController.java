package com.pigeonkart.api.controller;

import com.pigeonkart.api.dto.FeedbackRequest;
import com.pigeonkart.api.model.Feedback;
import com.pigeonkart.api.repository.FeedbackRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class FeedbackController {
    private final FeedbackRepo feedbackRepository;

    @PostMapping("/api/feedback")
    public ResponseEntity<Void> submit(@Valid @RequestBody FeedbackRequest request) {
        Feedback fb = Feedback.builder()
                .name(request.getName())
                .email(request.getEmail())
                .message(request.getMessage())
                .createdAt(Instant.now())
                .build();
        feedbackRepository.save(fb);
        return ResponseEntity.ok().build();
    }

    // Admin-only — protected by AdminAuthInterceptor (see config package), which
    // guards every /api/admin/** path except /api/admin/login.
    @GetMapping("/api/admin/feedback")
    public List<Feedback> list() {
        return feedbackRepository.findAll();
    }

}
