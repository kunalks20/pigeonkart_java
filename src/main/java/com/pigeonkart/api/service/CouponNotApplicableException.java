package com.pigeonkart.api.service;

public class CouponNotApplicableException extends RuntimeException {
    public CouponNotApplicableException(String message) {
        super(message);
    }
}