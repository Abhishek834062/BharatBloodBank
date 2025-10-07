package com.BharatBlood.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Blood_component")
public class BloodComponentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private ComponentType type;
	
	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private BloodGroup bloodGroup;
	
	@Column(nullable = false)
	private Long volumeInMl;
	
	@Column(updatable = false)
	@CreationTimestamp
    private LocalDate collectionDate;
	private LocalDate expiryDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bank_id", nullable = false)
    private BloodBankEntity bloodBank;
}
