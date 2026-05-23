package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "alimento")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_alimento", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Alimento")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Alimento extends Base {

    @NotBlank(message = "El nombre del alimento es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(name = "nombre")
    private String nombre;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Column(name = "precio")
    private Double precio;

    @Size(max = 50, message = "La unidad no puede superar 50 caracteres")
    @Column(name = "unidad")
    private String unidad;

    @Column(name = "tipo")
    private String tipo;

    @Override
    public String toString() {
        return nombre;
    }
}
