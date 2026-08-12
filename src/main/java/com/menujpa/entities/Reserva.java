package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "reserva")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Reserva extends Base {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idCliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idMesa", nullable = false)
    private Mesa mesa;

    @NotNull(message = "La fecha es obligatoria")
    @Temporal(TemporalType.DATE)
    @Column(name = "fecha", nullable = false)
    private Date fecha;

    @NotBlank(message = "La hora de inicio es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "El formato de hora de inicio es inválido (HH:mm)")
    @Column(name = "hora_inicio", length = 10, nullable = false)
    private String horaInicio;

    @NotBlank(message = "La hora de fin es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "El formato de hora de fin es inválido (HH:mm)")
    @Column(name = "hora_fin", length = 10, nullable = false)
    private String horaFin;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe ser al menos 1 persona")
    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "ACTIVA";

    @Override
    public String toString() {
        return "Reserva #" + getId();
    }
}
