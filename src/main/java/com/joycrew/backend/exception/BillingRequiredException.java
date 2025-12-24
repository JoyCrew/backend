package com.joycrew.backend.exception;

import org.springframework.http.HttpStatus;

public class BillingRequiredException extends RuntimeException {
    public BillingRequiredException() {
        super("Billing method registration required.");
    }

    public HttpStatus status() {
        return HttpStatus.FORBIDDEN;
    }

    public String code() {
        return "BILLING_REQUIRED";
    }
}
