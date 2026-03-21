package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.Doctor;
import com.bharatbloodbank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    Optional<Doctor> findByUser_Id(Long userId);
    boolean existsByMedicalRegistrationNumber(String mrn);
}
