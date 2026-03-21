
package com.bharatbloodbank.service;

import com.bharatbloodbank.dto.request.ConfirmDonationRequest;
import com.bharatbloodbank.dto.response.DonationRecordResponse;
import com.bharatbloodbank.entity.*;
import com.bharatbloodbank.enums.*;
import com.bharatbloodbank.exception.BusinessException;
import com.bharatbloodbank.exception.DonorNotEligibleException;
import com.bharatbloodbank.exception.ResourceNotFoundException;
import com.bharatbloodbank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private final DonationRecordRepository donationRecordRepository;
    private final BloodBankRepository bloodBankRepository;
    private final DonorRepository donorRepository;
    private final BloodUnitRepository bloodUnitRepository;
    private final BloodRequestRepository bloodRequestRepository;

    @Value("${app.donor.eligibility.gap-days}")
    private int eligibilityGapDays;

    // ===================== CONFIRM DONATION + AUTO SPLIT =====================
    @Transactional
    public DonationRecordResponse confirmDonation(ConfirmDonationRequest request, User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));

        Donor donor = donorRepository.findById(request.getDonorId())
            .orElseThrow(() -> new ResourceNotFoundException("Donor", request.getDonorId()));

        // Verify donor belongs to this bank
        if (!donor.getAddedByBank().getId().equals(bank.getId())
            && !donor.getLinkedBanks().contains(bank)) {
            throw new BusinessException("This donor is not linked to your blood bank");
        }

        // Eligibility check - 3 months gap
        if (donor.getLastDonationDate() != null) {
            LocalDate nextEligible = donor.getLastDonationDate().plusDays(eligibilityGapDays);
            if (!request.getDonationDate().isAfter(nextEligible.minusDays(1))) {
                throw new DonorNotEligibleException(
                    "Donor is not eligible to donate yet. Next eligible date: " + nextEligible);
            }
        }

        // Link to blood request if provided
        BloodRequest linkedRequest = null;
        if (request.getLinkedRequestId() != null) {
            linkedRequest = bloodRequestRepository.findById(request.getLinkedRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "BloodRequest", request.getLinkedRequestId()));
        }

        // Create donation record
        DonationRecord record = DonationRecord.builder()
            .donor(donor)
            .bloodBank(bank)
            .donationDate(request.getDonationDate())
            .bloodGroup(donor.getBloodGroup())
            .volumeMl(450.0)
            .status(DonationStatus.CONFIRMED)
            .splitDone(false)
            .linkedRequest(linkedRequest)
            .notes(request.getNotes())
            .build();
        donationRecordRepository.save(record);

        // Auto-split whole blood into components
        splitIntoComponents(record, bank);

        // Update donor's last donation date & eligibility
        donor.setLastDonationDate(request.getDonationDate());
        donor.setEligible(false); // Will become eligible after 90 days (scheduler handles)
        donorRepository.save(donor);

        log.info("Donation confirmed and split for donor: {} at bank: {}",
            donor.getName(), bank.getBankName());
        return mapToResponse(record);
    }

    // ===================== AUTO SPLIT: 1 whole blood → 3 components =====================
    private void splitIntoComponents(DonationRecord record, BloodBank bank) {
        List<BloodUnit> units = new ArrayList<>();
        LocalDate donationDate = record.getDonationDate();

        for (BloodComponent component : BloodComponent.values()) {
            LocalDate expiryDate = donationDate.plusDays(component.getExpiryDays());

            BloodUnit unit = BloodUnit.builder()
                .bloodBank(bank)
                .bloodGroup(record.getBloodGroup())
                .component(component)
                .donationRecord(record)
                .donationDate(donationDate)
                .expiryDate(expiryDate)
                .status(BloodUnitStatus.AVAILABLE)
                .build();
            units.add(unit);
        }

        bloodUnitRepository.saveAll(units);

        record.setSplitDone(true);
        record.setStatus(DonationStatus.SPLIT_DONE);
        donationRecordRepository.save(record);

        log.info("Split donation #{} into {} components (RBC exp:{}, Plasma exp:{}, Platelets exp:{})",
            record.getId(),
            units.size(),
            donationDate.plusDays(BloodComponent.RBC.getExpiryDays()),
            donationDate.plusDays(BloodComponent.PLASMA.getExpiryDays()),
            donationDate.plusDays(BloodComponent.PLATELETS.getExpiryDays())
        );
    }

    // ===================== GET ALL DONATIONS FOR BANK =====================
    @Transactional(readOnly = true)
    public List<DonationRecordResponse> getDonationsByBank(User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        return donationRecordRepository.findByBloodBank(bank).stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ===================== GET DONATION BY ID =====================
    @Transactional(readOnly = true)
    public DonationRecordResponse getDonationById(Long id, User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        DonationRecord record = donationRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("DonationRecord", id));
        if (!record.getBloodBank().getId().equals(bank.getId())) {
            throw new BusinessException("This donation does not belong to your bank");
        }
        return mapToResponse(record);
    }

    // ===================== GET DONATIONS FOR A DONOR =====================
    public List<DonationRecordResponse> getDonationsByDonor(Long donorId, User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        Donor donor = donorRepository.findById(donorId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor", donorId));
        return donationRecordRepository.findByDonor(donor).stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ===================== MAPPER =====================
    public DonationRecordResponse mapToResponse(DonationRecord r) {
        return DonationRecordResponse.builder()
            .id(r.getId())
            .donorId(r.getDonor().getId())
            .donorName(r.getDonor().getName())
            .donorPhone(r.getDonor().getPhone())
            .bloodBankId(r.getBloodBank().getId())
            .bloodBankName(r.getBloodBank().getBankName())
            .donationDate(r.getDonationDate())
            .bloodGroup(r.getBloodGroup())
            .volumeMl(r.getVolumeMl())
            .status(r.getStatus())
            .splitDone(r.isSplitDone())
            .linkedRequestId(r.getLinkedRequest() != null ? r.getLinkedRequest().getId() : null)
            .notes(r.getNotes())
            .confirmedAt(r.getConfirmedAt())
            .build();
    }
}
