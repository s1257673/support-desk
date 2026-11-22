package com.supportdesk.support_desk.ticket.repository;


import com.supportdesk.support_desk.ticket.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    List<TicketCategory> findByIsActiveTrue();
    boolean existsByName(String name);
}