package com.bharatbloodbank.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ConfirmDonationRequest {

    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @NotNull(message = "Donation date is required")
    private LocalDate donationDate;

    // Optional - if this donation is for a specific blood request
    private Long linkedRequestId;

    private String notes;
}
