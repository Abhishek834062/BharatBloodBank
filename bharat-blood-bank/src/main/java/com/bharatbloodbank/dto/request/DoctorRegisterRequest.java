package com.bharatbloodbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoctorRegisterRequest {

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    private String specialization;

    @NotBlank(message = "Medical registration number is required")
    private String medicalRegistrationNumber;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    @NotBlank(message = "Hospital address is required")
    private String hospitalAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private String pincode;

    @NotBlank @Email
    private String loginEmail;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
        message = "Password must contain uppercase, lowercase, digit and special character")
    private String password;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;
}
