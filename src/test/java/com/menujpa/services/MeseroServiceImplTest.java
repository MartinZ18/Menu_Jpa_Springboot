package com.menujpa.services;

import com.menujpa.entities.Mesero;
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
class MeseroServiceImplTest {

    @Mock
    private BaseRepository<Mesero, Long> baseRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private MeseroServiceImpl meseroService;

    @BeforeEach
    void setUp() {
        meseroService = new MeseroServiceImpl(baseRepository);
        ReflectionTestUtils.setField(meseroService, "pedidoRepository", pedidoRepository);
    }

    @Test
    void findAll_devuelveTodosLosMeseros() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(mesero));

        assertThat(meseroService.findAll()).containsExactly(mesero);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meseroService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setNombre("Juan");
        when(baseRepository.save(mesero)).thenReturn(mesero);

        assertThat(meseroService.save(mesero)).isEqualTo(mesero);
        verify(baseRepository).save(mesero);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meseroService.update(404L, new Mesero()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_conPedidosAsociados_lanzaExcepcionYNoBorra() {
        when(pedidoRepository.existsByMeserosId(5L)).thenReturn(true);

        assertThatThrownBy(() -> meseroService.delete(5L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("tiene pedidos asociados");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinPedidosYExistente_loBorra() throws Exception {
        when(pedidoRepository.existsByMeserosId(6L)).thenReturn(false);
        when(baseRepository.existsById(6L)).thenReturn(true);

        assertThat(meseroService.delete(6L)).isTrue();
        verify(baseRepository).deleteById(6L);
    }

    @Test
    void delete_sinPedidosPeroInexistente_lanzaExcepcion() {
        when(pedidoRepository.existsByMeserosId(7L)).thenReturn(false);
        when(baseRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> meseroService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("7");
    }

    @Test
    void registrarEntrada_conIdExistente_completaLaHoraDeIngreso() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setId(8L);
        when(baseRepository.findById(8L)).thenReturn(Optional.of(mesero));
        when(baseRepository.save(any(Mesero.class))).thenAnswer(inv -> inv.getArgument(0));

        Mesero resultado = meseroService.registrarEntrada(8L);

        assertThat(resultado.getHoraIngreso()).matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    @Test
    void registrarSalida_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meseroService.registrarSalida(999L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("999");
    }
}
