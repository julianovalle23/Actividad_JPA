package com.technova.campusdesk.feedback.dto;

import com.technova.campusdesk.feedback.entity.TicketFeedback;
import java.time.Instant;

// Response for a ticket feedback.
public record FeedbackResponse(
    Long id,
    Integer puntuacion,
    String comentario,
    Instant fechaCreacion
) {
  // Converts the entity into a response.
  public static FeedbackResponse from(TicketFeedback feedback) {
    return new FeedbackResponse(
        feedback.getId(),
        feedback.getPuntuacion(),
        feedback.getComentario(),
        feedback.getFechaCreacion()
    );
  }
}