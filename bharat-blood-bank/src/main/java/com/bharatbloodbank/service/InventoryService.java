
package com.bharatbloodbank.service;

import com.bharatbloodbank.dto.response.InventorySummaryResponse;
import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.BloodUnitStatus;
import com.bharatbloodbank.exception.ResourceNotFoundException;
import com.bharatbloodbank.repository.BloodBankRepository;
import com.bharatbloodbank.repository.BloodUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final BloodUnitRepository bloodUnitRepository;
    private final BloodBankRepository bloodBankRepository;

    // ===================== MY BANK INVENTORY SUMMARY =====================
    @Transactional(readOnly = true)
    public InventorySummaryResponse getMyInventory(User bankUser) {
        BloodBank bank = bloodBankRepository.findByUser(bankUser)
            .orElseThrow(() -> new ResourceNotFoundException("Blood bank profile not found"));
        return buildSummary(bank);
    }

    // ===================== PUBLIC: INVENTORY FOR SPECIFIC BANK =====================
    @Transactional(readOnly = true)
    public InventorySummaryResponse getInventoryByBankId(Long bankId) {
        BloodBank bank = bloodBankRepository.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", bankId));
        return buildSummary(bank);
    }

    // ===================== PUBLIC: ALL BANKS INVENTORY (for doctors to browse) =====================
    @Transactional(readOnly = true)
    public List<InventorySummaryResponse> getAllBanksInventory() {
        return bloodBankRepository.findAll().stream()
            .map(this::buildSummary)
            .toList();
    }

    // ===================== CHECK AVAILABILITY (used before blood request) =====================
    @Transactional(readOnly = true)
    public long getAvailableUnits(Long bankId, BloodGroup bloodGroup, BloodComponent component) {
        BloodBank bank = bloodBankRepository.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("BloodBank", bankId));
        return bloodUnitRepository.countByBloodBankAndBloodGroupAndComponentAndStatus(
            bank, bloodGroup, component, BloodUnitStatus.AVAILABLE);
    }

    // ===================== BUILD FULL INVENTORY MAP =====================
    private InventorySummaryResponse buildSummary(BloodBank bank) {
        List<Object[]> rawData = bloodUnitRepository.getInventorySummaryByBank(bank);
        Map<String, Long> inventoryMap = new HashMap<>();
        long total = 0;

        for (Object[] row : rawData) {
            BloodGroup bg = (BloodGroup) row[0];
            BloodComponent comp = (BloodComponent) row[1];
            Long count = (Long) row[2];
            String key = bg.getDisplayName() + " - " + comp.getDisplayName();
            inventoryMap.put(key, count);
            total += count;
        }

        // Fill zeros for missing combinations so UI always has full table
        for (BloodGroup bg : BloodGroup.values()) {
            for (BloodComponent comp : BloodComponent.values()) {
                String key = bg.getDisplayName() + " - " + comp.getDisplayName();
                inventoryMap.putIfAbsent(key, 0L);
            }
        }

        return InventorySummaryResponse.builder()
            .bloodBankId(bank.getId())
            .bloodBankName(bank.getBankName())
            .inventory(inventoryMap)
            .totalAvailableUnits(total)
            .build();
    }
}





