package com.bharatbloodbank.security;

import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.VerificationStatus;
import com.bharatbloodbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // For BLOOD_BANK and DOCTOR - must be approved to login
        if (user.getVerificationStatus() != null
            && user.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new UsernameNotFoundException(
                "Account not approved yet. Status: " + user.getVerificationStatus());
        }

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("Account is disabled");
        }

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .build();
    }
}
