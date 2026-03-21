
package com.bharatbloodbank.dto.response;

import com.bharatbloodbank.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private Long userId;
    private String doctorName;
    private String specialization;
    private String medicalRegistrationNumber;
    private String hospitalName;
    private String hospitalAddress;
    private String city;
    private String state;
    private String email;
    private String phone;
    private VerificationStatus verificationStatus;
    private boolean enabled;
    private LocalDateTime registeredAt;
}
