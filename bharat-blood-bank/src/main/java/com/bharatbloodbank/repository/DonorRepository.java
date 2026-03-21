
package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.Donor;
import com.bharatbloodbank.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    List<Donor> findByAddedByBank(BloodBank bloodBank);

    List<Donor> findByAddedByBankAndBloodGroup(BloodBank bloodBank, BloodGroup bloodGroup);

    List<Donor> findByAddedByBankAndBloodGroupAndEligibleTrue(BloodBank bloodBank, BloodGroup bloodGroup);

    Optional<Donor> findByPhoneAndAddedByBank(String phone, BloodBank bloodBank);

    boolean existsByPhoneAndAddedByBank(String phone, BloodBank bloodBank);

    // All eligible donors of a blood group linked to a bank
    @Query("""
        SELECT DISTINCT d FROM Donor d
        LEFT JOIN d.linkedBanks lb
        WHERE (d.addedByBank = :bank OR lb = :bank)
        AND d.bloodGroup = :bg
        AND d.eligible = true
    """)
    List<Donor> findEligibleDonorsByBankAndBloodGroup(
        @Param("bank") BloodBank bank,
        @Param("bg") BloodGroup bg
    );

   
    @Query("""
        SELECT d FROM Donor d
        WHERE d.addedByBank.id = :bankId
        AND d.lastDonationDate IS NOT NULL
        AND NOT EXISTS (
            SELECT r FROM BloodRequest r
            WHERE r.bloodBank.id = :bankId
            AND r.contactedDonor = d
            AND r.status NOT IN (
                com.bharatbloodbank.enums.RequestStatus.FULFILLED,
                com.bharatbloodbank.enums.RequestStatus.REJECTED,
                com.bharatbloodbank.enums.RequestStatus.CANCELLED
            )
        )
        AND NOT EXISTS (
            SELECT r FROM BloodRequest r
            WHERE r.bloodBank.id = :bankId
            AND r.contactedDonor = d
            AND r.updatedAt > d.lastDonationDate
        )
        ORDER BY d.lastDonationDate DESC
    """)
    List<Donor> findAvailableEligibleDonors(@Param("bankId") Long bankId);

    // Donors currently contacted in an active request (for duplicate check)
    @Query("""
        SELECT d FROM Donor d
        WHERE EXISTS (
            SELECT r FROM BloodRequest r
            WHERE r.bloodBank = :bank
            AND r.contactedDonor = d
            AND r.status NOT IN (
                com.bharatbloodbank.enums.RequestStatus.FULFILLED,
                com.bharatbloodbank.enums.RequestStatus.REJECTED,
                com.bharatbloodbank.enums.RequestStatus.CANCELLED
            )
        )
    """)
    List<Donor> findDonorsAlreadyContactedInActiveRequests(@Param("bank") BloodBank bank);
}

