package com.bharatbloodbank.controller;

import com.bharatbloodbank.dto.request.*;
import com.bharatbloodbank.dto.response.ApiResponse;
import com.bharatbloodbank.dto.response.LoginResponse;
import com.bharatbloodbank.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Body: { "email": "...", "password": "..." }
     * Roles: ALL
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/auth/register/blood-bank
     * Body: BloodBankRegisterRequest
     * Roles: PUBLIC (no auth required)
     */
    @PostMapping("/register/blood-bank")
    public ResponseEntity<ApiResponse<String>> registerBloodBank(
        @Valid @RequestBody BloodBankRegisterRequest request) {
        String message = authService.registerBloodBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(message));
    }

    /**
     * POST /api/auth/register/doctor
     * Body: DoctorRegisterRequest
     * Roles: PUBLIC (no auth required)
     */
    @PostMapping("/register/doctor")
    public ResponseEntity<ApiResponse<String>> registerDoctor(
        @Valid @RequestBody DoctorRegisterRequest request) {
        String message = authService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(message));
    }

    /**
     * POST /api/auth/forgot-password
     * Body: { "email": "..." }
     * Roles: PUBLIC
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * POST /api/auth/reset-password
     * Body: { "token": "...", "newPassword": "..." }
     * Roles: PUBLIC
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }
}
