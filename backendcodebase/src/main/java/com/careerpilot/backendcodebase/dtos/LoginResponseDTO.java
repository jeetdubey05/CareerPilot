package com.careerpilot.backendcodebase.dtos;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String jwtToken;
    private String email;
    private String name;
    private Set<String> roles;
}
