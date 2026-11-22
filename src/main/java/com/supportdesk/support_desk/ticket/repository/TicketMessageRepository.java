package com.supportdesk.support_desk.ticket.repository;


import com.supportdesk.support_desk.ticket.entity.Ticket;
import com.supportdesk.support_desk.ticket.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    // Fetch the full conversation thread for a ticket, oldest first
    List<TicketMessage> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
