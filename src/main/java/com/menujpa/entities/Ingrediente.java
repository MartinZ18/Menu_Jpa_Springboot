package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "ingrediente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Ingrediente extends Base {

    @NotBlank(message = "La descripción del ingrediente es obligatoria")
    @Size(max = 200, message = "La descripción no puede superar 200 caracteres")
    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "cantidad_stock", nullable = false)
    private Integer cantidadStock = 0;

    @Override
    public String toString() {
        return descripcion;
    }
}
