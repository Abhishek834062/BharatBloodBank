package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.BloodBank;
import com.bharatbloodbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodBankRepository extends JpaRepository<BloodBank, Long> {
    Optional<BloodBank> findByUser(User user);
    Optional<BloodBank> findByUser_Id(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);
    List<BloodBank> findByCity(String city);
    List<BloodBank> findByState(String state);
    List<BloodBank> findByCityAndState(String city, String state);
}
