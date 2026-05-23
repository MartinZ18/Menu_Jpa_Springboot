package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "chef")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Chef extends Empleado {

    @Size(max = 100, message = "La especialidad no puede superar 100 caracteres")
    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Size(max = 100, message = "La experiencia no puede superar 100 caracteres")
    @Column(name = "experiencia", length = 100)
    private String experiencia;

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_nacimiento")
    private Date fechaNacimiento;

    @Override
    public String toString() {
        return getApellido() + ", " + getNombre();
    }
}
