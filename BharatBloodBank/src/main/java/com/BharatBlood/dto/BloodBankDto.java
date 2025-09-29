package com.BharatBlood.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BloodBankDto {
	
	private Long id;
	private String bankName;
	private String location;
	private String email;
	private String password;

}
