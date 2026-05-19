package com.ecom.user.service;

import com.ecom.exception.ApiException;
import com.ecom.security.JwtService;
import com.ecom.user.client.UserAccountClient;
import com.ecom.user.client.UserAccountClient.UserAccountView;
import com.ecom.user.dto.AuthDtos.AuthResponse;
import com.ecom.user.dto.AuthDtos.LoginRequest;
import com.ecom.user.dto.AuthDtos.RegisterRequest;
import com.ecom.user.dto.AuthDtos.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountClient userAccountClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UserAccountClient userAccountClient,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userAccountClient = userAccountClient;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userAccountClient.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserAccountView createdUser = userAccountClient.createCustomer(
                request.fullName(),
                email,
                passwordEncoder.encode(request.password())
        );
        UserDetails details = userDetailsService.loadUserByUsername(createdUser.email());
        return new AuthResponse(jwtService.generateToken(details), "Bearer", toUserResponse(createdUser));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserAccountView user = userAccountClient.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        UserDetails details = userDetailsService.loadUserByUsername(user.email());
        return new AuthResponse(jwtService.generateToken(details), "Bearer", toUserResponse(user));
    }

    public UserResponse toUserResponse(UserAccountView user) {
        return new UserResponse(user.userId(), user.fullName(), user.email(), user.roles());
    }
}
