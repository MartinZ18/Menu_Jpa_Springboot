package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "gerente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Gerente extends Persona {

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_nacimiento")
    private Date fechaNacimiento;

    @Override
    public String toString() {
        return getApellido() + ", " + getNombre();
    }
}
