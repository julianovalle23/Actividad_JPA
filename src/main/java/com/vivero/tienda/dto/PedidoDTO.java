package com.vivero.tienda.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vivero.tienda.entity.EstadoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PedidoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "El cliente es obligatorio")
    @Positive(message = "El id del cliente debe ser positivo")
    private Long clienteId;

    // @Valid hace que tambien se validen los campos de cada detalle
    @Valid
    @NotEmpty(message = "El pedido debe tener al menos una planta")
    @Size(max = 50, message = "Un pedido no puede tener mas de 50 lineas")
    private List<DetallePedidoDTO> detalles = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String clienteNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate fecha;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private EstadoPedido estado;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal total;
}
