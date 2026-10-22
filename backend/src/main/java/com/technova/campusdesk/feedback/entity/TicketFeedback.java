package com.technova.campusdesk.feedback.entity;

import com.technova.campusdesk.ticket.entity.Ticket;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

// Rating given by the requester once the ticket is CERRADA.
// One feedback per ticket (OneToOne with shared primary key).
@Entity
@Table(name = "ticket_feedback")
public class TicketFeedback {

  // Same value as the ticket id (shared PK).
  @Id
  private Long id;

  // Score from 1 to 5.
  @Column(name = "puntuacion", nullable = false)
  private Integer puntuacion;

  // Optional comment, max 500 characters.
  @Column(name = "comentario", length = 500)
  private String comentario;

  // Creation timestamp.
  @Column(name = "fecha_creacion", nullable = false, updatable = false)
  private Instant fechaCreacion;

  // The ticket this feedback belongs to (OneToOne).
  @OneToOne(optional = false)
  @JoinColumn(name = "id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
  private Ticket ticket;

  public TicketFeedback() {
    // Required by JPA.
  }

  public TicketFeedback(Ticket ticket, Integer puntuacion, String comentario) {
    this.ticket = ticket;
    this.id = ticket.getId(); // shared PK
    this.puntuacion = puntuacion;
    this.comentario = comentario;
    this.fechaCreacion = Instant.now();
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Integer getPuntuacion() { return puntuacion; }
  public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }

  public String getComentario() { return comentario; }
  public void setComentario(String comentario) { this.comentario = comentario; }

  public Instant getFechaCreacion() { return fechaCreacion; }
  public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }

  public Ticket getTicket() { return ticket; }
  public void setTicket(Ticket ticket) { this.ticket = ticket; }
}