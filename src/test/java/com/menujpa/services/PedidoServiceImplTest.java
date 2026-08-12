package com.menujpa.services;

import com.menujpa.entities.Alimento;
import com.menujpa.entities.Cliente;
import com.menujpa.entities.Mesero;
import com.menujpa.entities.Pedido;
import com.menujpa.repositories.AlimentoRepository;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.MeseroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private BaseRepository<Pedido, Long> baseRepository;

    @Mock
    private MeseroRepository meseroRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AlimentoRepository alimentoRepository;

    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoServiceImpl(baseRepository);
        ReflectionTestUtils.setField(pedidoService, "meseroRepository", meseroRepository);
        ReflectionTestUtils.setField(pedidoService, "clienteRepository", clienteRepository);
        ReflectionTestUtils.setField(pedidoService, "alimentoRepository", alimentoRepository);
    }

    @Test
    void findAll_devuelveTodosLosPedidos() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(pedido));

        assertThat(pedidoService.findAll()).containsExactly(pedido);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Pedido pedido = new Pedido();
        when(baseRepository.save(pedido)).thenReturn(pedido);

        assertThat(pedidoService.save(pedido)).isEqualTo(pedido);
        verify(baseRepository).save(pedido);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.update(404L, new Pedido()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_conIdExistente_loBorra() throws Exception {
        when(baseRepository.existsById(5L)).thenReturn(true);

        assertThat(pedidoService.delete(5L)).isTrue();
        verify(baseRepository).deleteById(5L);
    }

    @Test
    void delete_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.existsById(6L)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.delete(6L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("6");
    }

    // --- tomarPedido ---

    @Test
    void tomarPedido_calculaElPrecioTotalSumandoLosAlimentos() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setId(1L);
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        Alimento plato = new Alimento();
        plato.setId(3L);
        plato.setPrecio(1500.0);
        Alimento bebida = new Alimento();
        bebida.setId(4L);
        bebida.setPrecio(500.0);

        when(meseroRepository.findByUsuario("mesero1")).thenReturn(Optional.of(mesero));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(alimentoRepository.findById(3L)).thenReturn(Optional.of(plato));
        when(alimentoRepository.findById(4L)).thenReturn(Optional.of(bebida));
        when(baseRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.tomarPedido("mesero1", List.of(2L), List.of(3L, 4L));

        assertThat(resultado.getPrecioTotalPedido()).isEqualTo(2000.0);
        assertThat(resultado.getMeseros()).containsExactly(mesero);
        assertThat(resultado.getClientes()).containsExactly(cliente);
        assertThat(resultado.getEstado()).isFalse();
    }

    @Test
    void tomarPedido_sumaElPrecioDeAlimentosRepetidos_comoCantidad() throws Exception {
        Mesero mesero = new Mesero();
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        Alimento bebida = new Alimento();
        bebida.setId(4L);
        bebida.setPrecio(500.0);

        when(meseroRepository.findByUsuario("mesero1")).thenReturn(Optional.of(mesero));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(alimentoRepository.findById(4L)).thenReturn(Optional.of(bebida));
        when(baseRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.tomarPedido("mesero1", List.of(2L), List.of(4L, 4L, 4L));

        assertThat(resultado.getPrecioTotalPedido()).isEqualTo(1500.0);
        assertThat(resultado.getAlimentosAdquiridos()).hasSize(3);
    }

    @Test
    void tomarPedido_conMeseroInexistente_lanzaExcepcion() {
        when(meseroRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.tomarPedido("fantasma", List.of(1L), List.of(1L)))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Mesero no encontrado");

        verifyNoInteractions(baseRepository);
    }

    @Test
    void tomarPedido_conClienteInexistente_lanzaExcepcion() {
        when(meseroRepository.findByUsuario("mesero1")).thenReturn(Optional.of(new Mesero()));
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.tomarPedido("mesero1", List.of(99L), List.of(1L)))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Cliente no encontrado con id: 99");
    }

    // --- modificarPedido ---

    @Test
    void modificarPedido_recalculaElPrecio() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        Alimento nuevoAlimento = new Alimento();
        nuevoAlimento.setId(5L);
        nuevoAlimento.setPrecio(900.0);

        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(alimentoRepository.findById(5L)).thenReturn(Optional.of(nuevoAlimento));
        when(baseRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.modificarPedido(1L, List.of(2L), List.of(5L), "gerente1", true);

        assertThat(resultado.getPrecioTotalPedido()).isEqualTo(900.0);
    }

    @Test
    void modificarPedido_yaEntregado_lanzaExcepcion() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(true);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.modificarPedido(1L, List.of(2L), List.of(5L), "gerente1", true))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("ya entregado");

        verify(baseRepository, never()).save(any());
    }

    @Test
    void modificarPedido_elMeseroQueLoTomo_puedeModificarlo() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setUsuario("mesero1");
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        pedido.getMeseros().add(mesero);
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        Alimento alimento = new Alimento();
        alimento.setId(5L);
        alimento.setPrecio(900.0);

        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(alimentoRepository.findById(5L)).thenReturn(Optional.of(alimento));
        when(baseRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.modificarPedido(1L, List.of(2L), List.of(5L), "mesero1", false);

        assertThat(resultado.getPrecioTotalPedido()).isEqualTo(900.0);
    }

    @Test
    void modificarPedido_otroMeseroQueNoLoTomo_lanzaExcepcionYNoGuarda() {
        Mesero meseroQueLoTomo = new Mesero();
        meseroQueLoTomo.setUsuario("mesero1");
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        pedido.getMeseros().add(meseroQueLoTomo);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.modificarPedido(1L, List.of(2L), List.of(5L), "otro_mesero", false))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no tomaste vos");

        verify(baseRepository, never()).save(any());
        verifyNoInteractions(clienteRepository, alimentoRepository);
    }

    // --- entregarPedido ---

    @Test
    void entregarPedido_marcaElEstadoComoEntregado() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(baseRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.entregarPedido(1L, "gerente1", true);

        assertThat(resultado.getEstado()).isTrue();
    }

    @Test
    void entregarPedido_yaEntregado_lanzaExcepcion() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(true);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.entregarPedido(1L, "gerente1", true))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("ya fue entregado");
    }

    @Test
    void entregarPedido_otroMeseroQueNoLoTomo_lanzaExcepcion() {
        Mesero meseroQueLoTomo = new Mesero();
        meseroQueLoTomo.setUsuario("mesero1");
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        pedido.getMeseros().add(meseroQueLoTomo);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.entregarPedido(1L, "otro_mesero", false))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no tomaste vos");

        verify(baseRepository, never()).save(any());
    }

    // --- cancelarPedido ---

    @Test
    void cancelarPedido_noEntregado_loBorra() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.cancelarPedido(1L, "gerente1", true);

        verify(baseRepository).deleteById(1L);
    }

    @Test
    void cancelarPedido_yaEntregado_lanzaExcepcionYNoBorra() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(true);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L, "gerente1", true))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("No se puede cancelar");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void cancelarPedido_otroMeseroQueNoLoTomo_lanzaExcepcionYNoBorra() {
        Mesero meseroQueLoTomo = new Mesero();
        meseroQueLoTomo.setUsuario("mesero1");
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(false);
        pedido.getMeseros().add(meseroQueLoTomo);
        when(baseRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L, "otro_mesero", false))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no tomaste vos");

        verify(baseRepository, never()).deleteById(any());
    }
}
