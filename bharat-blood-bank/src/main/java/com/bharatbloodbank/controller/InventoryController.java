package com.bharatbloodbank.controller;

import com.bharatbloodbank.dto.response.ApiResponse;
import com.bharatbloodbank.dto.response.InventorySummaryResponse;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.service.AuthService;
import com.bharatbloodbank.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final AuthService authService;

    // ==================== PUBLIC ENDPOINTS (no auth) ====================

    /**
     * GET /api/inventory/public/all
     * All blood banks inventory - doctors use this to decide where to request
     */
    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<List<InventorySummaryResponse>>> getAllBanksInventory() {
        List<InventorySummaryResponse> data = inventoryService.getAllBanksInventory();
        return ResponseEntity.ok(ApiResponse.success("All banks inventory", data));
    }

    /**
     * GET /api/inventory/public/bank/{bankId}
     * Public inventory for a specific blood bank
     */
    @GetMapping("/public/bank/{bankId}")
    public ResponseEntity<ApiResponse<InventorySummaryResponse>> getBankInventoryPublic(
        @PathVariable Long bankId) {
        InventorySummaryResponse data = inventoryService.getInventoryByBankId(bankId);
        return ResponseEntity.ok(ApiResponse.success("Bank inventory", data));
    }

    /**
     * GET /api/inventory/public/bank/{bankId}/available?bloodGroup=B_POSITIVE&component=RBC
     * Check how many units of specific type are available
     * Used by doctor BEFORE creating a blood request
     */
    @GetMapping("/public/bank/{bankId}/available")
    public ResponseEntity<ApiResponse<Long>> checkAvailability(
        @PathVariable Long bankId,
        @RequestParam BloodGroup bloodGroup,
        @RequestParam BloodComponent component) {
        long units = inventoryService.getAvailableUnits(bankId, bloodGroup, component);
        String msg = String.format("%s %s: %d units available",
            bloodGroup.getDisplayName(), component.getDisplayName(), units);
        return ResponseEntity.ok(ApiResponse.success(msg, units));
    }

    // ==================== BLOOD BANK ENDPOINTS (authenticated) ====================

    /**
     * GET /api/inventory/my
     * Blood bank views its own full inventory
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('BLOOD_BANK')")
    public ResponseEntity<ApiResponse<InventorySummaryResponse>> getMyInventory(
        @AuthenticationPrincipal UserDetails userDetails) {
        User bankUser = authService.getCurrentUser(userDetails.getUsername());
        InventorySummaryResponse data = inventoryService.getMyInventory(bankUser);
        return ResponseEntity.ok(ApiResponse.success("Your inventory", data));
    }

    // ==================== ADMIN ENDPOINT ====================

    /**
     * GET /api/inventory/admin/bank/{bankId}
     * Admin views any specific bank's inventory
     */
    @GetMapping("/admin/bank/{bankId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventorySummaryResponse>> getBankInventoryAdmin(
        @PathVariable Long bankId) {
        InventorySummaryResponse data = inventoryService.getInventoryByBankId(bankId);
        return ResponseEntity.ok(ApiResponse.success("Bank inventory", data));
    }
}
