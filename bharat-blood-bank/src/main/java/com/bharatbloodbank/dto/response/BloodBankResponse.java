
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
public class BloodBankResponse {
    private Long id;
    private Long userId;
    private String bankName;
    private String licenseNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String contactPhone;
    private String contactEmail;
    private VerificationStatus verificationStatus;
    private boolean enabled;
    private LocalDateTime registeredAt;
}
