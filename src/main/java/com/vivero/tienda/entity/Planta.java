package com.vivero.tienda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "plantas")
@Getter
@Setter
@NoArgsConstructor
public class Planta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "nombre_cientifico", length = 100)
    private String nombreCientifico;

    // BigDecimal evita errores de redondeo con dinero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_luz", length = 10)
    private NivelLuz nivelLuz;

    @Column(name = "riego_cada_dias")
    private Integer riegoCadaDias;

    // LAZY: la categoria solo se consulta cuando se necesita
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    /*
     * Bloqueo optimista: si dos pedidos modifican el stock de la misma planta
     * a la vez, el segundo falla en lugar de sobrescribir al primero.
     */
    @Version
    @Column(nullable = false)
    private Integer version;

    public boolean tieneStock(int cantidad) {
        return stock >= cantidad;
    }

    public void descontarStock(int cantidad) {
        stock = stock - cantidad;
    }

    public void reponerStock(int cantidad) {
        stock = stock + cantidad;
    }
}
