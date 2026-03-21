package com.bharatbloodbank.entity;

import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.BloodUnitStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_units",
    indexes = {
        @Index(name = "idx_blood_unit_bank", columnList = "blood_bank_id"),
        @Index(name = "idx_blood_unit_group_component", columnList = "blood_group, component"),
        @Index(name = "idx_blood_unit_status", columnList = "status"),
        @Index(name = "idx_blood_unit_expiry", columnList = "expiry_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, name = "blood_group")
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BloodComponent component;

    // The donation this unit came from
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_record_id", nullable = false)
    private DonationRecord donationRecord;

    @Column(nullable = false)
    private LocalDate donationDate;

    @Column(nullable = false, name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, name = "status")
    @Builder.Default
    private BloodUnitStatus status = BloodUnitStatus.AVAILABLE;

    // Which request consumed this unit (set when status = USED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_for_request_id")
    private BloodRequest usedForRequest;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime usedAt;
}
