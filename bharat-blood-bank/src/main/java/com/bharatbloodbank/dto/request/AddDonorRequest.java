package com.bharatbloodbank.dto.request;

import com.bharatbloodbank.enums.BloodGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AddDonorRequest {

    @NotBlank(message = "Donor name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian phone number")
    private String phone;

    private String email;

    private String address;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    private LocalDate dateOfBirth;

    private String gender;
}
