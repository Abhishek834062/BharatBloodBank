package com.bharatbloodbank.dto.response;

import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryResponse {
    private Long bloodBankId;
    private String bloodBankName;
    // Key: "A+ - RBC", Value: unit count
    private Map<String, Long> inventory;
    private long totalAvailableUnits;
}
