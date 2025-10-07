package com.BharatBlood.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BharatBlood.entity.BloodComponentEntity;

public interface BloodComponentRepository extends JpaRepository<BloodComponentEntity, Long> {
	List<BloodComponentEntity> findByBloodBankId(Long bankId);

}
