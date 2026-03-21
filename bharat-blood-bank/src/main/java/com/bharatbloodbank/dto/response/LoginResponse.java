package com.bharatbloodbank.dto.response;

import com.bharatbloodbank.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private Role role;
}
