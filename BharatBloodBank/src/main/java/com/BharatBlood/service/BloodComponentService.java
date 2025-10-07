package com.BharatBlood.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.BharatBlood.dto.BloodComponentDto;
import com.BharatBlood.entity.BloodBankEntity;
import com.BharatBlood.entity.BloodComponentEntity;
import com.BharatBlood.repository.BloodBankRepository;
import com.BharatBlood.repository.BloodComponentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BloodComponentService {
	
	private final BloodComponentRepository bloodComponentRepository;
	private final BloodBankRepository bloodBankRepository;
	
    public BloodComponentDto saveBloodComponent(BloodComponentDto dto) {
    	
    
    	BloodBankEntity bank = bloodBankRepository.findById(dto.getBankId())
    			.orElseThrow(() -> new RuntimeException("Bank not found with ID:- " + dto.getBankId()));
    	
    	
    	BloodComponentEntity entity = BloodComponentEntity.builder()
    			.type(dto.getType())
    			.bloodGroup(dto.getBloodGroup())
    			.volumeInMl(dto.getVolumeInMl())
    			.expiryDate(dto.getExpiryDate())
    			.bloodBank(bank)
    			.build();
    
    	BloodComponentEntity saved = bloodComponentRepository.save(entity);
    	
    	return toDto(saved);
    }
    
    public List<BloodComponentDto> getAllComponents() {
        return bloodComponentRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<BloodComponentDto> getComponentsByBank(Long bankId) {
        return bloodComponentRepository.findByBloodBankId(bankId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private BloodComponentDto toDto(BloodComponentEntity entity) {
    	return BloodComponentDto.builder()
    			.id(entity.getId())
    			.type(entity.getType())
    			.bloodGroup(entity.getBloodGroup())
    			.volumeInMl(entity.getVolumeInMl())
    			.expiryDate(entity.getExpiryDate())
    			.collectionDate(entity.getCollectionDate())
    			.bankId(entity.getBloodBank().getId())
    			.build();
    }
}
