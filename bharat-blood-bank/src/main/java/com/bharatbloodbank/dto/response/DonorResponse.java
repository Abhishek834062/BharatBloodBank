package com.bharatbloodbank.dto.response;

import com.bharatbloodbank.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private BloodGroup bloodGroup;
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate lastDonationDate;
    private boolean eligible;
    private LocalDate nextEligibleDate;
    private Long addedByBankId;
    private String addedByBankName;
    private LocalDateTime createdAt;
}




