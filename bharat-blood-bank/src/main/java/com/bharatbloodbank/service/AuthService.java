
package com.bharatbloodbank.service;

import com.bharatbloodbank.dto.request.*;
import com.bharatbloodbank.dto.response.LoginResponse;
import com.bharatbloodbank.entity.*;
import com.bharatbloodbank.enums.Role;
import com.bharatbloodbank.enums.VerificationStatus;
import com.bharatbloodbank.exception.BusinessException;
import com.bharatbloodbank.exception.ResourceNotFoundException;
import com.bharatbloodbank.repository.*;
import com.bharatbloodbank.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BloodBankRepository bloodBankRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Value("${app.jwt.reset-token-expiry-minutes}")
    private int resetTokenExpiryMinutes;

    // ===================== LOGIN =====================
    public LoginResponse login(LoginRequest request) {
        // First check if user exists and is approved BEFORE Spring Security auth
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("No account found with this email"));

        if (!user.isEnabled()) {
            throw new BusinessException("Your account has been disabled. Contact admin.");
        }

        if (user.getVerificationStatus() != null
            && user.getVerificationStatus() == VerificationStatus.PENDING) {
            throw new BusinessException(
                "Your account is pending admin approval. You will receive an email once approved.");
        }

        if (user.getVerificationStatus() != null
            && user.getVerificationStatus() == VerificationStatus.REJECTED) {
            throw new BusinessException(
                "Your registration was rejected. Reason: "
                + (user.getRejectionReason() != null ? user.getRejectionReason() : "Contact admin"));
        }

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtils.generateToken(authentication);

        return LoginResponse.builder()
            .token(token)
            .type("Bearer")
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole())
            .build();
    }

    // ===================== BLOOD BANK REGISTER =====================
    @Transactional
    public String registerBloodBank(BloodBankRegisterRequest request) {
        if (userRepository.existsByEmail(request.getLoginEmail())) {
            throw new BusinessException("Email already registered: " + request.getLoginEmail());
        }
        if (bloodBankRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new BusinessException("License number already registered: "
                + request.getLicenseNumber());
        }

        User user = User.builder()
            .email(request.getLoginEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getBankName())
            .phone(request.getContactPhone())
            .role(Role.BLOOD_BANK)
            .verificationStatus(VerificationStatus.PENDING)
            .enabled(true)
            .build();
        userRepository.save(user);

        BloodBank bank = BloodBank.builder()
            .user(user)
            .bankName(request.getBankName())
            .licenseNumber(request.getLicenseNumber())
            .address(request.getAddress())
            .city(request.getCity())
            .state(request.getState())
            .pincode(request.getPincode())
            .contactPhone(request.getContactPhone())
            .contactEmail(request.getContactEmail())
            .build();
        bloodBankRepository.save(bank);

        // Notify the registering email
        emailService.sendWelcomeRegistrationEmail(
            request.getContactEmail(), request.getBankName(), "Blood Bank");

        log.info("Blood Bank registered (PENDING): {}", request.getBankName());
        return "Blood Bank registered successfully. Awaiting admin approval.";
    }

    // ===================== DOCTOR REGISTER =====================
    @Transactional
    public String registerDoctor(DoctorRegisterRequest request) {
        if (userRepository.existsByEmail(request.getLoginEmail())) {
            throw new BusinessException("Email already registered: " + request.getLoginEmail());
        }
        if (doctorRepository.existsByMedicalRegistrationNumber(
            request.getMedicalRegistrationNumber())) {
            throw new BusinessException("Medical Registration Number already exists: "
                + request.getMedicalRegistrationNumber());
        }

        User user = User.builder()
            .email(request.getLoginEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getDoctorName())
            .phone(request.getPhone())
            .role(Role.DOCTOR)
            .verificationStatus(VerificationStatus.PENDING)
            .enabled(true)
            .build();
        userRepository.save(user);

        Doctor doctor = Doctor.builder()
            .user(user)
            .doctorName(request.getDoctorName())
            .specialization(request.getSpecialization())
            .medicalRegistrationNumber(request.getMedicalRegistrationNumber())
            .hospitalName(request.getHospitalName())
            .hospitalAddress(request.getHospitalAddress())
            .city(request.getCity())
            .state(request.getState())
            .pincode(request.getPincode())
            .build();
        doctorRepository.save(doctor);

        emailService.sendWelcomeRegistrationEmail(
            request.getLoginEmail(), request.getDoctorName(), "Doctor");

        log.info("Doctor registered (PENDING): {}", request.getDoctorName());
        return "Doctor registered successfully. Awaiting admin approval.";
    }

    // ===================== FORGOT PASSWORD =====================
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No account found with email: " + request.getEmail()));

        // Invalidate old tokens for this user
        resetTokenRepository.findByUserAndUsedFalse(user)
            .ifPresent(old -> {
                old.setUsed(true);
                resetTokenRepository.save(old);
            });

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(tokenValue)
            .user(user)
            .expiryDate(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
            .used(false)
            .build();
        resetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), tokenValue);
        return "Password reset link sent to your email.";
    }

    // ===================== RESET PASSWORD =====================
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = resetTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (token.isUsed()) {
            throw new BusinessException("This reset link has already been used");
        }
        if (token.isExpired()) {
            throw new BusinessException("Reset link has expired. Please request a new one.");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);

        log.info("Password reset successful for user: {}", user.getEmail());
        return "Password reset successful. You can now login with your new password.";
    }

    // ===================== GET CURRENT USER =====================
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
