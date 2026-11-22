package com.supportdesk.support_desk.auth.service;



import com.supportdesk.support_desk.auth.dto.AuthResponse;
import com.supportdesk.support_desk.auth.dto.LoginRequest;
import com.supportdesk.support_desk.auth.dto.RegisterRequest;
import com.supportdesk.support_desk.auth.entity.Role;
import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.auth.repository.UserRepository;
import com.supportdesk.support_desk.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.jwt.expiration-ms}")
    private long expirationMs;

    // ----------------------------------------------------------------
    // REGISTER — only creates CUSTOMER accounts
    // Agent accounts are created by Admin through the admin panel (Level 3+)
    // ----------------------------------------------------------------
    public AuthResponse register(RegisterRequest request) {

        // Check email uniqueness before attempting to save
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                // passwordEncoder.encode() runs BCrypt with strength 12
                // The hash is different every time even for the same password (salt)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)      // self-registration always = CUSTOMER
                .build();

        userRepository.save(user);

        // Generate and return a token immediately so the user is logged in
        // right after registration — no need for a separate login step
        String token = jwtService.generateToken(user);

        return buildAuthResponse(user, token);
    }

    // ----------------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------------
    public AuthResponse login(LoginRequest request) {

        // authenticationManager.authenticate() does two things internally:
        // 1. Calls UserDetailsServiceImpl.loadUserByUsername(email) → loads user from DB
        // 2. Calls passwordEncoder.matches(rawPassword, storedHash) → verifies BCrypt hash
        //
        // If email not found → UsernameNotFoundException → BadCredentialsException
        // If password wrong  → BadCredentialsException
        // If isActive=false  → DisabledException
        // All of these are caught by our GlobalExceptionHandler
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, credentials are valid
        User user = (User) auth.getPrincipal(); // Can't be empty — authenticate() just confirmed it exists

        String token = jwtService.generateToken(user);
        return buildAuthResponse(user, token);
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPER
    // ----------------------------------------------------------------
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .expiresIn(expirationMs)
                .build();
    }
}
