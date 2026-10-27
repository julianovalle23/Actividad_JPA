package com.vivero.tienda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * READ_ONLY: el campo aparece en las respuestas, pero si el cliente lo envia se ignora.
 * El constructor con todos los campos lo usa la consulta JPQL de CategoriaRepository.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @Size(max = 200, message = "La descripcion no puede superar 200 caracteres")
    private String descripcion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long cantidadPlantas;
}
