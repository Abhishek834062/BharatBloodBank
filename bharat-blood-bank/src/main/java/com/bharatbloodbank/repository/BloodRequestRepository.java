package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.BloodRequest;
import com.bharatbloodbank.entity.Doctor;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByDoctorOrderByRequestedAtDesc(Doctor doctor);

    List<BloodRequest> findByBloodBankOrderByRequestedAtDesc(BloodBank bloodBank);

    List<BloodRequest> findByBloodBankAndStatusOrderByRequestedAtDesc(
        BloodBank bloodBank, RequestStatus status
    );

    List<BloodRequest> findByBloodBankAndEmergencyTrueAndStatusOrderByRequestedAtDesc(
        BloodBank bloodBank, RequestStatus status
    );

    List<BloodRequest> findByDoctorAndStatus(Doctor doctor, RequestStatus status);

    long countByBloodBankAndStatus(BloodBank bloodBank, RequestStatus status);

    long countByBloodBankAndEmergencyTrue(BloodBank bloodBank);

    List<BloodRequest> findByBloodBankAndBloodGroupOrderByRequestedAtDesc(
        BloodBank bloodBank, BloodGroup bloodGroup
    );
}
