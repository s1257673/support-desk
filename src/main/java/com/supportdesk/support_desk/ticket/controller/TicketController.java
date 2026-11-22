package com.supportdesk.support_desk.ticket.controller;


import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.common.config.SecurityUtils;
import com.supportdesk.support_desk.common.responce.ApiResponse;
import com.supportdesk.support_desk.ticket.dto.CreateTicketRequest;
import com.supportdesk.support_desk.ticket.dto.TicketDetailResponse;
import com.supportdesk.support_desk.ticket.dto.TicketSummaryResponse;
import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import com.supportdesk.support_desk.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.supportdesk.support_desk.ticket.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SecurityUtils securityUtils;

    // Only CUSTOMER can create tickets
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {

        User currentUser = securityUtils.getCurrentUser();  // from JWT, no header needed
        TicketDetailResponse ticket = ticketService.createTicket(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully", ticket));
    }

    // Single ticket — service handles ownership check
    // CUSTOMER sees own, AGENT/ADMIN see any
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketById(
            @PathVariable Long id) {

        User currentUser = securityUtils.getCurrentUser();
        TicketDetailResponse ticket = ticketService.getTicketById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved", ticket));
    }

    // All tickets — only ADMIN and AGENT
// Replace the existing getAllTickets method:
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<Page<TicketSummaryResponse>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved",
                ticketService.getAllTickets(status, priority, categoryId, keyword, pageable)));
    }

    // Replace getMyTickets:
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<TicketSummaryResponse>>> getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TicketStatus status) {

        User currentUser = securityUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success("My tickets retrieved",
                ticketService.getMyTickets(currentUser, status, pageable)));
    }

    // Status update — AGENT or ADMIN only
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Status updated",
                        ticketService.updateTicketStatus(id, request))
        );
    }

    // Assign ticket — ADMIN only
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request) {

        User currentUser = securityUtils.getCurrentUser();
        return ResponseEntity.ok(
                ApiResponse.success("Ticket assigned successfully",
                        ticketService.assignTicket(id, request, currentUser))
        );
    }

    // Add message — service handles ownership (customer → own ticket only)
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<TicketMessageResponse>> addMessage(
            @PathVariable Long id,
            @Valid @RequestBody AddMessageRequest request) {

        User currentUser = securityUtils.getCurrentUser();
        TicketMessageResponse message = ticketService.addMessage(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message added", message));
    }

    // Tickets currently assigned to the logged-in create-ticket.html
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<List<TicketSummaryResponse>>> getAssignedTickets() {
        User currentUser = securityUtils.getCurrentUser();
        List<TicketSummaryResponse> tickets = ticketService.getAssignedTickets(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Assigned tickets retrieved", tickets));
    }
}
