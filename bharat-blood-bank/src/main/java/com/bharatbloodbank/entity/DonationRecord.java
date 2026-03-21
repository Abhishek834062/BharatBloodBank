package com.bharatbloodbank.entity;

import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Column(nullable = false)
    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BloodGroup bloodGroup;

    // Standard whole blood donation volume is ~450 ml = 1 unit
    @Builder.Default
    private double volumeMl = 450.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DonationStatus status = DonationStatus.CONFIRMED;

    // True when blood has been split into components (RBC, Plasma, Platelets)
    @Builder.Default
    private boolean splitDone = false;

    // Link to blood request this donation was for (nullable — can be voluntary)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_request_id")
    private BloodRequest linkedRequest;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    private LocalDateTime confirmedAt;
}
