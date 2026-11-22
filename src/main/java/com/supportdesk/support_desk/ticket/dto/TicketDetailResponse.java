package com.supportdesk.support_desk.ticket.dto;

import com.supportdesk.support_desk.auth.dto.UserSummaryResponse;
import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TicketDetailResponse {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private UserSummaryResponse customer;
    private String categoryName;
    private UserSummaryResponse assignedAgent;    // null if not yet assigned
    private List<TicketMessageResponse> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
