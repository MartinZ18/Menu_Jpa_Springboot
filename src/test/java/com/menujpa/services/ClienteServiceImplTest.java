package com.menujpa.services;

import com.menujpa.entities.Cliente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.PedidoRepository;
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
class ClienteServiceImplTest {

    @Mock
    private BaseRepository<Cliente, Long> baseRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private ClienteServiceImpl clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteServiceImpl(baseRepository);
        ReflectionTestUtils.setField(clienteService, "pedidoRepository", pedidoRepository);
    }

    @Test
    void findAll_devuelveTodosLosClientes() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(cliente));

        assertThat(clienteService.findAll()).containsExactly(cliente);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("jperez");
        when(baseRepository.save(cliente)).thenReturn(cliente);

        assertThat(clienteService.save(cliente)).isEqualTo(cliente);
        verify(baseRepository).save(cliente);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.update(404L, new Cliente()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_conPedidosAsociados_lanzaExcepcionYNoBorra() {
        when(pedidoRepository.existsByClientesId(5L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.delete(5L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("tiene pedidos asociados");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinPedidosYExistente_loBorra() throws Exception {
        when(pedidoRepository.existsByClientesId(6L)).thenReturn(false);
        when(baseRepository.existsById(6L)).thenReturn(true);

        assertThat(clienteService.delete(6L)).isTrue();
        verify(baseRepository).deleteById(6L);
    }

    @Test
    void delete_sinPedidosPeroInexistente_lanzaExcepcion() {
        when(pedidoRepository.existsByClientesId(7L)).thenReturn(false);
        when(baseRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("7");
    }
}
