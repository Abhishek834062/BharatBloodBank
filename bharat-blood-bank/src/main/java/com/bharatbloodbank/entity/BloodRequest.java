package com.bharatbloodbank.entity;

import com.bharatbloodbank.enums.BloodComponent;
import com.bharatbloodbank.enums.BloodGroup;
import com.bharatbloodbank.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests",
    indexes = {
        @Index(name = "idx_request_doctor", columnList = "doctor_id"),
        @Index(name = "idx_request_bank", columnList = "blood_bank_id"),
        @Index(name = "idx_request_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bank_id", nullable = false)
    private BloodBank bloodBank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private BloodComponent component;

    @Column(nullable = false)
    private int unitsRequired;

    // Patient information
    @Column(length = 100)
    private String patientName;

    @Column(columnDefinition = "TEXT")
    private String patientCondition;

    // Emergency fields
    @Builder.Default
    private boolean emergency = false;

    @Column(columnDefinition = "TEXT")
    private String emergencyReason;

    // After bank reviews emergency claim
    @Builder.Default
    private boolean emergencyVerifiedByBank = false;

    // If not emergency - bank contacts a donor from their list
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacted_donor_id")
    private Donor contactedDonor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Bank's internal notes
    @Column(columnDefinition = "TEXT")
    private String bankNotes;

    @CreationTimestamp
    private LocalDateTime requestedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime fulfilledAt;
}
