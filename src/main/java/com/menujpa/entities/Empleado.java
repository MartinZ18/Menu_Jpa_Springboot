package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;

@MappedSuperclass
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public abstract class Empleado extends Persona {

    @DecimalMin(value = "0.0", inclusive = true, message = "El salario no puede ser negativo")
    @Column(name = "salario")
    protected Double salario;

    @Past(message = "La fecha de vinculación debe ser una fecha pasada")
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_vinculacion")
    protected Date fechaVinculacion;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "El formato de hora de ingreso es inválido (HH:mm)")
    @Column(name = "hora_ingreso", length = 10)
    protected String horaIngreso;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "El formato de hora de salida es inválido (HH:mm)")
    @Column(name = "hora_salida", length = 10)
    protected String horaSalida;
}
