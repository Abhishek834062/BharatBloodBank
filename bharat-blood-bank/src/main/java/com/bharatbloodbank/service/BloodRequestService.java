
package com.bharatbloodbank.service;

import com.bharatbloodbank.dto.request.BloodRequestCreateRequest;
import com.bharatbloodbank.dto.request.HandleRequestRequest;
import com.bharatbloodbank.dto.response.BloodRequestResponse;
import com.bharatbloodbank.entity.*;
import com.bharatbloodbank.enums.*;
import com.bharatbloodbank.exception.BusinessException;
import com.bharatbloodbank.exception.InsufficientInventoryException;
import com.bharatbloodbank.exception.ResourceNotFoundException;
import com.bharatbloodbank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final BloodBankRepository bloodBankRepository;
    private final DoctorRepository doctorRepository;
    private final DonorRepository donorRepository;
    private final BloodUnitRepository bloodUnitRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final EmailService emailService;

    // ===================== DOCTOR: CREATE REQUEST =====================
    @Transactional
    public BloodRequestResponse createRequest(BloodRequestCreateRequest req, User doctorUser) {
        Doctor doctor = doctorRepository.findByUser(doctorUser)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        BloodBank bank = bloodBankRepository.findById(req.getBloodBankId())
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", req.getBloodBankId()));

        if (bank.getUser().getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new BusinessException("Selected blood bank is not active");
        }

        // Emergency requires a reason
        if (req.isEmergency() && (req.getEmergencyReason() == null
            || req.getEmergencyReason().isBlank())) {
            throw new BusinessException("Emergency reason is required for emergency requests");
        }

        BloodRequest request = BloodRequest.builder()
            .doctor(doctor)
            .bloodBank(bank)
            .bloodGroup(req.getBloodGroup())
            .component(req.getComponent())
            .unitsRequired(req.getUnitsRequired())
            .patientName(req.getPatientName())
            .patientCondition(req.getPatientCondition())
            .emergency(req.isEmergency())
            .emergencyReason(req.getEmergencyReason())
            .status(RequestStatus.PENDING)
            .build();
        bloodRequestRepository.save(request);

        // Notify blood bank via email
        emailService.sendBloodRequestNotification(
            bank.getContactEmail(), bank.getBankName(),
            doctor.getDoctorName(), doctor.getHospitalName(),
            req.getBloodGroup().getDisplayName(),
            req.getComponent().getDisplayName(),
            req.getUnitsRequired(), req.isEmergency());

        log.info("Blood request #{} created by Dr.{} to bank:{}",
            request.getId(), doctor.getDoctorName(), bank.getBankName());
        return mapToResponse(request);
    }

    // ===================== BANK: HANDLE REQUEST =====================
    @Transactional
    public BloodRequestResponse handleRequest(Long requestId,
                                               HandleRequestRequest req,
                                               User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));

        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        if (!request.getBloodBank().getId().equals(bank.getId())) {
            throw new BusinessException("This request does not belong to your bank");
        }

        switch (req.getAction().toUpperCase()) {

            case "ACCEPT" -> handleAccept(request, req);

            case "REJECT" -> handleReject(request, req);

            case "VERIFY_EMERGENCY" -> handleVerifyEmergency(request, req, bank);

            case "CONTACT_DONOR" -> handleContactDonor(request, req, bank);

            case "FULFILL" -> handleFulfill(request, req, bank);

            default -> throw new BusinessException(
                "Unknown action: " + req.getAction()
                    + ". Valid: ACCEPT, REJECT, VERIFY_EMERGENCY, CONTACT_DONOR, FULFILL");
        }

        bloodRequestRepository.save(request);

        // Notify doctor of status change
        emailService.sendRequestStatusUpdateToDoctor(
            request.getDoctor().getUser().getEmail(),
            request.getDoctor().getDoctorName(),
            request.getStatus().name(),
            request.getBloodGroup().getDisplayName(),
            bank.getBankName(),
            request.getBankNotes());

        return mapToResponse(request);
    }

    // ── ACCEPT ──────────────────────────────────────────────────────
    private void handleAccept(BloodRequest request, HandleRequestRequest req) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("Request must be PENDING to accept. Current: "
                + request.getStatus());
        }
        if (request.isEmergency()) {
            // Emergency: go to bank verification step
            request.setStatus(RequestStatus.EMERGENCY_PENDING_VERIFY);
        } else {
            request.setStatus(RequestStatus.ACCEPTED);
        }
        request.setBankNotes(req.getBankNotes());
        log.info("Request #{} accepted", request.getId());
    }

    // ── REJECT ──────────────────────────────────────────────────────
    private void handleReject(BloodRequest request, HandleRequestRequest req) {
        if (request.getStatus() == RequestStatus.FULFILLED
            || request.getStatus() == RequestStatus.REJECTED) {
            throw new BusinessException("Cannot reject a request in state: " + request.getStatus());
        }
        if (req.getRejectionReason() == null || req.getRejectionReason().isBlank()) {
            throw new BusinessException("Rejection reason is required");
        }
        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(req.getRejectionReason());
        request.setBankNotes(req.getBankNotes());
        log.info("Request #{} rejected: {}", request.getId(), req.getRejectionReason());
    }

    // ── VERIFY EMERGENCY (bank confirms emergency is real) ───────────
    private void handleVerifyEmergency(BloodRequest request,
                                        HandleRequestRequest req,
                                        BloodBank bank) {
        if (request.getStatus() != RequestStatus.EMERGENCY_PENDING_VERIFY) {
            throw new BusinessException(
                "Request must be in EMERGENCY_PENDING_VERIFY state. Current: "
                    + request.getStatus());
        }
        if (!request.isEmergency()) {
            throw new BusinessException("This is not an emergency request");
        }

        // Check if inventory has enough units
        long available = bloodUnitRepository
            .countByBloodBankAndBloodGroupAndComponentAndStatus(
                bank, request.getBloodGroup(),
                request.getComponent(), BloodUnitStatus.AVAILABLE);

        if (available < request.getUnitsRequired()) {
            throw new InsufficientInventoryException(
                String.format("Insufficient inventory. Available: %d, Required: %d",
                    available, request.getUnitsRequired()));
        }

        request.setEmergencyVerifiedByBank(true);
        request.setStatus(RequestStatus.EMERGENCY_VERIFIED);
        request.setBankNotes(req.getBankNotes());
        log.info("Emergency verified for request #{}", request.getId());
    }

    // ── CONTACT DONOR (normal requests — arrange fresh donation) ─────
    private void handleContactDonor(BloodRequest request,
                                     HandleRequestRequest req,
                                     BloodBank bank) {
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new BusinessException(
                "Request must be ACCEPTED before contacting a donor. Current: "
                    + request.getStatus());
        }
        if (request.isEmergency()) {
            throw new BusinessException(
                "Emergency requests should be fulfilled from inventory, not donor contact");
        }
        if (req.getDonorId() == null) {
            throw new BusinessException("Donor ID is required for CONTACT_DONOR action");
        }

        Donor donor = donorRepository.findById(req.getDonorId())
            .orElseThrow(() -> new ResourceNotFoundException("Donor", req.getDonorId()));

        // ── RULE 1: Donor must belong to this bank ──────────────────
        if (!donor.getAddedByBank().getId().equals(bank.getId())
            && !donor.getLinkedBanks().contains(bank)) {
            throw new BusinessException("This donor is not linked to your bank");
        }

        // ── RULE 2: Donor must have at least 1 donation at this bank ─
        List<DonationRecord> donorHistory = donationRecordRepository
            .findByDonorAndBloodBank(donor, bank);
        if (donorHistory.isEmpty()) {
            throw new BusinessException(
                "Donor '" + donor.getName()
                    + "' has no confirmed donations at this bank yet. "
                    + "Please confirm their donation in the Donations section first.");
        }

        // NOTE: Blood group and eligibility checks removed.
        // Donor has already donated — stock is already in inventory.
        // This step just links the donor to the request for tracking purposes.

        // ── RULE 3: Donor must not be already linked to another active request ──
        List<Donor> alreadyContacted = donorRepository
            .findDonorsAlreadyContactedInActiveRequests(bank);
        if (alreadyContacted.stream().anyMatch(d -> d.getId().equals(donor.getId()))) {
            throw new BusinessException(
                "Donor '" + donor.getName()
                    + "' is already linked to another active request. "
                    + "Please select a different donor.");
        }

        request.setContactedDonor(donor);
        request.setStatus(RequestStatus.DONOR_CONTACT_PENDING);
        request.setBankNotes(req.getBankNotes());
        log.info("Donor {} ({}) contacted for request #{} ({})",
            donor.getName(), donor.getBloodGroup(),
            request.getId(), request.getBloodGroup());
    }

    // ── FULFILL ──────────────────────────────────────────────────────
    @Transactional
    protected void handleFulfill(BloodRequest request,
                                  HandleRequestRequest req,
                                  BloodBank bank) {

        // Normal path — donor MUST be connected first (DONOR_CONTACT_PENDING)
        if (!request.isEmergency() && request.getStatus() != RequestStatus.DONOR_CONTACT_PENDING) {
            throw new BusinessException(
                "Please contact a donor first using CONTACT_DONOR action. "
                    + "Current status: " + request.getStatus());
        }

        // Emergency path — must be EMERGENCY_VERIFIED
        if (request.isEmergency() && request.getStatus() != RequestStatus.EMERGENCY_VERIFIED) {
            throw new BusinessException(
                "Emergency request must be verified first. "
                    + "Current status: " + request.getStatus());
        }

        // ALWAYS deduct from inventory (FIFO — oldest expiry first)
        // Donor donation already added to stock when confirmed — no need to touch donation records
        deductFromInventory(request, bank);

        request.setStatus(RequestStatus.FULFILLED);
        request.setFulfilledAt(LocalDateTime.now());
        request.setBankNotes(req.getBankNotes() != null ? req.getBankNotes() : "Blood dispatched");
        log.info("Request #{} FULFILLED from inventory ({} {} x{})",
            request.getId(), request.getBloodGroup(),
            request.getComponent(), request.getUnitsRequired());
    }

    // ── DEDUCT INVENTORY (FIFO - use oldest expiring units first) ───
    private void deductFromInventory(BloodRequest request, BloodBank bank) {
        List<BloodUnit> units = bloodUnitRepository
            .findByBloodBankAndBloodGroupAndComponentAndStatusOrderByExpiryDateAsc(
                bank, request.getBloodGroup(),
                request.getComponent(), BloodUnitStatus.AVAILABLE);

        if (units.size() < request.getUnitsRequired()) {
            throw new InsufficientInventoryException(
                String.format("Not enough units. Available: %d, Required: %d",
                    units.size(), request.getUnitsRequired()));
        }

        List<BloodUnit> toDeduct = units.subList(0, request.getUnitsRequired());
        for (BloodUnit unit : toDeduct) {
            unit.setStatus(BloodUnitStatus.USED);
            unit.setUsedForRequest(request);
            unit.setUsedAt(LocalDateTime.now());
        }
        bloodUnitRepository.saveAll(toDeduct);
        log.info("Deducted {} units of {}-{} from inventory for request #{}",
            request.getUnitsRequired(), request.getBloodGroup(), request.getComponent(),
            request.getId());
    }

    // ── DEDUCT FROM FRESH DONATION UNITS ────────────────────────────
    private void deductFromDonationUnits(BloodRequest request, DonationRecord donationRecord) {
        List<BloodUnit> donationUnits = bloodUnitRepository.findByDonationRecord(donationRecord)
            .stream()
            .filter(u -> u.getComponent() == request.getComponent()
                && u.getBloodGroup() == request.getBloodGroup()
                && u.getStatus() == BloodUnitStatus.AVAILABLE)
            .toList();

        if (donationUnits.size() < request.getUnitsRequired()) {
            throw new InsufficientInventoryException(
                "Insufficient units from this donation for the request");
        }

        List<BloodUnit> toUse = donationUnits.subList(0, request.getUnitsRequired());
        for (BloodUnit unit : toUse) {
            unit.setStatus(BloodUnitStatus.USED);
            unit.setUsedForRequest(request);
            unit.setUsedAt(LocalDateTime.now());
        }
        bloodUnitRepository.saveAll(toUse);
    }

    // ===================== GET AVAILABLE DONORS FOR A REQUEST =====================
    // Returns donors who have donated at this bank and are not contacted in any active request
    @Transactional(readOnly = true)
    public List<com.bharatbloodbank.dto.response.DonorResponse> getAvailableDonorsForRequest(
            Long requestId, User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));
        if (!request.getBloodBank().getId().equals(bank.getId())) {
            throw new BusinessException("This request does not belong to your bank");
        }
        // Native SQL query takes bankId (Long), not bank object
        List<com.bharatbloodbank.entity.Donor> available =
            donorRepository.findAvailableEligibleDonors(bank.getId());

        log.info("Available donors for bank {} (id={}): {} found",
            bank.getBankName(), bank.getId(), available.size());

        return available.stream().map(d ->
            com.bharatbloodbank.dto.response.DonorResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .phone(d.getPhone())
                .bloodGroup(d.getBloodGroup())
                .eligible(d.isEligible())
                .lastDonationDate(d.getLastDonationDate())
                .addedByBankId(d.getAddedByBank().getId())
                .build()
        ).toList();
    }

    // ===================== DOCTOR: MY REQUESTS =====================
    @Transactional(readOnly = true)
    public List<BloodRequestResponse> getDoctorRequests(User doctorUser) {
        Doctor doctor = doctorRepository.findByUser(doctorUser)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        return bloodRequestRepository.findByDoctorOrderByRequestedAtDesc(doctor).stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ===================== DOCTOR: CANCEL REQUEST =====================
    @Transactional
    public BloodRequestResponse cancelRequest(Long requestId, User doctorUser) {
        Doctor doctor = doctorRepository.findByUser(doctorUser)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        if (!request.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("This request does not belong to you");
        }
        if (request.getStatus() == RequestStatus.FULFILLED
            || request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel a request in state: " + request.getStatus());
        }
        request.setStatus(RequestStatus.CANCELLED);
        bloodRequestRepository.save(request);
        return mapToResponse(request);
    }

    // ===================== BANK: GET REQUESTS =====================
    @Transactional(readOnly = true)
    public List<BloodRequestResponse> getBankRequests(User bankUser, String status) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        if (status != null && !status.isBlank()) {
            RequestStatus rs = RequestStatus.valueOf(status.toUpperCase());
            return bloodRequestRepository
                .findByBloodBankAndStatusOrderByRequestedAtDesc(bank, rs).stream()
                .map(this::mapToResponse).toList();
        }
        return bloodRequestRepository.findByBloodBankOrderByRequestedAtDesc(bank).stream()
            .map(this::mapToResponse).toList();
    }

    // ===================== BANK: EMERGENCY REQUESTS =====================
    @Transactional(readOnly = true)
    public List<BloodRequestResponse> getBankEmergencyRequests(User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        return bloodRequestRepository
            .findByBloodBankAndEmergencyTrueAndStatusOrderByRequestedAtDesc(
                bank, RequestStatus.EMERGENCY_PENDING_VERIFY)
            .stream().map(this::mapToResponse).toList();
    }

    // ===================== GET SINGLE REQUEST =====================
    @Transactional(readOnly = true)
    public BloodRequestResponse getRequestById(Long requestId, User currentUser) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));
        // Access control: doctor sees own, bank sees own
        boolean isDoctor = currentUser.getRole() == Role.DOCTOR;
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isAdmin) {
            if (isDoctor) {
                Doctor doctor = doctorRepository.findByUser(currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
                if (!request.getDoctor().getId().equals(doctor.getId())) {
                    throw new BusinessException("Access denied");
                }
            } else {
                BloodBank bank = bloodBankRepository.findByUser(currentUser)
                    .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
                if (!request.getBloodBank().getId().equals(bank.getId())) {
                    throw new BusinessException("Access denied");
                }
            }
        }
        return mapToResponse(request);
    }

    // ===================== MAPPER =====================
    public BloodRequestResponse mapToResponse(BloodRequest r) {
        return BloodRequestResponse.builder()
            .id(r.getId())
            .doctorId(r.getDoctor().getId())
            .doctorName(r.getDoctor().getDoctorName())
            .hospitalName(r.getDoctor().getHospitalName())
            .bloodBankId(r.getBloodBank().getId())
            .bloodBankName(r.getBloodBank().getBankName())
            .bloodGroup(r.getBloodGroup())
            .component(r.getComponent())
            .unitsRequired(r.getUnitsRequired())
            .patientName(r.getPatientName())
            .patientCondition(r.getPatientCondition())
            .emergency(r.isEmergency())
            .emergencyReason(r.getEmergencyReason())
            .emergencyVerifiedByBank(r.isEmergencyVerifiedByBank())
            .status(r.getStatus())
            .rejectionReason(r.getRejectionReason())
            .bankNotes(r.getBankNotes())
            .contactedDonorName(r.getContactedDonor() != null
                ? r.getContactedDonor().getName() : null)
            .contactedDonorPhone(r.getContactedDonor() != null
                ? r.getContactedDonor().getPhone() : null)
            .requestedAt(r.getRequestedAt())
            .updatedAt(r.getUpdatedAt())
            .fulfilledAt(r.getFulfilledAt())
            .build();
    }
}

