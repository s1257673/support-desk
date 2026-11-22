package com.supportdesk.support_desk.admin.service;


import com.supportdesk.support_desk.admin.dto.CreateAgentRequest;
import com.supportdesk.support_desk.auth.dto.UserSummaryResponse;
import com.supportdesk.support_desk.auth.entity.Role;
import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.auth.repository.UserRepository;
import com.supportdesk.support_desk.common.exception.BadRequestException;
import com.supportdesk.support_desk.common.exception.ResourceNotFoundException;
import com.supportdesk.support_desk.ticket.repository.TicketAssignmentRepository;
import com.supportdesk.support_desk.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    // --- User Management ---

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToSummary)
                .toList();
    }

    // Admin creates create-ticket.html accounts (customers self-register)
    public UserSummaryResponse createAgent(CreateAgentRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        User agent = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.AGENT)
                .build();

        return mapToSummary(userRepository.save(agent));
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsActive(!user.getIsActive());
        // dirty checking handles the update
    }

    // --- Dashboard Stats ---

    @Transactional(readOnly = true)
    public Map<String, Long> getTicketStats() {
        return Map.of(
                "total",        ticketRepository.count(),
                "open",         ticketRepository.count(),
                "resolved",     ticketRepository.count(),
                "escalated",    ticketRepository.count()
        );
        // We'll improve this with proper @Query methods soon
    }

    // --- Agent: get their assigned tickets ---
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getActiveAgents() {
        return userRepository.findByRoleAndIsActive(Role.AGENT, true)
                .stream().map(this::mapToSummary).toList();
    }

    private UserSummaryResponse mapToSummary(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}