package com.menujpa.services;
import com.menujpa.entities.Reserva;
import java.util.Date;

public interface ReservaService extends BaseService<Reserva, Long> {
    Reserva reservarMesa(String clienteUsuario, Long mesaId, Date fecha, String horaInicio, String horaFin,
                          Integer cantidadPersonas) throws Exception;
    void cancelarReservacion(Long reservaId, String usuarioSolicitante, boolean esGerente) throws Exception;
}
