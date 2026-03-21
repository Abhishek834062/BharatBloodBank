
package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.Donor;
import com.bharatbloodbank.entity.DonationRecord;
import com.bharatbloodbank.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {

    List<DonationRecord> findByBloodBank(BloodBank bloodBank);

    List<DonationRecord> findByDonor(Donor donor);

    List<DonationRecord> findByDonorAndBloodBank(Donor donor, BloodBank bloodBank);

    List<DonationRecord> findByBloodBankAndBloodGroup(BloodBank bloodBank, BloodGroup bloodGroup);

    List<DonationRecord> findBySplitDoneFalse();

    long countByBloodBankAndBloodGroup(BloodBank bloodBank, BloodGroup bloodGroup);
}
