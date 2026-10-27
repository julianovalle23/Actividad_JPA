package com.technova.campusdesk.history.repository;

import com.technova.campusdesk.history.entity.StatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Access to the ticket_history table. Insert-only: no update or delete methods are used
 * (the database also blocks them with triggers).
 */
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

  /** History of a ticket, oldest change first, with who made each change (no N+1). */
  @EntityGraph(attributePaths = "changedBy")
  List<StatusHistory> findByTicketIdOrderByChangedAtAscIdAsc(Long ticketId);
}
