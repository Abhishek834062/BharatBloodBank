package com.bharatbloodbank.controller;

import com.bharatbloodbank.dto.request.AddDonorRequest;
import com.bharatbloodbank.dto.response.ApiResponse;
import com.bharatbloodbank.dto.response.DonorResponse;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.service.AuthService;
import com.bharatbloodbank.service.DonorService;
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
@RequestMapping("/donors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BLOOD_BANK')")
public class DonorController {

    private final DonorService donorService;
    private final AuthService authService;

    /**
     * POST /api/donors
     * Blood bank adds a donor manually
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DonorResponse>> addDonor(
        @Valid @RequestBody AddDonorRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        DonorResponse response = donorService.addDonor(request, bankUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Donor added successfully", response));
    }

    /**
     * GET /api/donors
     * Get all donors added by this blood bank
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonorResponse>>> getMyDonors(
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        List<DonorResponse> donors = donorService.getDonorsByBank(bankUser);
        return ResponseEntity.ok(ApiResponse.success("Donors fetched", donors));
    }

    /**
     * GET /api/donors/{id}
     * Get a specific donor by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonorResponse>> getDonorById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        DonorResponse donor = donorService.getDonorById(id, bankUser);
        return ResponseEntity.ok(ApiResponse.success("Donor fetched", donor));
    }

    /**
     * GET /api/donors/eligible?bloodGroup=B_POSITIVE
     * Get all eligible donors of a specific blood group for this bank
     */
    @GetMapping("/eligible")
    public ResponseEntity<ApiResponse<List<DonorResponse>>> getEligibleDonors(
        @RequestParam BloodGroup bloodGroup,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        List<DonorResponse> donors = donorService.getEligibleDonorsByBloodGroup(
            bankUser, bloodGroup);
        return ResponseEntity.ok(
            ApiResponse.success("Eligible donors for " + bloodGroup.getDisplayName(), donors));
    }

    /**
     * GET /api/donors/{id}/eligibility
     * Check if a specific donor is currently eligible
     */
    @GetMapping("/{id}/eligibility")
    public ResponseEntity<ApiResponse<Boolean>> checkEligibility(@PathVariable Long id) {
        boolean eligible = donorService.checkEligibility(id);
        String message = eligible ? "Donor is eligible to donate" : "Donor is not eligible yet";
        return ResponseEntity.ok(ApiResponse.success(message, eligible));
    }
}
