package com.supportdesk.support_desk.ticket.dto;


import com.supportdesk.support_desk.auth.dto.UserSummaryResponse;
import com.supportdesk.support_desk.ticket.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TicketMessageResponse {
    private Long id;
    private String content;
    private UserSummaryResponse sender;
    private MessageType messageType;
    private Boolean isAiGenerated;
    private LocalDateTime createdAt;
}
