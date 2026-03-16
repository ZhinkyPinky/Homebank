package com.example.Homebank.businessLogic.security;

import com.example.Homebank.businessLogic.services.UserService;
import com.example.Homebank.error.ApiErrorCode;
import com.example.Homebank.exceptions.authentication.TokenType;
import com.example.Homebank.presentation.dto.error.ApiError;
import com.example.Homebank.presentation.dto.error.TokenErrorDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Authenticates requests using the access JWT from the {@code Authorization} header.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final AccessJwtUtil jwtUtil;
    private final UserService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Authenticates a user based on a provided JWT.
     *
     * @param request     Request sent by a user.
     * @param response    Response to send to the user.
     * @param filterChain Filters to pass the request through.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("Processing JWT authentication for request: {}", request.getRequestURI());

        String authHeader = request.getHeader(AUTHORIZATION);
        String username = null;
        String jwt = null;

        //Find the username of the subject making the request if JWT exists.
        if (authHeader != null && authHeader.startsWith("Bearer")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractEmail(jwt);
                logger.debug("Extracted JWT for username: {}", username);
            } catch (ExpiredJwtException e) {
                logger.error("JWT expired: {}", e.getMessage());
                writeErrorResponse(
                        response,
                        request.getRequestURI(),
                        HttpStatus.UNAUTHORIZED,
                        ApiErrorCode.TOKEN_EXPIRED,
                        "Token expired."
                );
                return;
            } catch (JwtException | IllegalArgumentException e) {
                logger.error("Invalid JWT: {}", e.getMessage(), e);
                writeErrorResponse(
                        response,
                        request.getRequestURI(),
                        HttpStatus.UNAUTHORIZED,
                        ApiErrorCode.TOKEN_INVALID,
                        "Invalid token."
                );
                return;
            }
        } else {
            logger.warn("No Bearer token found in Authorization header");
        }

        //Load the user and authenticate if the token is valid.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Loading user details for username: {}", username);

            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (AuthenticationException e) {
                logger.warn("JWT subject is invalid or no longer exists: {}", username);
                writeErrorResponse(
                        response,
                        request.getRequestURI(),
                        HttpStatus.UNAUTHORIZED,
                        ApiErrorCode.TOKEN_INVALID,
                        "Invalid token."
                );
                return;
            }

            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {
                logger.debug("JWT is valid for username: {}", username);

                UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(userDetails.getUsername(), null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                logger.info("Authenticated user: {}", username);
            } else {
                logger.warn("Invalid JWT for username: {}", username);
                writeErrorResponse(
                        response,
                        request.getRequestURI(),
                        HttpStatus.UNAUTHORIZED,
                        ApiErrorCode.TOKEN_INVALID,
                        "Invalid token."
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
        logger.info("JWT authentication completed for request: {}", request.getRequestURI());
    }

    /**
     * Writes a standardized JSON error payload for JWT authentication failures.
     *
     * @param response Target HTTP response.
     * @param path     Request path that failed.
     * @param status   HTTP status to return.
     * @param code     Stable machine-readable error code.
     * @param message  Human-readable message for the client.
     * @throws IOException If writing the response body fails.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            String path,
            HttpStatus status,
            ApiErrorCode code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                new TokenErrorDetail(TokenType.ACCESS.name())
        );

        objectMapper.writeValue(response.getWriter(), apiError);
        response.getWriter().flush();
    }
}
