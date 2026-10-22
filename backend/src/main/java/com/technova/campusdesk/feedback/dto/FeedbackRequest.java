package com.technova.campusdesk.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// Input for creating ticket feedback.
public record FeedbackRequest(
    // Must be between 1 and 5.
    @Min(1) @Max(5)
    Integer puntuacion,

    // Optional, up to 500 characters.
    @Size(max = 500)
    String comentario
) {
}