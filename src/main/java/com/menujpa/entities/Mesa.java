package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "mesa")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Mesa extends Base {

    @NotNull(message = "El número de mesa es obligatorio")
    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser de al menos 1 persona")
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Override
    public String toString() {
        return "Mesa " + numero;
    }
}
