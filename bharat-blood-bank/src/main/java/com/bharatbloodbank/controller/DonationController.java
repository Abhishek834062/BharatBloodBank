package com.bharatbloodbank.controller;

import com.bharatbloodbank.dto.request.ConfirmDonationRequest;
import com.bharatbloodbank.dto.response.ApiResponse;
import com.bharatbloodbank.dto.response.DonationRecordResponse;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.service.AuthService;
import com.bharatbloodbank.service.DonationService;
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
@RequestMapping("/donations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BLOOD_BANK')")
public class DonationController {

    private final DonationService donationService;
    private final AuthService authService;

    /**
     * POST /api/donations/confirm
     * Blood bank confirms a donation event.
     * System auto-splits whole blood into RBC, Plasma, Platelets.
     * Body: { "donorId": 1, "donationDate": "2024-06-15",
     *         "linkedRequestId": null, "notes": "..." }
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<DonationRecordResponse>> confirmDonation(
        @Valid @RequestBody ConfirmDonationRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        DonationRecordResponse response = donationService.confirmDonation(request, bankUser);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Donation confirmed and blood components created successfully", response));
    }

    /**
     * GET /api/donations
     * All donations for this blood bank
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonationRecordResponse>>> getMyDonations(
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        List<DonationRecordResponse> records = donationService.getDonationsByBank(bankUser);
        return ResponseEntity.ok(ApiResponse.success("Donations fetched", records));
    }

    /**
     * GET /api/donations/{id}
     * Get a specific donation record
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationRecordResponse>> getDonationById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        DonationRecordResponse record = donationService.getDonationById(id, bankUser);
        return ResponseEntity.ok(ApiResponse.success("Donation record fetched", record));
    }

    /**
     * GET /api/donations/donor/{donorId}
     * Get all donations by a specific donor
     */
    @GetMapping("/donor/{donorId}")
    public ResponseEntity<ApiResponse<List<DonationRecordResponse>>> getDonorHistory(
        @PathVariable Long donorId,
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        List<DonationRecordResponse> records = donationService
            .getDonationsByDonor(donorId, bankUser);
        return ResponseEntity.ok(ApiResponse.success("Donor donation history", records));
    }
}
