package com.nextuple.promoengine.controller;

import com.nextuple.promoengine.dto.JwtResponse;
import com.nextuple.promoengine.dto.RegistrationResponse;
import com.nextuple.promoengine.dto.UserLoginDto;
import com.nextuple.promoengine.dto.UserRegistrationDto;
import com.nextuple.promoengine.model.User;
import com.nextuple.promoengine.repository.UserRepository;
import com.nextuple.promoengine.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



@PostMapping("/login")
public ResponseEntity<JwtResponse> login(@RequestBody UserLoginDto loginDto) {
    log.info(" Inside Login method in auth controller for user: {}", loginDto.getUsername());

    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        String token = jwtUtil.generateToken(loginDto.getUsername());
        log.info("Generated token: {}", token);
        return ResponseEntity.ok(new JwtResponse(token));
    } catch (BadCredentialsException e) {
        log.warn("Invalid credentials for user: {}", loginDto.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("Invalid credentials"));
    } catch (Exception e) {
        log.error("An error occurred during authentication", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JwtResponse("An error occurred"));
    }
}



    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RegistrationResponse("Username already exists"));
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword())); // Encode password
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse("User registered successfully"));
    }
}

