package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "pago")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Pago extends Base {

    @NotBlank(message = "El nombre del empleado es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    @Column(name = "empleado_nombre", length = 200, nullable = false)
    private String empleadoNombre;

    @NotBlank(message = "El rol del empleado es obligatorio")
    @Size(max = 20, message = "El rol no puede superar 20 caracteres")
    @Column(name = "empleado_rol", length = 20, nullable = false)
    private String empleadoRol;

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto no puede ser negativo")
    @Column(name = "monto", nullable = false)
    private Double monto;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_pago", nullable = false)
    private Date fechaPago;

    @Override
    public String toString() {
        return "Pago #" + getId() + " - " + empleadoNombre;
    }
}
