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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "[User]", schema = "bank")
public class User implements UserDetails, CredentialsContainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "UserName")
    private String username;

    @Column(name = "Password")
    private String password;

    @Column(name = "EMail")
    private String email;

    @Column(name = "Description")
    private String description;

    @Column(name = "UserToken")
    private String userToken;

    @Column(name = "NextUserTokenChangeDate")
    private LocalDateTime nextUserTokenChangeDate;

    @Column(name = "TypeOfUser_Code")
    private String typeOfUserCode;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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
        return UserDetails.super.isEnabled();
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
