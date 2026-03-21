
package com.bharatbloodbank.service;

import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.BloodUnit;
import com.bharatbloodbank.repository.BloodUnitRepository;
import com.bharatbloodbank.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiryScheduler {

    private final BloodUnitRepository bloodUnitRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final DonorService donorService;

    // ===================== DAILY EXPIRY CLEANUP - runs at midnight =====================
    @Scheduled(cron = "0 0 0 * * *")     // Every day at 00:00
    @Transactional
    public void removeExpiredBloodUnits() {
        LocalDate today = LocalDate.now();

        // Find before deleting - need info for logging
        List<BloodUnit> expired = bloodUnitRepository.findExpiredAvailableUnits(today);
        int count = expired.size();

        if (count > 0) {
            int deleted = bloodUnitRepository.deleteExpiredUnits(today);
            log.warn("🗑️  Expired blood cleanup: Removed {} units (date: {})", deleted, today);
        } else {
            log.info("✅ No expired blood units today ({})", today);
        }
    }

    // ===================== EXPIRY WARNING - runs daily at 8 AM =====================
    // Warns banks about units expiring within 2 days
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendExpiryWarnings() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(2);

        List<BloodUnit> expiringSoon = bloodUnitRepository
            .findUnitExpiringSoon(today, warningDate);

        if (expiringSoon.isEmpty()) {
            log.info("No blood units expiring in next 2 days");
            return;
        }

        // Group by blood bank → blood group → component
        Map<BloodBank, Map<String, Long>> grouped = expiringSoon.stream()
            .collect(Collectors.groupingBy(
                BloodUnit::getBloodBank,
                Collectors.groupingBy(
                    u -> u.getBloodGroup().getDisplayName()
                        + " - " + u.getComponent().getDisplayName(),
                    Collectors.counting()
                )
            ));

        for (Map.Entry<BloodBank, Map<String, Long>> bankEntry : grouped.entrySet()) {
            BloodBank bank = bankEntry.getKey();
            for (Map.Entry<String, Long> unitEntry : bankEntry.getValue().entrySet()) {
                String[] parts = unitEntry.getKey().split(" - ");
                String bloodGroup = parts[0];
                String component = parts.length > 1 ? parts[1] : "Unknown";
                long units = unitEntry.getValue();

                emailService.sendExpiryWarningToBank(
                    bank.getContactEmail(),
                    bank.getBankName(),
                    bloodGroup,
                    component,
                    units,
                    2
                );
                log.info("⚠️  Expiry warning sent to {} for {} {} ({} units)",
                    bank.getBankName(), bloodGroup, component, units);
            }
        }
    }

    // ===================== DONOR ELIGIBILITY REFRESH - daily at 6 AM =====================
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void refreshDonorEligibility() {
        log.info("🔄 Refreshing donor eligibility...");
        donorService.refreshDonorEligibility();
    }

    // ===================== CLEANUP EXPIRED RESET TOKENS - daily at 2 AM =====================
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredResetTokens() {
        resetTokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
        log.info("🧹 Cleaned up expired/used password reset tokens");
    }
}
