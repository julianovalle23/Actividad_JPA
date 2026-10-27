package com.technova.campusdesk.feedback.service;

import com.technova.campusdesk.feedback.dto.FeedbackRequest;
import com.technova.campusdesk.feedback.dto.FeedbackResponse;
import com.technova.campusdesk.feedback.dto.TechnicianRatingResponse;
import com.technova.campusdesk.feedback.entity.TicketFeedback;
import com.technova.campusdesk.feedback.repository.TicketFeedbackRepository;
import com.technova.campusdesk.ticket.entity.Ticket;
import com.technova.campusdesk.ticket.entity.TicketStatus;
import com.technova.campusdesk.ticket.repository.TicketRepository;
import com.technova.campusdesk.user.entity.Role;
import com.technova.campusdesk.user.entity.User;
import com.technova.campusdesk.user.repository.UserRepository;
import com.technova.campusdesk.exception.BadRequestException;
import com.technova.campusdesk.exception.BusinessRuleException;
import com.technova.campusdesk.exception.ForbiddenException;
import com.technova.campusdesk.exception.ResourceNotFoundException;
import com.technova.campusdesk.security.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for ticket feedback. */
@Service
public class TicketFeedbackService {

  private final TicketFeedbackRepository feedbackRepository;
  private final TicketRepository ticketRepository;
  private final UserRepository userRepository;

  public TicketFeedbackService(TicketFeedbackRepository feedbackRepository,
                               TicketRepository ticketRepository,
                               UserRepository userRepository) {
    this.feedbackRepository = feedbackRepository;
    this.ticketRepository = ticketRepository;
    this.userRepository = userRepository;
  }

  /** Creates feedback for a closed ticket. Only the requester can do it. */
  @Transactional
  public FeedbackResponse createFeedback(Long ticketId, FeedbackRequest request, AuthUser actor) {
    // 1. Load ticket
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado"));

    // 2. Only the requester (USER) can create feedback
    if (actor.role() != Role.USER || !isRequester(ticket, actor)) {
      throw new ForbiddenException("Solo el solicitante puede calificar su ticket");
    }

    // 3. Ticket must be strictly CERRADA
    if (ticket.getStatus() != TicketStatus.CERRADA) {
      throw new BusinessRuleException("Solo se puede calificar un ticket en estado CERRADA");
    }

    // 4. No duplicate feedback (OneToOne with shared PK enforces this at DB level,
    //    but we check first to return a clean 409)
    if (feedbackRepository.existsById(ticketId)) {
      throw new BusinessRuleException("El ticket ya ha sido calificado");
    }

    // 5. Create and save feedback
    TicketFeedback feedback = new TicketFeedback(ticket, request.puntuacion(), request.comentario());
    TicketFeedback saved = feedbackRepository.save(feedback);

    return FeedbackResponse.from(saved);
  }

  /** Returns average rating for a technician. ADMIN only. */
  @Transactional(readOnly = true)
  public TechnicianRatingResponse getTechnicianAverageRating(Long technicianId, AuthUser actor) {
    // Only ADMIN can access this endpoint
    if (actor.role() != Role.ADMIN) {
      throw new ForbiddenException("Solo los administradores pueden consultar el promedio de calificaciones");
    }

    // Verify technician exists and has TECHNICIAN role
    User technician = userRepository.findById(technicianId)
        .orElseThrow(() -> new ResourceNotFoundException("Tecnico no encontrado"));
    if (technician.getRole() != Role.TECHNICIAN) {
      throw new BadRequestException("El usuario no es un tecnico");
    }

    // Check if there are any ratings
    if (!feedbackRepository.hasRatingsForTechnician(technicianId)) {
      return new TechnicianRatingResponse(technicianId, technician.getFullName(), 0.0, 0L);
    }

    return feedbackRepository.findAverageRatingByTechnicianId(technicianId);
  }

  /** True when the actor reported the ticket. */
  private boolean isRequester(Ticket ticket, AuthUser actor) {
    return ticket.getRequester() != null && ticket.getRequester().getId().equals(actor.id());
  }
}