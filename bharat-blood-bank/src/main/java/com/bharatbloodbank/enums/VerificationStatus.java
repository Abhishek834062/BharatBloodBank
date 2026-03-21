package com.bharatbloodbank.enums;

public enum VerificationStatus {
    PENDING,    // Registered, waiting for admin review
    APPROVED,   // Admin approved, can login and operate
    REJECTED    // Admin rejected with reason
}
