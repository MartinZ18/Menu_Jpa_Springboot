package com.menujpa.services;

import com.menujpa.entities.Pedido;
import com.menujpa.repositories.BaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private BaseRepository<Pedido, Long> baseRepository;

    private PedidoServiceImpl pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoServiceImpl(baseRepository);
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
}
