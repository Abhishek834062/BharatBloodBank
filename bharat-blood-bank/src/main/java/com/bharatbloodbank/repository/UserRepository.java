package com.bharatbloodbank.repository;

import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.Role;
import com.bharatbloodbank.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRoleAndVerificationStatus(Role role, VerificationStatus status);
    List<User> findByRole(Role role);
}
