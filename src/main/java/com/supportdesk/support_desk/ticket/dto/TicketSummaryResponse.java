package com.supportdesk.support_desk.ticket.dto;

import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TicketSummaryResponse {
    private Long id;
    private String ticketNumber;
    private String title;
    private TicketStatus status;
    private TicketPriority priority;
    private String customerName;        // "John Doe" — no need for full User object
    private String categoryName;        // "Billing" — no need for full Category object
    private String assignedAgentName;   // null if unassigned
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}