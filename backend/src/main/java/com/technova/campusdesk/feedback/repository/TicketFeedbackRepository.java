package com.technova.campusdesk.feedback.repository;

import com.technova.campusdesk.feedback.dto.TechnicianRatingResponse;
import com.technova.campusdesk.feedback.entity.TicketFeedback;
import com.technova.campusdesk.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Access to ticket_feedback table. */
@Repository
public interface TicketFeedbackRepository extends JpaRepository<TicketFeedback, Long> {

  /** Finds feedback by ticket id. */
  @Query("SELECT f FROM TicketFeedback f WHERE f.ticket.id = :ticketId")
  TicketFeedback findByTicketId(@Param("ticketId") Long ticketId);

  /** Calculates average rating and count for a technician. */
  @Query("SELECT new com.technova.campusdesk.feedback.dto.TechnicianRatingResponse(" +
         "   t.technician.id, " +
         "   t.technician.fullName, " +
         "   AVG(f.puntuacion), " +
         "   COUNT(f)" +
         ") " +
         "FROM TicketFeedback f " +
         "JOIN f.ticket t " +
         "WHERE t.technician.id = :technicianId " +
         "GROUP BY t.technician.id, t.technician.fullName")
  TechnicianRatingResponse findAverageRatingByTechnicianId(@Param("technicianId") Long technicianId);

  /** Checks if a technician has any rated tickets. */
  @Query("SELECT COUNT(f) > 0 FROM TicketFeedback f JOIN f.ticket t WHERE t.technician.id = :technicianId")
  boolean hasRatingsForTechnician(@Param("technicianId") Long technicianId);
}