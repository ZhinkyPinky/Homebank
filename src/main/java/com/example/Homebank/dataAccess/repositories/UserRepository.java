package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByPassword(String password);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUserToken(String refreshToken);
}
