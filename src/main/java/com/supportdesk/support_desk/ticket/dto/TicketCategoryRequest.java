package com.supportdesk.support_desk.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// This DTO is used for BOTH create and update operations.
// @NotBlank → field must not be null, empty, or just whitespace
// @Size     → enforces length limits matching our database column

@Getter
@Setter
public class TicketCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}