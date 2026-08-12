package com.menujpa.services;

import com.menujpa.entities.Cliente;
import com.menujpa.entities.Mesa;
import com.menujpa.entities.Reserva;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.MesaRepository;
import com.menujpa.repositories.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private BaseRepository<Reserva, Long> baseRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private ReservaServiceImpl reservaService;

    private final Date fecha = Date.valueOf("2026-09-01");

    @BeforeEach
    void setUp() {
        reservaService = new ReservaServiceImpl(baseRepository);
        ReflectionTestUtils.setField(reservaService, "clienteRepository", clienteRepository);
        ReflectionTestUtils.setField(reservaService, "mesaRepository", mesaRepository);
        ReflectionTestUtils.setField(reservaService, "reservaRepository", reservaRepository);
    }

    private Mesa mesaDeCapacidad(int capacidad) {
        Mesa mesa = new Mesa();
        mesa.setId(1L);
        mesa.setNumero(7);
        mesa.setCapacidad(capacidad);
        return mesa;
    }

    // --- reservarMesa ---

    @Test
    void reservarMesa_sinSolapamiento_creaLaReserva() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        when(clienteRepository.findByUsuario("marta")).thenReturn(Optional.of(cliente));
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDeCapacidad(4)));
        when(reservaRepository.findByMesaIdAndFechaAndEstado(1L, fecha, "ACTIVA")).thenReturn(List.of());
        when(baseRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva reserva = reservaService.reservarMesa("marta", 1L, fecha, "20:00", "21:00", 3);

        assertThat(reserva.getEstado()).isEqualTo("ACTIVA");
        assertThat(reserva.getCliente()).isEqualTo(cliente);
    }

    @Test
    void reservarMesa_conCantidadMayorQueLaCapacidad_lanzaExcepcion() throws Exception {
        when(clienteRepository.findByUsuario("marta")).thenReturn(Optional.of(new Cliente()));
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDeCapacidad(2)));

        assertThatThrownBy(() -> reservaService.reservarMesa("marta", 1L, fecha, "20:00", "21:00", 5))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("capacidad para 2 personas");

        verify(baseRepository, never()).save(any());
    }

    @Test
    void reservarMesa_conHoraFinAnteriorOIgualAHoraInicio_lanzaExcepcion() throws Exception {
        when(clienteRepository.findByUsuario("marta")).thenReturn(Optional.of(new Cliente()));
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDeCapacidad(4)));

        assertThatThrownBy(() -> reservaService.reservarMesa("marta", 1L, fecha, "21:00", "21:00", 2))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("posterior");
    }

    @Test
    void reservarMesa_conHorarioQueSolapaUnaReservaExistente_lanzaExcepcion() throws Exception {
        Reserva existente = new Reserva();
        existente.setHoraInicio("20:00");
        existente.setHoraFin("21:30");

        when(clienteRepository.findByUsuario("marta")).thenReturn(Optional.of(new Cliente()));
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDeCapacidad(4)));
        when(reservaRepository.findByMesaIdAndFechaAndEstado(1L, fecha, "ACTIVA")).thenReturn(List.of(existente));

        // pide 21:00-22:00, que se solapa con la existente 20:00-21:30
        assertThatThrownBy(() -> reservaService.reservarMesa("marta", 1L, fecha, "21:00", "22:00", 2))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("ya está reservada");

        verify(baseRepository, never()).save(any());
    }

    @Test
    void reservarMesa_conHorarioConsecutivoSinSolapar_laPermite() throws Exception {
        Reserva existente = new Reserva();
        existente.setHoraInicio("20:00");
        existente.setHoraFin("21:00");

        when(clienteRepository.findByUsuario("marta")).thenReturn(Optional.of(new Cliente()));
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDeCapacidad(4)));
        when(reservaRepository.findByMesaIdAndFechaAndEstado(1L, fecha, "ACTIVA")).thenReturn(List.of(existente));
        when(baseRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        // pide 21:00-22:00: arranca justo cuando termina la anterior, no se solapa
        Reserva reserva = reservaService.reservarMesa("marta", 1L, fecha, "21:00", "22:00", 2);

        assertThat(reserva.getHoraInicio()).isEqualTo("21:00");
    }

    @Test
    void reservarMesa_conClienteInexistente_lanzaExcepcion() {
        when(clienteRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.reservarMesa("fantasma", 1L, fecha, "20:00", "21:00", 2))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Cliente no encontrado");

        verifyNoInteractions(mesaRepository);
    }

    // --- cancelarReservacion ---

    @Test
    void cancelarReservacion_elClienteDuenio_laCancela() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setEstado("ACTIVA");
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        reservaService.cancelarReservacion(1L, "marta", false);

        assertThat(reserva.getEstado()).isEqualTo("CANCELADA");
        verify(baseRepository).save(reserva);
    }

    @Test
    void cancelarReservacion_otroClienteQueNoEsDuenio_lanzaExcepcion() {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setEstado("ACTIVA");
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReservacion(1L, "otro_cliente", false))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no es tuya");

        verify(baseRepository, never()).save(any());
    }

    @Test
    void cancelarReservacion_gerente_puedeCancelarAunNoSiendoElDuenio() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setEstado("ACTIVA");
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        reservaService.cancelarReservacion(1L, "gerente1", true);

        assertThat(reserva.getEstado()).isEqualTo("CANCELADA");
    }

    @Test
    void cancelarReservacion_yaCancelada_lanzaExcepcion() {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setEstado("CANCELADA");
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.cancelarReservacion(1L, "marta", false))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("ya estaba cancelada");
    }

    // --- misReservas / obtenerParaUsuario ---

    @Test
    void misReservas_devuelveSoloLasDelClienteIndicado() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        when(reservaRepository.findByClienteUsuario("marta")).thenReturn(List.of(reserva));

        assertThat(reservaService.misReservas("marta")).containsExactly(reserva);
    }

    @Test
    void obtenerParaUsuario_elClienteDuenio_laVe() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThat(reservaService.obtenerParaUsuario(1L, "marta", false)).isEqualTo(reserva);
    }

    @Test
    void obtenerParaUsuario_otroClienteSinSerStaff_lanzaExcepcion() {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThatThrownBy(() -> reservaService.obtenerParaUsuario(1L, "otro_cliente", false))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no es tuya");
    }

    @Test
    void obtenerParaUsuario_staff_puedeVerCualquierReserva() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThat(reservaService.obtenerParaUsuario(1L, "un_mesero", true)).isEqualTo(reserva);
    }
}
