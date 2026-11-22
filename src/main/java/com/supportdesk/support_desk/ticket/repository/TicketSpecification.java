package com.supportdesk.support_desk.ticket.repository;


import com.supportdesk.support_desk.ticket.entity.Ticket;
import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.supportdesk.support_desk.auth.entity.User;
import java.util.ArrayList;
import java.util.List;

// Specification is a functional interface that builds JPA Criteria predicates.
// Each static method returns one filter condition.
// The service combines them with .and() as needed.

public class TicketSpecification {

    // Filter by status
    public static Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    // Filter by priority
    public static Specification<Ticket> hasPriority(TicketPriority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    // Filter by category
    public static Specification<Ticket> hasCategoryId(Long categoryId) {
        return (root, query, cb) ->
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }

    // Filter by customer (for "my tickets" view)
    public static Specification<Ticket> hasCustomer(User customer) {
        return (root, query, cb) ->
                customer == null ? null : cb.equal(root.get("customer"), customer);
    }

    // Search by title keyword (case-insensitive)
    public static Specification<Ticket> titleContains(String keyword) {
        return (root, query, cb) ->
                keyword == null ? null
                        : cb.like(cb.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%");
    }

    // Build a combined specification from all active filters
    // Returns null predicates for inactive filters (JPA ignores null predicates)
    public static Specification<Ticket> withFilters(
            TicketStatus status,
            TicketPriority priority,
            Long categoryId,
            String keyword,
            User customer) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));
            if (priority != null)
                predicates.add(cb.equal(root.get("priority"), priority));
            if (categoryId != null)
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            if (keyword != null && !keyword.isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%"));
            if (customer != null)
                predicates.add(cb.equal(root.get("customer"), customer));

            // AND all active predicates together
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
