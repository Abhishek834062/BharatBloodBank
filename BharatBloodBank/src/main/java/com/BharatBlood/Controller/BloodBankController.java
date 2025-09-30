package com.BharatBlood.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.BharatBlood.dto.BloodBankDto;
import com.BharatBlood.dto.BloodBankLoginDto;
import com.BharatBlood.service.BloodBankService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BloodBankController {
	
	private final BloodBankService bloodBankService;
	
	@PostMapping("/register")
	public ResponseEntity<BloodBankDto> registerBloodBank(@RequestBody BloodBankDto bloodBankDto)
	{
		BloodBankDto registerBank = bloodBankService.bloodBankService(bloodBankDto);
		
		
		return ResponseEntity.status(HttpStatus.CREATED).body(registerBank);
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> loginBloodBank(@RequestBody BloodBankLoginDto bloodBankLoginDto) {
	    try {
	        BloodBankDto loggedInBank = bloodBankService.bloodBankLoginService(bloodBankLoginDto);
	        return ResponseEntity.ok(loggedInBank);
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
	    }
	}


}
