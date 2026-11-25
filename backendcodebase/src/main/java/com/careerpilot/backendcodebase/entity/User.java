package com.careerpilot.backendcodebase.entity;


import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@Table(name="user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(nullable = false)
    private String name;
    @Column(nullable=false)
    private String password;
    @Column(nullable=false,unique = true)
    private String email;
    @Column(nullable=false)
    private String phoneNumber;
    //private String stream;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return roles.stream()
               .map(SimpleGrantedAuthority::new)
               .collect(Collectors.toList());
    }
}
