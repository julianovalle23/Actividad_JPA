package com.vivero.tienda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vivero.tienda.entity.NivelLuz;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PlantaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombre;

    @Size(max = 100, message = "El nombre cientifico no puede superar 100 caracteres")
    private String nombreCientifico;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    @Digits(integer = 8, fraction = 2, message = "El precio admite como maximo 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    // Valores permitidos: SOL, MEDIA o SOMBRA
    private NivelLuz nivelLuz;

    @Min(value = 1, message = "El riego debe ser al menos cada 1 dia")
    @Max(value = 60, message = "El riego no puede ser mayor a cada 60 dias")
    private Integer riegoCadaDias;

    @NotNull(message = "La categoria es obligatoria")
    @Positive(message = "El id de la categoria debe ser positivo")
    private Long categoriaId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String categoriaNombre;
}
