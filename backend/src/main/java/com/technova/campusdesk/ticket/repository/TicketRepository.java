package com.technova.campusdesk.ticket.repository;

import com.technova.campusdesk.ticket.entity.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Access to the tickets table. Role filters live in TicketSpecifications.
 * Requester and technician are LAZY in the entity; the entity graphs below load
 * them in the same SELECT (JOIN) so building TicketResponse never causes N+1 queries.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

  /** Filtered list with requester and technician already joined. */
  @Override
  @EntityGraph(attributePaths = {"requester", "technician"})
  List<Ticket> findAll(Specification<Ticket> spec, Sort sort);

  /** One ticket with requester and technician already joined. */
  @Override
  @EntityGraph(attributePaths = {"requester", "technician"})
  Optional<Ticket> findById(Long id);
}
