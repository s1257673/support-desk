package com.supportdesk.support_desk.ticket.service;

import com.supportdesk.support_desk.auth.entity.Role;
import com.supportdesk.support_desk.common.exception.BadRequestException;
import com.supportdesk.support_desk.common.exception.ForbiddenException;
import com.supportdesk.support_desk.ticket.dto.*;
import com.supportdesk.support_desk.ticket.entity.*;
import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.auth.repository.UserRepository;
import com.supportdesk.support_desk.common.exception.ResourceNotFoundException;
import com.supportdesk.support_desk.ticket.entity.Ticket;
import com.supportdesk.support_desk.ticket.entity.TicketCategory;
import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import com.supportdesk.support_desk.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.supportdesk.support_desk.auth.dto.UserSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService{

    private final TicketRepository ticketRepository;
    private final TicketCategoryRepository categoryRepository;
    private final TicketMessageRepository messageRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    // ------------------------------------------------------------------
    // CREATE TICKET
    // ------------------------------------------------------------------
    public TicketDetailResponse createTicket(CreateTicketRequest request, User customer) {

//        User customer = findUserOrThrow(customerId);

        // Category is optional at creation time
        TicketCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", request.getCategoryId()));
        }

        // Build the ticket — priority defaults to MEDIUM via @Builder.Default if null
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .customer(customer)
                .category(category)
                .priority(request.getPriority() != null
                        ? request.getPriority()
                        : TicketPriority.MEDIUM)
                .build();

        ticket.setTicketNumber("dummy");

        // First save — Hibernate assigns the auto-generated ID
        ticket = ticketRepository.save(ticket);

        // Now generate the human-readable ticket number using that ID
        // String.format("%05d", 7) → "00007"  → full: "TKT-00007"
        ticket.setTicketNumber("TKT-" + String.format("%05d", ticket.getId()));
        // Dirty checking will UPDATE the ticket_number column automatically
        // at the end of this transaction — no second save() needed
        System.out.println("Setted Tiken Number : "+ ticket.getTicketNumber());

        return mapToDetailResponse(ticket);
    }

    // ------------------------------------------------------------------
    // READ — SINGLE TICKET
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketById(Long id, User currentUser) {
        Ticket ticket = findTicketOrThrow(id);
        // CUSTOMER: can only view their own tickets
        // AGENT/ADMIN: can view any ticket
        if (currentUser.getRole() == Role.CUSTOMER &&
                !ticket.getCustomer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to view this ticket");
        }
        return mapToDetailResponse(ticket);
    }

    // ------------------------------------------------------------------
    // READ — ALL TICKETS (admin/create-ticket.html view, paginated)
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<TicketSummaryResponse> getAllTickets(TicketStatus status,
                                                     TicketPriority priority,
                                                     Long categoryId,
                                                     String keyword,
                                                     Pageable pageable) {
        Specification<Ticket> spec = TicketSpecification.withFilters(
                status, priority, categoryId, keyword, null
        );
        return ticketRepository.findAll(spec, pageable)
                .map(this::mapToSummaryResponse);
    }

    // Also update getMyTickets to support filtering:
    @Transactional(readOnly = true)
    public Page<TicketSummaryResponse> getMyTickets(User customer,
                                                    TicketStatus status,
                                                    Pageable pageable) {
        Specification<Ticket> spec = TicketSpecification.withFilters(
                status, null, null, null, customer
        );
        return ticketRepository.findAll(spec, pageable)
                .map(this::mapToSummaryResponse);
    }

    // ------------------------------------------------------------------
    // UPDATE STATUS
    // ------------------------------------------------------------------
    public TicketDetailResponse updateTicketStatus(Long id, UpdateTicketStatusRequest request) {
        Ticket ticket = findTicketOrThrow(id);

        // Cannot change status of an already CLOSED ticket
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot change status of a closed ticket");
        }

        ticket.setStatus(request.getStatus());

        // If resolving, record the exact timestamp for SLA calculations later
        if (request.getStatus() == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        return mapToDetailResponse(ticket);
    }

    // ------------------------------------------------------------------
    // ASSIGN TICKET TO AGENT
    // ------------------------------------------------------------------
    public TicketDetailResponse assignTicket(Long ticketId,
                                             AssignTicketRequest request,
                                             User admin) {
        Ticket ticket = findTicketOrThrow(ticketId);
        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.getAgentId()));
        if (agent.getRole() != Role.AGENT) {
            throw new BadRequestException("User is not an create-ticket.html");
        }
        assignmentRepository.findByTicketAndIsActiveTrue(ticket)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setUnassignedAt(LocalDateTime.now());
                });
        TicketAssignment assignment = TicketAssignment.builder()
                .ticket(ticket)
                .agent(agent)
                .assignedBy(admin)           // ← full User object
                .build();
        assignmentRepository.save(assignment);
        ticket.setStatus(TicketStatus.ASSIGNED);
        return mapToDetailResponse(ticket);
    }

    // ------------------------------------------------------------------
    // ADD MESSAGE TO TICKET
    // ------------------------------------------------------------------
    public TicketMessageResponse addMessage(Long ticketId,
                                            AddMessageRequest request,
                                            User sender) {
        Ticket ticket = findTicketOrThrow(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot add messages to a closed ticket");
        }

//        User sender = findUserOrThrow(senderId);
        if (sender.getRole() == Role.CUSTOMER &&
                !ticket.getCustomer().getId().equals(sender.getId())) {
            throw new ForbiddenException("You are not authorized to message this ticket");
        }

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .build();

        TicketMessage saved = messageRepository.save(message);

        // If customer replies to a WAITING_FOR_CUSTOMER ticket,
        // automatically move it back to IN_PROGRESS
        if (ticket.getStatus() == TicketStatus.WAITING_FOR_CUSTOMER
                && sender.getRole() == Role.CUSTOMER) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        return mapToMessageResponse(saved);
    }

    // ------------------------------------------------------------------
    // PRIVATE HELPERS
    // ------------------------------------------------------------------
    private Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private UserSummaryResponse mapToUserSummary(User user) {
        if (user == null) return null;
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private TicketMessageResponse mapToMessageResponse(TicketMessage message) {
        return TicketMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .sender(mapToUserSummary(message.getSender()))
                .messageType(message.getMessageType())
                .isAiGenerated(message.getIsAiGenerated())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private TicketSummaryResponse mapToSummaryResponse(Ticket ticket) {
        // Find the currently active create-ticket.html for this ticket (if any)
        String agentName = assignmentRepository
                .findByTicketAndIsActiveTrue(ticket)
                .map(a -> a.getAgent().getFirstName() + " " + a.getAgent().getLastName())
                .orElse(null);

        return TicketSummaryResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .customerName(ticket.getCustomer().getFirstName()
                        + " " + ticket.getCustomer().getLastName())
                .categoryName(ticket.getCategory() != null
                        ? ticket.getCategory().getName() : null)
                .assignedAgentName(agentName)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private TicketDetailResponse mapToDetailResponse(Ticket ticket) {
        // Load messages sorted oldest-first for the conversation thread
        List<TicketMessageResponse> messages = messageRepository
                .findByTicketOrderByCreatedAtAsc(ticket)
                .stream()
                .map(this::mapToMessageResponse)
                .toList();

        // Find currently active create-ticket.html
        UserSummaryResponse assignedAgent = assignmentRepository
                .findByTicketAndIsActiveTrue(ticket)
                .map(a -> mapToUserSummary(a.getAgent()))
                .orElse(null);

        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .customer(mapToUserSummary(ticket.getCustomer()))
                .categoryName(ticket.getCategory() != null
                        ? ticket.getCategory().getName() : null)
                .assignedAgent(assignedAgent)
                .messages(messages)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getAssignedTickets(User agent) {
        return assignmentRepository.findByAgentAndIsActiveTrue(agent)
                .stream()
                .map(assignment -> mapToSummaryResponse(assignment.getTicket()))
                .toList();
    }
}