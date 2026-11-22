package com.supportdesk.support_desk.ticket.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignTicketRequest {

    @NotNull(message = "Agent ID is required")
    private Long agentId;
}
