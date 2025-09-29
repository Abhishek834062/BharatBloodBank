package com.BharatBlood.service;

import org.springframework.stereotype.Service;

import com.BharatBlood.dto.BloodBankDto;
import com.BharatBlood.entity.BloodBankEntity;
import com.BharatBlood.repository.BloodBankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BloodBankService {

	private final  BloodBankRepository bloodBankRepository;
	
	public BloodBankDto bloodBankService(BloodBankDto bloodBankDto)
	{
		BloodBankEntity newBloodBankEntity=toEntity(bloodBankDto);
		newBloodBankEntity=bloodBankRepository.save(newBloodBankEntity);
		
		return toDto(newBloodBankEntity);
	}
	
	public BloodBankEntity toEntity(BloodBankDto bloodBankDto)
	{
		
		return BloodBankEntity.builder()
				.id(bloodBankDto.getId())
				.bankName(bloodBankDto.getBankName())
				.location(bloodBankDto.getLocation())
				.email(bloodBankDto.getEmail())
				.password(bloodBankDto.getPassword())
				.build();
				
	}
	
	public BloodBankDto toDto(BloodBankEntity bloodBankEntity)
	{
		
		return BloodBankDto.builder()
				.id(bloodBankEntity.getId())
				.bankName(bloodBankEntity.getBankName())
				.location(bloodBankEntity.getLocation())
				.email(bloodBankEntity.getEmail())
				.password(bloodBankEntity.getPassword())
				.build();
	}
	
}
