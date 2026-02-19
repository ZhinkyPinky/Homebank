package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByPassword(String password);

    Optional<UserEntity> findByRefreshToken(String refreshToken);

    Optional<UserEntity> findByActivationToken(String activationToken);
}
