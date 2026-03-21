package com.bharatbloodbank.dto.response;

import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String hospitalName;
    private Long bloodBankId;
    private String bloodBankName;
    private BloodGroup bloodGroup;
    private BloodComponent component;
    private int unitsRequired;
    private String patientName;
    private String patientCondition;
    private boolean emergency;
    private String emergencyReason;
    private boolean emergencyVerifiedByBank;
    private RequestStatus status;
    private String rejectionReason;
    private String bankNotes;
    private String contactedDonorName;
    private String contactedDonorPhone;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime fulfilledAt;
}
