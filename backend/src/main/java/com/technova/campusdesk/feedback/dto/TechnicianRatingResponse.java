package com.technova.campusdesk.feedback.dto;

// Response for technician average rating.
public record TechnicianRatingResponse(
    Long technicianId,
    String technicianName,
    Double promedioCalificacion,
    Long totalCalificaciones
) {
}