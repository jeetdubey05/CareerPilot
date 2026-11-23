package com.careerpilot.backendcodebase.controller;

import com.careerpilot.backendcodebase.dtos.LoginRequestDTO;
import com.careerpilot.backendcodebase.dtos.LoginResponseDTO;
import com.careerpilot.backendcodebase.dtos.RegisterUserDTO;
import com.careerpilot.backendcodebase.entity.User;
import com.careerpilot.backendcodebase.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AdminController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register-admin")
    public ResponseEntity<User> registerAdminUser(@RequestBody RegisterUserDTO registerUserDTO){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.registerAdminUser(registerUserDTO));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody LoginRequestDTO loginRequestDTO){
        return ResponseEntity.ok(authenticationService.loginUser(loginRequestDTO));
    }
}
