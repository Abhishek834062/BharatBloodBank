
package com.bharatbloodbank.controller;

import com.bharatbloodbank.dto.request.BloodRequestCreateRequest;
import com.bharatbloodbank.dto.request.HandleRequestRequest;
import com.bharatbloodbank.dto.response.ApiResponse;
import com.bharatbloodbank.dto.response.BloodRequestResponse;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.service.AuthService;
import com.bharatbloodbank.service.BloodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final AuthService authService;

    // ==================== DOCTOR ENDPOINTS ====================

    /**
     * POST /api/requests
     * Doctor creates a blood request to a specific bank
     * First check inventory via GET /api/inventory/public/bank/{id}/available
     * Body: BloodRequestCreateRequest
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> createRequest(
        @Valid @RequestBody BloodRequestCreateRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        User doctorUser = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.createRequest(request, doctorUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Blood request submitted successfully", response));
    }

    /**
     * GET /api/requests/doctor/my
     * Doctor views all their requests
     */
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getMyRequests(
        @AuthenticationPrincipal UserDetails userDetails) {
        User doctorUser = authService.getCurrentUser(userDetails.getUsername());
        List<BloodRequestResponse> requests = bloodRequestService.getDoctorRequests(doctorUser);
        return ResponseEntity.ok(ApiResponse.success("Your blood requests", requests));
    }

    /**
     * PUT /api/requests/{id}/cancel
     * Doctor cancels a pending request
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> cancelRequest(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        User doctorUser = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.cancelRequest(id, doctorUser);
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", response));
    }

    // ==================== BLOOD BANK ENDPOINTS ====================

    /**
     * GET /api/requests/bank/my
     * Blood bank views all incoming requests
     * Optional: ?status=PENDING | ACCEPTED | FULFILLED | ...
     */
    @GetMapping("/bank/my")
    @PreAuthorize("hasRole('BLOOD_BANK')")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getBankRequests(
        @RequestParam(required = false) String status,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        List<BloodRequestResponse> requests = bloodRequestService.getBankRequests(bankUser, status);
        return ResponseEntity.ok(ApiResponse.success("Blood requests fetched", requests));
    }

    /**
     * GET /api/requests/bank/emergency
     * Blood bank views emergency requests needing verification
     */
    @GetMapping("/bank/emergency")
    @PreAuthorize("hasRole('BLOOD_BANK')")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getBankEmergencyRequests(
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        List<BloodRequestResponse> requests = bloodRequestService
            .getBankEmergencyRequests(bankUser);
        return ResponseEntity.ok(ApiResponse.success("Emergency requests", requests));
    }

    /**
     * PUT /api/requests/{id}/handle
     * Blood bank handles a request
     * Actions: ACCEPT, REJECT, VERIFY_EMERGENCY, CONTACT_DONOR, FULFILL
     *
     * Flow:
     * - Normal:    PENDING → ACCEPT → CONTACT_DONOR → (donor donates) → FULFILL
     * - Emergency: PENDING → ACCEPT → EMERGENCY_PENDING_VERIFY → VERIFY_EMERGENCY → FULFILL
     */
    @PutMapping("/{id}/handle")
    @PreAuthorize("hasRole('BLOOD_BANK')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> handleRequest(
        @PathVariable Long id,
        @Valid @RequestBody HandleRequestRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.handleRequest(id, request, bankUser);
        return ResponseEntity.ok(ApiResponse.success("Request updated: " + request.getAction(),
            response));
    }

    /**
     * GET /api/requests/{id}/available-donors
     * Blood bank gets eligible+available donors for a specific request
     * (excludes donors already contacted in other active requests)
     */
    @GetMapping("/{id}/available-donors")
    @PreAuthorize("hasRole('BLOOD_BANK')")
    public ResponseEntity<ApiResponse<List<com.bharatbloodbank.dto.response.DonorResponse>>> getAvailableDonors(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        var donors = bloodRequestService.getAvailableDonorsForRequest(id, bankUser);
        return ResponseEntity.ok(ApiResponse.success("Available donors for this request", donors));
    }

    // ==================== SHARED ENDPOINT ====================

    /**
     * GET /api/requests/{id}
     * Get single request - accessible by the doctor who created it,
     * the blood bank it was sent to, or admin
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','BLOOD_BANK','ADMIN')")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> getRequestById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.getRequestById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Request fetched", response));
    }
}
