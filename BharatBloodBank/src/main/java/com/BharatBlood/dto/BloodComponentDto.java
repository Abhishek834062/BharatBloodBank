package com.BharatBlood.dto;

import java.time.LocalDate;

import com.BharatBlood.entity.BloodGroup;
import com.BharatBlood.entity.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BloodComponentDto {

    private Long id;
	private ComponentType type;
	private BloodGroup bloodGroup;
	private Long volumeInMl;
    private LocalDate collectionDate;
	private LocalDate expiryDate;
	private Long bankId;
}
