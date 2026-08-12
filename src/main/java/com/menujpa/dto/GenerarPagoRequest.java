package com.menujpa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GenerarPagoRequest {

    @NotNull(message = "El id del empleado es obligatorio")
    private Long empleadoId;

    @NotBlank(message = "El rol del empleado es obligatorio (CHEF o MESERO)")
    private String rol;
}
