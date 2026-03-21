
package com.bharatbloodbank.service;

import com.bharatbloodbank.dto.request.AddDonorRequest;
import com.bharatbloodbank.dto.response.DonorResponse;
import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.Donor;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.exception.BusinessException;
import com.bharatbloodbank.exception.ResourceNotFoundException;
import com.bharatbloodbank.repository.BloodBankRepository;
import com.bharatbloodbank.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {

    private final DonorRepository donorRepository;
    private final BloodBankRepository bloodBankRepository;

    @Value("${app.donor.eligibility.gap-days}")
    private int eligibilityGapDays;

    // ===================== ADD DONOR =====================
    @Transactional
    public DonorResponse addDonor(AddDonorRequest request, User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));

        // Prevent duplicate donor phone in same bank
        if (donorRepository.existsByPhoneAndAddedByBank(request.getPhone(), bank)) {
            throw new BusinessException(
                "Donor with phone " + request.getPhone() + " already exists in this bank");
        }

        Donor donor = Donor.builder()
            .name(request.getName())
            .phone(request.getPhone())
            .email(request.getEmail())
            .address(request.getAddress())
            .bloodGroup(request.getBloodGroup())
            .dateOfBirth(request.getDateOfBirth())
            .gender(request.getGender())
            .addedByBank(bank)
            .eligible(true)
            .build();
        donor.getLinkedBanks().add(bank);

        donorRepository.save(donor);
        log.info("Donor added: {} by bank: {}", donor.getName(), bank.getBankName());
        return mapToResponse(donor);
    }

    // ===================== GET ALL DONORS FOR BANK =====================
    @Transactional(readOnly = true)
    public List<DonorResponse> getDonorsByBank(User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        return donorRepository.findByAddedByBank(bank).stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ===================== GET ELIGIBLE DONORS BY BLOOD GROUP =====================
    @Transactional(readOnly = true)
    public List<DonorResponse> getEligibleDonorsByBloodGroup(User bankUser, BloodGroup bloodGroup) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        return donorRepository.findEligibleDonorsByBankAndBloodGroup(bank, bloodGroup).stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ===================== GET DONOR BY ID =====================
    @Transactional(readOnly = true)
    public DonorResponse getDonorById(Long donorId, User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor", donorId));

        // Ensure this donor belongs to this bank
        if (!donor.getAddedByBank().getId().equals(bank.getId())
            && !donor.getLinkedBanks().contains(bank)) {
            throw new BusinessException("Donor not found in your bank");
        }
        return mapToResponse(donor);
    }

    // ===================== UPDATE ELIGIBILITY (scheduled) =====================
    @Transactional
    public void refreshDonorEligibility() {
        List<Donor> allDonors = donorRepository.findAll();
        LocalDate today = LocalDate.now();
        int updated = 0;

        for (Donor donor : allDonors) {
            if (donor.getLastDonationDate() == null) {
                if (!donor.isEligible()) {
                    donor.setEligible(true);
                    updated++;
                }
            } else {
                boolean shouldBeEligible = donor.getLastDonationDate()
                    .plusDays(eligibilityGapDays).isBefore(today);
                if (donor.isEligible() != shouldBeEligible) {
                    donor.setEligible(shouldBeEligible);
                    updated++;
                }
            }
        }

        if (updated > 0) {
            donorRepository.saveAll(allDonors);
            log.info("Refreshed eligibility for {} donors", updated);
        }
    }

    // ===================== CHECK ELIGIBILITY =====================
    public boolean checkEligibility(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor", donorId));
        if (donor.getLastDonationDate() == null) return true;
        return donor.getLastDonationDate().plusDays(eligibilityGapDays)
            .isBefore(LocalDate.now());
    }

    // ===================== GET DONOR ENTITY (internal) =====================
    public Donor getDonorEntity(Long donorId) {
        return donorRepository.findById(donorId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor", donorId));
    }

    // ===================== MAPPER =====================
    public DonorResponse mapToResponse(Donor donor) {
        LocalDate nextEligible = donor.getLastDonationDate() != null
            ? donor.getLastDonationDate().plusDays(eligibilityGapDays)
            : null;

        return DonorResponse.builder()
            .id(donor.getId())
            .name(donor.getName())
            .phone(donor.getPhone())
            .email(donor.getEmail())
            .address(donor.getAddress())
            .bloodGroup(donor.getBloodGroup())
            .dateOfBirth(donor.getDateOfBirth())
            .gender(donor.getGender())
            .lastDonationDate(donor.getLastDonationDate())
            .eligible(donor.isEligible())
            .nextEligibleDate(nextEligible)
            .addedByBankId(donor.getAddedByBank().getId())
            .addedByBankName(donor.getAddedByBank().getBankName())
            .createdAt(donor.getCreatedAt())
            .build();
    }
}
