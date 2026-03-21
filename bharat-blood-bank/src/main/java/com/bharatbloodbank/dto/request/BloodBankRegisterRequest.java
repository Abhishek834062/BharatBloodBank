package com.bharatbloodbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BloodBankRegisterRequest {

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    private String pincode;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String contactPhone;

    // Login credentials
    @NotBlank @Email
    private String loginEmail;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
        message = "Password must contain uppercase, lowercase, digit and special character")
    private String password;
}
