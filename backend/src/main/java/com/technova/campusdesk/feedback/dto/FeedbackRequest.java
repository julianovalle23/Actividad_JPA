package com.technova.campusdesk.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Input for creating ticket feedback. */
public record FeedbackRequest(
    /** Required, between 1 and 5. */
    @NotNull(message = "La puntuacion es obligatoria")
    @Min(value = 1, message = "La puntuacion minima es 1")
    @Max(value = 5, message = "La puntuacion maxima es 5")
    Integer puntuacion,

    /** Optional, up to 500 characters. */
    @Size(max = 500, message = "El comentario es muy largo")
    String comentario
) {
}