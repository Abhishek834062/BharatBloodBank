package com.bharatbloodbank.dto.request;

import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BloodRequestCreateRequest {

    @NotNull(message = "Blood bank ID is required")
    private Long bloodBankId;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Blood component is required")
    private BloodComponent component;

    @Min(value = 1, message = "At least 1 unit required")
    @Max(value = 10, message = "Max 10 units per request")
    private int unitsRequired;

    private String patientName;

    private String patientCondition;

    private boolean emergency = false;

    private String emergencyReason;
}
