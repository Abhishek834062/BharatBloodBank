package com.bharatbloodbank.controller;

import com.bharatbloodbank.dto.request.VerifyUserRequest;
import com.bharatbloodbank.dto.response.ApiResponse;
import com.bharatbloodbank.dto.response.BloodBankResponse;
import com.bharatbloodbank.dto.response.DoctorResponse;
import com.bharatbloodbank.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/dashboard
     * Returns stats: total banks, doctors, pending approvals
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        return ResponseEntity.ok(
            ApiResponse.success("Dashboard stats", adminService.getDashboardStats()));
    }

    // ==================== BLOOD BANK MANAGEMENT ====================

    /**
     * GET /api/admin/blood-banks
     * List all registered blood banks
     */
    @GetMapping("/blood-banks")
    public ResponseEntity<ApiResponse<List<BloodBankResponse>>> getAllBloodBanks() {
        return ResponseEntity.ok(
            ApiResponse.success("All blood banks", adminService.getAllBloodBanks()));
    }

    /**
     * GET /api/admin/blood-banks/pending
     * List all pending blood bank registrations
     */
    @GetMapping("/blood-banks/pending")
    public ResponseEntity<ApiResponse<List<BloodBankResponse>>> getPendingBloodBanks() {
        return ResponseEntity.ok(
            ApiResponse.success("Pending blood banks", adminService.getPendingBloodBanks()));
    }

    /**
     * PUT /api/admin/blood-banks/{userId}/verify
     * Approve or reject a blood bank
     * Body: { "status": "APPROVED" | "REJECTED", "rejectionReason": "..." }
     */
    @PutMapping("/blood-banks/{userId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyBloodBank(
        @PathVariable Long userId,
        @Valid @RequestBody VerifyUserRequest request) {
        String result = adminService.verifyBloodBank(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ==================== DOCTOR MANAGEMENT ====================

    /**
     * GET /api/admin/doctors
     * List all registered doctors
     */
    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        return ResponseEntity.ok(
            ApiResponse.success("All doctors", adminService.getAllDoctors()));
    }

    /**
     * GET /api/admin/doctors/pending
     * List all pending doctor registrations
     */
    @GetMapping("/doctors/pending")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getPendingDoctors() {
        return ResponseEntity.ok(
            ApiResponse.success("Pending doctors", adminService.getPendingDoctors()));
    }

    /**
     * PUT /api/admin/doctors/{userId}/verify
     * Approve or reject a doctor
     * Body: { "status": "APPROVED" | "REJECTED", "rejectionReason": "..." }
     */
    @PutMapping("/doctors/{userId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyDoctor(
        @PathVariable Long userId,
        @Valid @RequestBody VerifyUserRequest request) {
        String result = adminService.verifyDoctor(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * PUT /api/admin/users/{userId}/toggle-status
     * Enable or disable any user account
     */
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(@PathVariable Long userId) {
        String result = adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
