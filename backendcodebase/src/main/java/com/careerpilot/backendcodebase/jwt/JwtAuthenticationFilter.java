package com.careerpilot.backendcodebase.jwt;

import com.careerpilot.backendcodebase.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Autowired
    public JwtAuthenticationFilter(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final  String authHeader=request.getHeader("Authorization");
        final String jwtToken;
        final String email;


        System.out.println("JWT Filter hit: " + request.getRequestURI() + " | Header: " + request.getHeader("Authorization"));
        if(authHeader==null||!authHeader.startsWith("Bearer ")) {
             filterChain.doFilter(request,response);
             return;
        }
        //Extract the Jwt Token from the Header;
        jwtToken=authHeader.substring(7);
        email=jwtService.extractEmail(jwtToken);
        //check if we have a username and no authentication exist yet
         if(email!=null&& SecurityContextHolder.getContext().getAuthentication()==null) {
             var userDetails=userRepository.findByEmail(email)
                     .orElseThrow(()->new UsernameNotFoundException("email  not found in jwt Token"));
             //validate the Token
             if(jwtService.isTokenValid(jwtToken,userDetails)){
                 //create the authentication  with user roles
                 List<SimpleGrantedAuthority> authorities=userDetails.getRoles()
                         .stream()
                         .map(SimpleGrantedAuthority::new)
                         .toList();
                 UsernamePasswordAuthenticationToken authToken=
                         new UsernamePasswordAuthenticationToken(userDetails,null,authorities);
                 //set Authentication details
                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
             //Update the security context with authentication
                 SecurityContextHolder.getContext().setAuthentication(authToken);
             }

         }
       filterChain.doFilter(request,response);
    }

}
