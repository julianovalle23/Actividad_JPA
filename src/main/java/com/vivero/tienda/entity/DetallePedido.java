package com.vivero.tienda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedidos")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cantidad;

    // Se guarda el precio del momento de la compra (aunque luego cambie el de la planta)
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planta_id", nullable = false)
    private Planta planta;

    /**
     * Crea una linea de pedido tomando el precio actual de la planta.
     */
    public DetallePedido(Planta planta, int cantidad) {
        this.planta = planta;
        this.cantidad = cantidad;
        this.precioUnitario = planta.getPrecio();
        this.subtotal = planta.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
