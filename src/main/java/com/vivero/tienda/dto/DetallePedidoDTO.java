package com.vivero.tienda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/*
 * Al crear un pedido el cliente solo envia plantaId y cantidad.
 * El resto de campos los calcula el servidor (READ_ONLY).
 */
@Data
@NoArgsConstructor
public class DetallePedidoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "La planta es obligatoria")
    @Positive(message = "El id de la planta debe ser positivo")
    private Long plantaId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad minima es 1")
    @Max(value = 100, message = "La cantidad maxima por linea es 100")
    private Integer cantidad;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String plantaNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal precioUnitario;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal subtotal;
}
