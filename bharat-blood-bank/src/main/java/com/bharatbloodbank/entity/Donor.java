package com.bharatbloodbank.entity;

import com.bharatbloodbank.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "donors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BloodGroup bloodGroup;

    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    // Last time this donor donated blood — used for 3-month eligibility check
    private LocalDate lastDonationDate;

    // Is this donor currently eligible to donate? (auto-computed)
    @Builder.Default
    private boolean eligible = true;

    // The blood bank that first registered this donor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_bank_id", nullable = false)
    private BloodBank addedByBank;

    // Donor can be linked to multiple blood banks over time
    @ManyToMany
    @JoinTable(
        name = "donor_blood_bank_links",
        joinColumns = @JoinColumn(name = "donor_id"),
        inverseJoinColumns = @JoinColumn(name = "blood_bank_id")
    )
    @Builder.Default
    private Set<BloodBank> linkedBanks = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
