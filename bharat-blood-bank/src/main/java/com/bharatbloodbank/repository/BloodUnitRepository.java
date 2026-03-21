package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.BloodUnit;
import com.bharatbloodbank.entity.DonationRecord;
import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.BloodUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {

    List<BloodUnit> findByBloodBankAndBloodGroupAndComponentAndStatus(
        BloodBank bloodBank, BloodGroup bloodGroup,
        BloodComponent component, BloodUnitStatus status
    );

    long countByBloodBankAndBloodGroupAndComponentAndStatus(
        BloodBank bloodBank, BloodGroup bloodGroup,
        BloodComponent component, BloodUnitStatus status
    );

    // Inventory summary: count per blood group + component for a bank
    @Query("""
        SELECT b.bloodGroup, b.component, COUNT(b)
        FROM BloodUnit b
        WHERE b.bloodBank = :bank
        AND b.status = 'AVAILABLE'
        GROUP BY b.bloodGroup, b.component
    """)
    List<Object[]> getInventorySummaryByBank(@Param("bank") BloodBank bank);

    // Find oldest units first (FIFO - use older stock first before it expires)
    List<BloodUnit> findByBloodBankAndBloodGroupAndComponentAndStatusOrderByExpiryDateAsc(
        BloodBank bloodBank, BloodGroup bloodGroup,
        BloodComponent component, BloodUnitStatus status
    );

    // Find all expired AVAILABLE units (for scheduler cleanup)
    @Query("""
        SELECT b FROM BloodUnit b
        WHERE b.status = 'AVAILABLE'
        AND b.expiryDate < :today
    """)
    List<BloodUnit> findExpiredAvailableUnits(@Param("today") LocalDate today);

    // Delete expired units (hard delete after marking)
    @Modifying
    @Query("""
        DELETE FROM BloodUnit b
        WHERE b.status = 'AVAILABLE'
        AND b.expiryDate < :today
    """)
    int deleteExpiredUnits(@Param("today") LocalDate today);

    // Find units expiring soon (for warning emails)
    @Query("""
        SELECT b FROM BloodUnit b
        WHERE b.status = 'AVAILABLE'
        AND b.expiryDate BETWEEN :today AND :warningDate
    """)
    List<BloodUnit> findUnitExpiringSoon(
        @Param("today") LocalDate today,
        @Param("warningDate") LocalDate warningDate
    );

    List<BloodUnit> findByDonationRecord(DonationRecord donationRecord);

    long countByBloodBankAndStatus(BloodBank bloodBank, BloodUnitStatus status);
}
