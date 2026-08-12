package com.menujpa.services;

import com.menujpa.entities.Cliente;
import com.menujpa.entities.Mesa;
import com.menujpa.entities.Reserva;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.MesaRepository;
import com.menujpa.repositories.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Service
public class ReservaServiceImpl extends BaseServiceImpl<Reserva, Long> implements ReservaService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private MesaRepository mesaRepository;
    @Autowired private ReservaRepository reservaRepository;

    public ReservaServiceImpl(BaseRepository<Reserva, Long> baseRepository) { super(baseRepository); }

    @Override @Transactional
    public Reserva reservarMesa(String clienteUsuario, Long mesaId, Date fecha, String horaInicio, String horaFin,
                                 Integer cantidadPersonas) throws Exception {
        try {
            Cliente cliente = clienteRepository.findByUsuario(clienteUsuario)
                .orElseThrow(() -> new Exception("Cliente no encontrado: " + clienteUsuario));

            // Lock pesimista (SELECT ... FOR UPDATE) sobre la mesa: sin esto, dos reservas
            // concurrentes para el mismo horario podrian pasar el chequeo de solapamiento las
            // dos antes de que ninguna haga commit (TOCTOU) y terminar con una doble reserva.
            // Al bloquear la fila de la mesa, la segunda transaccion espera a que la primera
            // termine y recien ahi hace su propio chequeo, ya viendo la reserva nueva. Otras
            // mesas no se ven afectadas -- el lock es por fila, no global.
            Mesa mesa = mesaRepository.findByIdForUpdate(mesaId)
                .orElseThrow(() -> new Exception("Mesa no encontrada con id: " + mesaId));

            if (cantidadPersonas > mesa.getCapacidad())
                throw new Exception("La mesa " + mesa.getNumero() + " tiene capacidad para "
                    + mesa.getCapacidad() + " personas.");

            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);
            if (!fin.isAfter(inicio))
                throw new Exception("La hora de fin debe ser posterior a la hora de inicio.");

            List<Reserva> reservasDelDia = reservaRepository.findByMesaIdAndFechaAndEstado(mesaId, fecha, "ACTIVA");
            for (Reserva existente : reservasDelDia) {
                LocalTime otroInicio = LocalTime.parse(existente.getHoraInicio());
                LocalTime otroFin = LocalTime.parse(existente.getHoraFin());
                boolean solapa = inicio.isBefore(otroFin) && otroInicio.isBefore(fin);
                if (solapa)
                    throw new Exception("La mesa " + mesa.getNumero() + " ya está reservada de "
                        + existente.getHoraInicio() + " a " + existente.getHoraFin() + " ese día.");
            }

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setMesa(mesa);
            reserva.setFecha(fecha);
            reserva.setHoraInicio(horaInicio);
            reserva.setHoraFin(horaFin);
            reserva.setCantidadPersonas(cantidadPersonas);
            reserva.setEstado("ACTIVA");

            return baseRepository.save(reserva);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public void cancelarReservacion(Long reservaId, String usuarioSolicitante, boolean esGerente) throws Exception {
        try {
            Reserva reserva = baseRepository.findById(reservaId)
                .orElseThrow(() -> new Exception("Reserva no encontrada con id: " + reservaId));

            if (!esGerente && !reserva.getCliente().getUsuario().equals(usuarioSolicitante))
                throw new Exception("No podés cancelar una reserva que no es tuya.");

            if ("CANCELADA".equals(reserva.getEstado()))
                throw new Exception("La reserva ya estaba cancelada.");

            reserva.setEstado("CANCELADA");
            baseRepository.save(reserva);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override
    public List<Reserva> misReservas(String clienteUsuario) throws Exception {
        try {
            return reservaRepository.findByClienteUsuario(clienteUsuario);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override
    public Reserva obtenerParaUsuario(Long reservaId, String usuarioSolicitante, boolean esStaff) throws Exception {
        try {
            Reserva reserva = baseRepository.findById(reservaId)
                .orElseThrow(() -> new Exception("Entidad no encontrada con id: " + reservaId));

            if (!esStaff && !reserva.getCliente().getUsuario().equals(usuarioSolicitante))
                throw new Exception("No podés ver una reserva que no es tuya.");

            return reserva;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
}
