package com.vivero.tienda.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/*
 * En las entidades usamos @Getter/@Setter y NO @Data:
 * @Data genera equals/hashCode/toString con TODOS los campos, lo que recorre
 * las relaciones (bucles infinitos y cargas LAZY no deseadas).
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    // Lado inverso de la relacion. Las colecciones son LAZY: solo se cargan si se usan
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Planta> plantas = new ArrayList<>();
}
