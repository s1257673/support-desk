package com.supportdesk.support_desk.ticket.entity;


import com.supportdesk.support_desk.auth.entity.User;
import com.supportdesk.support_desk.ticket.enums.TicketPriority;
import com.supportdesk.support_desk.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable ticket reference like "TKT-00001"
    // Generated in the service layer, not by the database
    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(nullable = false, length = 255)
    private String title;

    // TEXT in MySQL — no length limit (unlike VARCHAR)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // MANY TICKETS → ONE CUSTOMER (the customer who created it)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;


    // MANY TICKETS → ONE CATEGORY (optional — a ticket may
    // not have a category when first created)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")   // nullable — no nullable=false
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    // -------------------------------------------------------
    // ONE TICKET → MANY MESSAGES
    //
    // @OneToMany   → "one ticket has many messages"
    // mappedBy     → "ticket" is the field name in TicketMessage
    //                that owns the relationship (holds the FK).
    //                We're just saying: "the other side manages this."
    // cascade      → if Ticket is deleted, delete its messages too
    // orphanRemoval → if a message is removed from this list,
    //                 delete it from the database too
    // -------------------------------------------------------
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TicketMessage> messages = new ArrayList<>();

    // -------------------------------------------------------
    // ONE TICKET → MANY ASSIGNMENTS (history of who was assigned)
    // -------------------------------------------------------
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TicketAssignment> assignments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Set only when status changes to RESOLVED
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}