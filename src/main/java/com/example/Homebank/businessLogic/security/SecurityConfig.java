package com.example.Homebank.businessLogic.security;

import com.example.Homebank.businessLogic.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for the application. Configures authentication and authorization, as well as other
 * security-related settings.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, UserStatusFilter userStatusFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers(
                                        "/auth/login",
                                        "/auth/refresh",
                                        "/auth/register",
                                        "/auth/activate",
                                        "/account-recovery/initiate",
                                        "/account-recovery/authenticate",
                                        "/account-recovery/set-new-password"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userStatusFilter, JwtAuthFilter.class)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.NEVER)
                )
                //.logout(LogoutConfigurer::permitAll)
                .build();
    }

    /**
     * Creates and configures an AuthenticationManager implementation used for authenticating users.
     *
     * @param authenticationProviders The DaoAuthenticationProvider implementations used for authenticating users.
     * @return The AuthenticationManager implementation.
     */
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider... authenticationProviders) {
        ProviderManager providerManager = new ProviderManager(authenticationProviders);
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }

    /**
     * Creates and configures DaoAuthenticationProvider implementations used for authenticating users.
     *
     * @param userService     Service used for retrieving user details from the database.
     * @param passwordEncoder Encoder used for encoding passwords before storing them in the database.
     * @return The DaoAuthenticationProvider implementation.
     */
    @Bean
    public DaoAuthenticationProvider[] authenticationProviders(UserService userService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new DaoAuthenticationProvider[]{authenticationProvider};
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //TODO: Switch to Argon2
        return new BCryptPasswordEncoder();
    }
}
