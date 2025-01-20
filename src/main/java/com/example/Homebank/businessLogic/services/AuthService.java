package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.User;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.bodies.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshJwtUtil refreshJwtUtil;
    private final AccessJwtUtil accessJwtUtil;
    private final UserRepository userRepository;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(authenticationRequest.username(), authenticationRequest.password())
        );

        String username = ((User) authentication.getPrincipal()).getUsername();
        String accessToken = accessJwtUtil.generateToken(username);
        String refreshToken = refreshJwtUtil.generateToken(username);

        return new AuthenticationResponse(accessToken, refreshToken, "Login successful");
    }


    public RegistrationResponse register(RegistrationRequest registrationRequest) {
        validateRegistrationDetails(registrationRequest);

        String username = registrationRequest.getUsername();
        String accessToken = accessJwtUtil.generateToken(username);
        String refreshToken = refreshJwtUtil.generateToken(username);

        //TODO: Needs a procedure?
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setEmail(registrationRequest.getEmail());
        user.setUserToken(refreshToken);
        user.setNextUserTokenChangeDate(LocalDateTime.now()); //TODO: Remove column.
        user.setTypeOfUserCode("ENDUSER");
        user.setRowCreatedDate(LocalDateTime.now());
        user.setRowLastEditDate(LocalDateTime.now());
        user.setRowVersion(LocalDateTime.now());

        userRepository.save(user);

        return new RegistrationResponse(accessToken, refreshToken, "Registration successful");
    }

    private void validateRegistrationDetails(RegistrationRequest registrationRequest) {
        if (registrationRequest.getUsername().isBlank()) {
            //TODO
        }

        if (registrationRequest.getPassword().isBlank()) {
            //TODO
        }

        if (registrationRequest.getEmail().isBlank()) {
            //TODO
        }

        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            throw new EntityExistsException("Username");
        }

        if (userRepository.findByPassword(registrationRequest.getPassword()).isPresent()) {
            throw new EntityExistsException("Password");
        }

        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new EntityExistsException("Email");
        }
    }

    public RefreshResponse refreshToken(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();

        String username = refreshJwtUtil.extractUsername(refreshToken);

        User user = userRepository.findByUserToken(refreshRequest.refreshToken()).orElseThrow(() ->
                new EntityNotFoundException("User not found")
        );

        if (!refreshJwtUtil.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return new RefreshResponse(accessJwtUtil.generateToken(username));
    }
}
