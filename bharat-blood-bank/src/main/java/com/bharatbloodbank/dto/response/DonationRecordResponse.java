package com.bharatbloodbank.dto.response;

import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.DonationStatus;
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
public class DonationRecordResponse {
    private Long id;
    private Long donorId;
    private String donorName;
    private String donorPhone;
    private Long bloodBankId;
    private String bloodBankName;
    private LocalDate donationDate;
    private BloodGroup bloodGroup;
    private double volumeMl;
    private DonationStatus status;
    private boolean splitDone;
    private Long linkedRequestId;
    private String notes;
    private LocalDateTime confirmedAt;
}
