
package com.bharatbloodbank.service;

import com.bharatbloodbank.dto.request.VerifyUserRequest;
import com.bharatbloodbank.dto.response.BloodBankResponse;
import com.bharatbloodbank.dto.response.DoctorResponse;
import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.Doctor;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.Role;
import com.bharatbloodbank.enums.VerificationStatus;
import com.bharatbloodbank.exception.BusinessException;
import com.bharatbloodbank.exception.ResourceNotFoundException;
import com.bharatbloodbank.repository.BloodBankRepository;
import com.bharatbloodbank.repository.DoctorRepository;
import com.bharatbloodbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final BloodBankRepository bloodBankRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    // ===================== PENDING BLOOD BANKS =====================
    @Transactional(readOnly = true)
    public List<BloodBankResponse> getPendingBloodBanks() {
        List<User> pendingUsers = userRepository.findByRoleAndVerificationStatus(
            Role.BLOOD_BANK, VerificationStatus.PENDING);
        return pendingUsers.stream()
            .map(u -> bloodBankRepository.findByUser(u).orElseThrow())
            .map(this::mapToBloodBankResponse)
            .toList();
    }

    // ===================== PENDING DOCTORS =====================
    @Transactional(readOnly = true)
    public List<DoctorResponse> getPendingDoctors() {
        List<User> pendingUsers = userRepository.findByRoleAndVerificationStatus(
            Role.DOCTOR, VerificationStatus.PENDING);
        return pendingUsers.stream()
            .map(u -> doctorRepository.findByUser(u).orElseThrow())
            .map(this::mapToDoctorResponse)
            .toList();
    }

    // ===================== ALL BLOOD BANKS =====================
    @Transactional(readOnly = true)
    public List<BloodBankResponse> getAllBloodBanks() {
        return bloodBankRepository.findAll().stream()
            .map(this::mapToBloodBankResponse)
            .toList();
    }

    // ===================== ALL DOCTORS =====================
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
            .map(this::mapToDoctorResponse)
            .toList();
    }

    // ===================== VERIFY BLOOD BANK =====================
    @Transactional
    public String verifyBloodBank(Long userId, VerifyUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() != Role.BLOOD_BANK) {
            throw new BusinessException("This user is not a Blood Bank");
        }
        if (user.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BusinessException("User is not in PENDING state");
        }

        if (request.getStatus() == VerificationStatus.REJECTED
            && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new BusinessException("Rejection reason is required when rejecting");
        }

        user.setVerificationStatus(request.getStatus());
        user.setRejectionReason(request.getRejectionReason());
        userRepository.save(user);

        BloodBank bank = bloodBankRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank profile not found"));

        if (request.getStatus() == VerificationStatus.APPROVED) {
            emailService.sendApprovalEmail(
                bank.getContactEmail(), bank.getBankName(),
                user.getEmail(), "(use your registered password)");
            log.info("Blood Bank APPROVED: {}", bank.getBankName());
            return "Blood Bank approved successfully. Credentials sent via email.";
        } else {
            emailService.sendRejectionEmail(
                bank.getContactEmail(), bank.getBankName(), request.getRejectionReason());
            log.info("Blood Bank REJECTED: {}", bank.getBankName());
            return "Blood Bank rejected. Notification sent.";
        }
    }

    // ===================== VERIFY DOCTOR =====================
    @Transactional
    public String verifyDoctor(Long userId, VerifyUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() != Role.DOCTOR) {
            throw new BusinessException("This user is not a Doctor");
        }
        if (user.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new BusinessException("User is not in PENDING state");
        }

        if (request.getStatus() == VerificationStatus.REJECTED
            && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new BusinessException("Rejection reason is required when rejecting");
        }

        user.setVerificationStatus(request.getStatus());
        user.setRejectionReason(request.getRejectionReason());
        userRepository.save(user);

        Doctor doctor = doctorRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        if (request.getStatus() == VerificationStatus.APPROVED) {
            emailService.sendApprovalEmail(
                user.getEmail(), doctor.getDoctorName(),
                user.getEmail(), "(use your registered password)");
            log.info("Doctor APPROVED: {}", doctor.getDoctorName());
            return "Doctor approved successfully. Credentials sent via email.";
        } else {
            emailService.sendRejectionEmail(
                user.getEmail(), doctor.getDoctorName(), request.getRejectionReason());
            log.info("Doctor REJECTED: {}", doctor.getDoctorName());
            return "Doctor rejected. Notification sent.";
        }
    }

    // ===================== DASHBOARD STATS =====================
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        long totalBanks = bloodBankRepository.count();
        long totalDoctors = doctorRepository.count();
        long pendingBanks = userRepository.findByRoleAndVerificationStatus(
            Role.BLOOD_BANK, VerificationStatus.PENDING).size();
        long pendingDoctors = userRepository.findByRoleAndVerificationStatus(
            Role.DOCTOR, VerificationStatus.PENDING).size();

        return Map.of(
            "totalBloodBanks", totalBanks,
            "totalDoctors", totalDoctors,
            "pendingBloodBankApprovals", pendingBanks,
            "pendingDoctorApprovals", pendingDoctors
        );
    }

    // ===================== DISABLE / ENABLE USER =====================
    @Transactional
    public String toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot disable admin accounts");
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        String status = user.isEnabled() ? "enabled" : "disabled";
        log.info("User {} has been {}", user.getEmail(), status);
        return "User account has been " + status;
    }

    // ===================== MAPPERS =====================
    private BloodBankResponse mapToBloodBankResponse(BloodBank bank) {
        return BloodBankResponse.builder()
            .id(bank.getId())
            .userId(bank.getUser().getId())
            .bankName(bank.getBankName())
            .licenseNumber(bank.getLicenseNumber())
            .address(bank.getAddress())
            .city(bank.getCity())
            .state(bank.getState())
            .pincode(bank.getPincode())
            .contactPhone(bank.getContactPhone())
            .contactEmail(bank.getContactEmail())
            .verificationStatus(bank.getUser().getVerificationStatus())
            .enabled(bank.getUser().isEnabled())
            .registeredAt(bank.getRegisteredAt())
            .build();
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
            .id(doctor.getId())
            .userId(doctor.getUser().getId())
            .doctorName(doctor.getDoctorName())
            .specialization(doctor.getSpecialization())
            .medicalRegistrationNumber(doctor.getMedicalRegistrationNumber())
            .hospitalName(doctor.getHospitalName())
            .hospitalAddress(doctor.getHospitalAddress())
            .city(doctor.getCity())
            .state(doctor.getState())
            .email(doctor.getUser().getEmail())
            .phone(doctor.getUser().getPhone())
            .verificationStatus(doctor.getUser().getVerificationStatus())
            .enabled(doctor.getUser().isEnabled())
            .registeredAt(doctor.getRegisteredAt())
            .build();
    }
}
