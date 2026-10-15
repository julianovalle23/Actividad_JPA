package com.technova.campusdesk.ticket.repository;

import com.technova.campusdesk.user.entity.Role;
import com.technova.campusdesk.ticket.entity.Ticket;
import com.technova.campusdesk.ticket.entity.TicketCategory;
import com.technova.campusdesk.ticket.entity.TicketPriority;
import com.technova.campusdesk.ticket.entity.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

// Ticket filters translated to SQL.
// ADMIN sees everything; TECHNICIAN only assigned; USER only own.
public final class TicketSpecifications {

  // Private constructor: this class only has static functions.
  private TicketSpecifications() {
  }

  // Limits tickets according to the requester's role.
  public static Specification<Ticket> visibleTo(Long userId, Role role) {
    return (root, query, criteria) -> switch (role) {
      case ADMIN -> criteria.conjunction(); // no filter: sees everything
      case TECHNICIAN -> criteria.equal(root.get("technician").get("id"), userId);
      case USER -> criteria.equal(root.get("requester").get("id"), userId);
    };
  }

  // Filters by status (null means no filter).
  public static Specification<Ticket> hasStatus(TicketStatus status) {
    return (root, query, criteria) ->
        status == null ? criteria.conjunction() : criteria.equal(root.get("status"), status);
  }

  // Filters by priority (null means no filter).
  public static Specification<Ticket> hasPriority(TicketPriority priority) {
    return (root, query, criteria) ->
        priority == null ? criteria.conjunction() : criteria.equal(root.get("priority"), priority);
  }

  // Filters by category (null means no filter).
  public static Specification<Ticket> hasCategory(TicketCategory category) {
    return (root, query, criteria) ->
        category == null ? criteria.conjunction() : criteria.equal(root.get("category"), category);
  }
}
