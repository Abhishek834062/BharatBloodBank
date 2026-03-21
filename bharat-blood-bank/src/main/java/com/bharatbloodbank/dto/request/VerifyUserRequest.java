package com.bharatbloodbank.dto.request;

import com.bharatbloodbank.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyUserRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus status; // APPROVED or REJECTED

    private String rejectionReason; // Required if status = REJECTED
}
