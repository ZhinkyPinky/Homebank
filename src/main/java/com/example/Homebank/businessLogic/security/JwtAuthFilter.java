package com.example.Homebank.businessLogic.security;

import com.example.Homebank.businessLogic.services.CustomerService;
import com.example.Homebank.businessLogic.services.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final AccessJwtUtil jwtUtil;
    private final UserService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            logger.info("Processing JWT authentication for request: {}", request.getRequestURI());

            String authHeader = request.getHeader(AUTHORIZATION);
            String username = null;
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer")) {
                jwt = authHeader.substring(7);
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted JWT for username: {}", username);
            } else {
                logger.warn("No Bearer token found in Authorization header");
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Loading user details for username: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    logger.debug("JWT is valid for username: {}", username);

                    UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(userDetails.getUsername(), null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    logger.info("Authenticated user: {}", username);
                } else {
                    logger.warn("Invalid JWT for username: {}", username);
                }
            }

            filterChain.doFilter(request, response);

            logger.info("JWT authentication completed for request: {}", request.getRequestURI());
        } catch (ExpiredJwtException e) {
            logger.error("JWT expired: {}", e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired.");
            response.getWriter().flush();
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage(), e);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token.");
            response.getWriter().flush();
        }
    }
}
