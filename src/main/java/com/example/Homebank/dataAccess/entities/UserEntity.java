package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entity representing a user in the system. Implements UserDetails and CredentialsContainer for integration with Spring Security.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "[User]", schema = "bank")
public class UserEntity implements UserDetails, CredentialsContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int id;

    @Column(name = "EMail")
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "Description")
    private String description;

    @Column(name = "RefreshToken")
    private String refreshToken;

    @Column(name = "NextRefreshTokenExpirationDate")
    private LocalDateTime nextRefreshTokenExpirationDate;

    @Column(name = "TypeOfUser_Code")
    private String typeOfUserCode = "ENDUSER";

    @Column(name = "IsEnabled_Code")
    private boolean isEnabled = true;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVATION_PENDING;

    @Column(name = "ActivationToken")
    private String activationToken;

    @Column(name = "ActivationTokenExpirationDate")
    private LocalDateTime activationTokenExpirationDate;

    @Column(name = "RowCreatedBy")
    private String rowCreatedBy;

    @Column(name = "RowCreatedDate")
    private LocalDateTime rowCreatedDate;

    @Column(name = "RowLastEditBy")
    private String rowLastEditBy;

    @Column(name = "RowLastEditDate")
    private LocalDateTime rowLastEditDate;

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;

    @Column(name = "RecoveryPassword")
    private String recoveryPassword;

    @Column(name = "RecoveryPasswordExpiration")
    private LocalDateTime recoveryPasswordExpiration;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
