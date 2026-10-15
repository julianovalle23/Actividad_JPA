package com.technova.campusdesk.ticket.dto;

import com.technova.campusdesk.ticket.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;

// Asks to move a ticket to a new status.
public record StatusChangeRequest(
    @NotNull(message = "Debe indicar el nuevo estado")
    TicketStatus status) {
}
