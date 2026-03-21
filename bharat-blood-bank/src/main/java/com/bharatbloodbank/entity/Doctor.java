package com.bharatbloodbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String doctorName;

    @Column(length = 100)
    private String specialization;

    // Medical Council of India Registration Number
    @Column(unique = true, nullable = false, length = 50)
    private String medicalRegistrationNumber;

    // In real life, a doctor practices at one primary hospital
    @Column(nullable = false, length = 200)
    private String hospitalName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String hospitalAddress;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @CreationTimestamp
    private LocalDateTime registeredAt;
}
