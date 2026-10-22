package com.technova.campusdesk.ticket.dto;

// SLA status of a ticket.
public record SlaStatusResponse(
    long minutosRestantes,
    boolean estaVencido,
    double porcentajeConsumido
) {
}
