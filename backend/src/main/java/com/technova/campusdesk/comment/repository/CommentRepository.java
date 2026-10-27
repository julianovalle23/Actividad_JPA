package com.technova.campusdesk.comment.repository;

import com.technova.campusdesk.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Access to the comments table. Spring writes the code. */
public interface CommentRepository extends JpaRepository<Comment, Long> {

  /** Comments of a ticket, oldest first, with their author in the same query (no N+1). */
  @EntityGraph(attributePaths = "author")
  List<Comment> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);
}
