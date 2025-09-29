package com.BharatBlood.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BharatBlood.entity.BloodBankEntity;
import java.util.Optional;


public interface BloodBankRepository extends JpaRepository<BloodBankEntity, Long>{

	Optional <BloodBankEntity> findByEmail(String email);
	
}
