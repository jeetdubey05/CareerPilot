package com.careerpilot.backendcodebase.service;
import com.careerpilot.backendcodebase.dtos.LoginRequestDTO;
import com.careerpilot.backendcodebase.dtos.LoginResponseDTO;
import com.careerpilot.backendcodebase.dtos.RegisterUserDTO;
import com.careerpilot.backendcodebase.entity.User;
import com.careerpilot.backendcodebase.exception.EmailFoundException;
import com.careerpilot.backendcodebase.jwt.JwtService;
import com.careerpilot.backendcodebase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }



    public User registerNormalUser(RegisterUserDTO registerUserDTO) {
        if (userRepository.findByEmail(registerUserDTO.getEmail()).isPresent()) {
            throw new EmailFoundException("User With this Email Already Exists");
        }
        Set<String> roles = new HashSet<String>();
        roles.add("ROLE_USER");
        User user = new User();
        user.setEmail(registerUserDTO.getEmail());
        user.setPhoneNumber(registerUserDTO.getPhoneNumber());
        user.setName(registerUserDTO.getName());
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
        return userRepository.save(user);
    }

    public User registerAdminUser(RegisterUserDTO registerUserDTO) {
        if (userRepository.findByEmail(registerUserDTO.getEmail()).isPresent()) {
            throw new EmailFoundException("Admin With this Email Already Exists");
        }
        Set<String> roles = new HashSet<String>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_USER");
        User user = new User();
        user.setEmail(registerUserDTO.getEmail());
        user.setPhoneNumber(registerUserDTO.getPhoneNumber());
        user.setName(registerUserDTO.getName());
        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
        return userRepository.save(user);

    }
        public LoginResponseDTO loginUser (LoginRequestDTO loginRequestDTO){
            User user=userRepository.findByEmail(loginRequestDTO.getEmail())
                    .orElseThrow(()->new RuntimeException("User Not with Email"));
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDTO.getEmail(),
                            loginRequestDTO.getPassword()
                    )
            );
            String Token=jwtService.generateToken(user);
            return LoginResponseDTO.builder()
                    .jwtToken(Token)
                    .email(loginRequestDTO.getEmail())
                    .name(user.getName())
                    .roles(user.getRoles())
                    .build();
        }
        }

