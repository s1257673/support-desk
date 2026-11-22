package com.supportdesk.support_desk.ticket.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Response DTO — shapes what we send back to the client.
// Notice: only the fields the client actually needs.
// No internal fields like database IDs of related objects.

@Getter
@Builder
public class TicketCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
