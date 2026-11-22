package com.supportdesk.support_desk.ticket.dto;


import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    // Optional fields — customer may not know category or priority yet
    private Long categoryId;
    private TicketPriority priority;  // defaults to MEDIUM in entity if null
}