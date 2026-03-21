package com.bharatbloodbank.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_banks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String bankName;

    @Column(unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 15)
    private String contactPhone;

    @Column(length = 100)
    private String contactEmail;

    @CreationTimestamp
    private LocalDateTime registeredAt;
}
