package com.example.Homebank.businessLogic.security;

import com.example.Homebank.businessLogic.services.UserService;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.entities.UserStatus;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

/**
 * Filter to check if the user is enabled before allowing access to protected resources.
 * If the user is not enabled, a 403 Forbidden response is returned with an appropriate error message.
 */
@Component
@RequiredArgsConstructor
public class UserStatusFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(UserStatusFilter.class);
    private static final Set<String> ALLOWED_PENDING_ACTIVATION_ENDPOINTS = Set.of(
            ApiPaths.AUTH + ApiPaths.ACTIVATE,
            ApiPaths.AUTH + ApiPaths.RESEND_ACTIVATION,
            ApiPaths.AUTH + ApiPaths.SIGN_OUT
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    /**
     * Blocks activated-only endpoints for users whose status is {@code ACTIVATION_PENDING}.
     *
     * @param request     Incoming HTTP request.
     * @param response    Outgoing HTTP response.
     * @param filterChain Remaining filter chain.
     * @throws ServletException If filter processing fails.
     * @throws IOException      If writing the response fails.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestPath = request.getRequestURI();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String email) {
            UserEntity userEntity = (UserEntity) userService.loadUserByUsername(email);

            if (userEntity.getStatus() == UserStatus.ACTIVATION_PENDING
                    && !ALLOWED_PENDING_ACTIVATION_ENDPOINTS.contains(requestPath)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");

                ApiError error = new ApiError(
                        Instant.now(),
                        HttpStatus.FORBIDDEN.value(),
                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                        "ACCOUNT_NOT_ACTIVATED",
                        "Please activate your account first.",
                        requestPath,
                        null
                );

                objectMapper.writeValue(response.getWriter(), error);

                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
