package com.menujpa.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class ReservaRequest {

    @NotNull(message = "La mesa es obligatoria")
    private Long mesaId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o en el futuro")
    private LocalDate fecha;

    @NotBlank(message = "La hora de inicio es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "El formato de hora de inicio es inválido (HH:mm)")
    private String horaInicio;

    @NotBlank(message = "La hora de fin es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "El formato de hora de fin es inválido (HH:mm)")
    private String horaFin;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "Debe ser al menos 1 persona")
    private Integer cantidadPersonas;
}
