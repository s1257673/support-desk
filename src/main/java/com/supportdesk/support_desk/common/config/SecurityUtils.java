package com.supportdesk.support_desk.common.config;


import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// A utility component any class can inject to get the currently logged-in user.
// Reads from the SecurityContextHolder which JwtAuthFilter populated.

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        // getPrincipal() returns the UserDetails object we stored in JwtAuthFilter
        // We cast it to our User entity (which implements UserDetails)
        return (User) authentication.getPrincipal();
    }
}