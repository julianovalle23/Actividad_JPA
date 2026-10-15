package com.technova.campusdesk.comment.service;

import com.technova.campusdesk.comment.dto.CommentRequest;
import com.technova.campusdesk.comment.dto.CommentResponse;
import com.technova.campusdesk.comment.entity.Comment;
import com.technova.campusdesk.ticket.entity.Ticket;
import com.technova.campusdesk.ticket.entity.TicketStatus;
import com.technova.campusdesk.user.entity.User;
import com.technova.campusdesk.exception.BusinessRuleException;
import com.technova.campusdesk.exception.ResourceNotFoundException;
import com.technova.campusdesk.comment.repository.CommentRepository;
import com.technova.campusdesk.ticket.service.TicketService;
import com.technova.campusdesk.user.repository.UserRepository;
import com.technova.campusdesk.security.AuthUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Ticket comments.
@Service
public class CommentService {

  private final CommentRepository comments;
  private final UserRepository users;
  private final TicketService ticketService;

  // Spring only injects constructor dependencies.
  public CommentService(CommentRepository comments, UserRepository users,
      TicketService ticketService) {
    this.comments = comments;
    this.users = users;
    this.ticketService = ticketService;
  }

  // Lists comments in chronological order (only with ticket access).
  @Transactional(readOnly = true)
  public List<CommentResponse> list(Long ticketId, AuthUser actor) {
    ticketService.findAccessibleTicket(ticketId, actor);
    return comments.findByTicketIdOrderByCreatedAtAscIdAsc(ticketId)
        .stream().map(CommentResponse::from).toList();
  }

  // Adds a comment with the authenticated user as author.
  // A CERRADA ticket accepts no comments.
  @Transactional
  public CommentResponse add(Long ticketId, CommentRequest request, AuthUser actor) {
    Ticket ticket = ticketService.findAccessibleTicket(ticketId, actor);
    if (ticket.getStatus() == TicketStatus.CERRADA) {
      throw new BusinessRuleException("Un ticket CERRADA no admite comentarios");
    }
    User author = users.findById(actor.id())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    Comment saved = comments.save(new Comment(ticket, author, request.content().trim()));
    return CommentResponse.from(saved);
  }
}
