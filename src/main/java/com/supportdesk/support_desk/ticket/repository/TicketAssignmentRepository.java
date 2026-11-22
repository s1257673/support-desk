package com.supportdesk.support_desk.ticket.repository;


import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.ticket.entity.Ticket;
import com.supportdesk.support_desk.ticket.entity.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Long> {

    // Find the CURRENT active assignment for a ticket
    // Only one should ever be active at a time
    Optional<TicketAssignment> findByTicketAndIsActiveTrue(Ticket ticket);

    // Find all tickets currently assigned to an create-ticket.html
    List<TicketAssignment> findByAgentAndIsActiveTrue(User agent);
}