package com.pigeonkart.api.repository;

import com.pigeonkart.api.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepo extends JpaRepository<Feedback, Long> {
}