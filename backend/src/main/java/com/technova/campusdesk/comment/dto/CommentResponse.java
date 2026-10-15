package com.technova.campusdesk.comment.dto;

import com.technova.campusdesk.comment.entity.Comment;
import com.technova.campusdesk.user.dto.UserSummary;
import java.time.Instant;

// Comment view with its author.
public record CommentResponse(
    Long id,
    String content,
    UserSummary author,
    Long ticketId,
    Instant createdAt) {

  // Converts the entity into a response.
  public static CommentResponse from(Comment comment) {
    return new CommentResponse(comment.getId(), comment.getContent(),
        UserSummary.from(comment.getAuthor()), comment.getTicket().getId(),
        comment.getCreatedAt());
  }
}
