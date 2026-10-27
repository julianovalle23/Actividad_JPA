package com.technova.campusdesk.feedback.controller;

import com.technova.campusdesk.feedback.dto.FeedbackRequest;
import com.technova.campusdesk.feedback.dto.FeedbackResponse;
import com.technova.campusdesk.feedback.dto.TechnicianRatingResponse;
import com.technova.campusdesk.feedback.service.TicketFeedbackService;
import com.technova.campusdesk.security.AuthUser;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Feedback endpoints: rating tickets and technician averages. */
@RestController
@RequestMapping("/api")
public class TicketFeedbackController {

  private final TicketFeedbackService feedbackService;

  public TicketFeedbackController(TicketFeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  /**
   * POST /api/tickets/{id}/feedback
   * Only the ticket creator (USER) can rate, only when ticket is CERRADA,
   * and only once per ticket (returns 409 if already rated).
   */
  @PostMapping("/tickets/{id}/feedback")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<FeedbackResponse> createFeedback(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthUser actor,
      @PathVariable Long id,
      @Valid @RequestBody FeedbackRequest request) {
    FeedbackResponse response = feedbackService.createFeedback(id, request, actor);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * GET /api/technicians/{id}/promedio-calificacion
   * Only ADMIN can access. Returns average rating and total count.
   */
  @GetMapping("/technicians/{id}/promedio-calificacion")
  @PreAuthorize("hasRole('ADMIN')")
  public TechnicianRatingResponse getTechnicianAverageRating(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthUser actor,
      @PathVariable Long id) {
    return feedbackService.getTechnicianAverageRating(id, actor);
  }
}