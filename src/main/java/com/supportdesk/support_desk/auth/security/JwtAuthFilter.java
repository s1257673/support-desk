package com.supportdesk.support_desk.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.supportdesk.support_desk.auth.service.JwtService;
import java.io.IOException;

// OncePerRequestFilter guarantees this filter runs EXACTLY once per request,
// even if the request is forwarded internally (some filters run multiple times).

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract the Authorization header
        // Format must be: "Bearer eyJhbGci..."
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token present — pass to next filter.
            // If the endpoint requires auth, the AuthorizationFilter will reject it with 401.
            // If the endpoint is public (like /api/auth/**), it passes through fine.
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract the token (remove "Bearer " prefix — 7 characters)
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Token is malformed or signature invalid — treat as unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: If email extracted AND no authentication in context yet
        // (we check context to avoid re-processing on the same request)
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 4: Load user from DB — this also checks isActive via isEnabled()
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Step 5: Validate token (signature + expiry + email match)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 6: Create authentication token and put in SecurityContext.
                // null = no credentials needed (token already validated)
                // getAuthorities() = ["ROLE_CUSTOMER"] etc.
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,              // principal — the User object
                                null,                     // credentials — not needed post-auth
                                userDetails.getAuthorities()
                        );

                // Attach request details (IP address etc.) to the auth token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 7: Store in SecurityContextHolder
                // Now the entire application can call:
                // SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                // to get the current logged-in User object — in any class, any layer
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 8: Always pass to the next filter, regardless of auth result
        // The AuthorizationFilter later decides whether to allow or block
        filterChain.doFilter(request, response);
    }
}