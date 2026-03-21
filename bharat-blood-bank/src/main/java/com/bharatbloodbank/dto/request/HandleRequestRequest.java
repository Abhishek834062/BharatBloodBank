package com.bharatbloodbank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleRequestRequest {
    // ACCEPT / REJECT / VERIFY_EMERGENCY / CONTACT_DONOR / FULFILL
    @NotBlank
    private String action;

    private String rejectionReason;

    private String bankNotes;

    // For CONTACT_DONOR action
    private Long donorId;

    // For FULFILL action - which donor donation fulfilled this
    private Long donationRecordId;
}
