package com.supportdesk.support_desk.ticket.dto;


import com.supportdesk.support_desk.ticket.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    @NotNull(message = "Message type is required")
    private MessageType messageType;
}
