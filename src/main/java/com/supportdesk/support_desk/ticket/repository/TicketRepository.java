package com.supportdesk.support_desk.ticket.repository;

import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.ticket.entity.Ticket;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>,
        JpaSpecificationExecutor<Ticket> {

    // Customer views their own tickets — paginated
    // Page<T> returns both the data AND pagination metadata (total pages, total elements)
    Page<Ticket> findByCustomer(User customer, Pageable pageable);

    // Agent/Admin views tickets by status
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    // Look up a ticket by its human-readable number (TKT-00001)
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    // Count how many tickets a customer has open (for dashboard)
    long countByCustomerAndStatus(User customer, TicketStatus status);
}
