package com.bharatbloodbank.exception;

public class DonorNotEligibleException extends RuntimeException {
    public DonorNotEligibleException(String message) {
        super(message);
    }
}
