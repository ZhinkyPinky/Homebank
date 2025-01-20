package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPassword(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserToken(String refreshToken);
}
