package com.menujpa.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PedidoRequest {

    @NotEmpty(message = "El pedido debe tener al menos un cliente")
    private List<Long> clienteIds;

    @NotEmpty(message = "El pedido debe tener al menos un alimento")
    private List<Long> alimentoIds;
}
