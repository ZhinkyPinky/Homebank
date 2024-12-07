package com.example.Homebank.presentation.controllers;

import com.example.Homebank.presentation.bodies.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/accounts")
public class AccountController {

    @PostMapping("/authenticate")
    public ResponseEntity<String> signIn(@RequestBody Authentication authentication) {
        System.out.println("Username: " + authentication.getUsername());
        System.out.println("Password: " + authentication.getPassword());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<String> signUp(@RequestBody Authentication authentication) {
        System.out.println("Username: " + authentication.getUsername());
        System.out.println("Password: " + authentication.getPassword());

        return ResponseEntity.ok().build();
    }
}
