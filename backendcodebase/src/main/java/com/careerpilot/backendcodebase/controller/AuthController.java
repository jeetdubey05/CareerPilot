package com.careerpilot.backendcodebase.controller;

import com.careerpilot.backendcodebase.dtos.LoginRequestDTO;
import com.careerpilot.backendcodebase.dtos.LoginResponseDTO;
import com.careerpilot.backendcodebase.dtos.RegisterUserDTO;
import com.careerpilot.backendcodebase.entity.User;
import com.careerpilot.backendcodebase.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register-user")
    public ResponseEntity<User> registerUser(@RequestBody RegisterUserDTO registerUserDTO){
          return ResponseEntity
                  .status(HttpStatus.CREATED)
                  .body(authenticationService.registerNormalUser(registerUserDTO));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody LoginRequestDTO loginRequestDTO){
        return ResponseEntity.ok(authenticationService.loginUser(loginRequestDTO));
    }
    @PostMapping("/register-admin")
    public ResponseEntity<User> registerAdminUser(@RequestBody RegisterUserDTO registerUserDTO){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authenticationService.registerAdminUser(registerUserDTO));
    }
}
