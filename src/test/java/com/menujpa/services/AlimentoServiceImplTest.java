package com.menujpa.services;

import com.menujpa.entities.Alimento;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.PedidoRepository;
import com.menujpa.repositories.RecetaRepository;
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
class AlimentoServiceImplTest {

    @Mock
    private BaseRepository<Alimento, Long> baseRepository;

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private AlimentoServiceImpl alimentoService;

    @BeforeEach
    void setUp() {
        alimentoService = new AlimentoServiceImpl(baseRepository);
        ReflectionTestUtils.setField(alimentoService, "recetaRepository", recetaRepository);
        ReflectionTestUtils.setField(alimentoService, "pedidoRepository", pedidoRepository);
    }

    @Test
    void findAll_devuelveTodosLosAlimentos() throws Exception {
        Alimento alimento = new Alimento();
        alimento.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(alimento));

        assertThat(alimentoService.findAll()).containsExactly(alimento);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alimentoService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Alimento alimento = new Alimento();
        when(baseRepository.save(alimento)).thenReturn(alimento);

        assertThat(alimentoService.save(alimento)).isEqualTo(alimento);
        verify(baseRepository).save(alimento);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alimentoService.update(404L, new Alimento()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_usadoEnUnaReceta_lanzaExcepcionYNoBorra() {
        when(recetaRepository.existsByAlimentosSeleccionadosId(5L)).thenReturn(true);

        assertThatThrownBy(() -> alimentoService.delete(5L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("usado en una o más recetas");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_conPedidosAsociados_lanzaExcepcionYNoBorra() {
        when(recetaRepository.existsByAlimentosSeleccionadosId(8L)).thenReturn(false);
        when(pedidoRepository.existsByAlimentosAdquiridosId(8L)).thenReturn(true);

        assertThatThrownBy(() -> alimentoService.delete(8L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("tiene pedidos asociados");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinUsoYExistente_loBorra() throws Exception {
        when(recetaRepository.existsByAlimentosSeleccionadosId(6L)).thenReturn(false);
        when(pedidoRepository.existsByAlimentosAdquiridosId(6L)).thenReturn(false);
        when(baseRepository.existsById(6L)).thenReturn(true);

        boolean resultado = alimentoService.delete(6L);

        assertThat(resultado).isTrue();
        verify(baseRepository).deleteById(6L);
    }

    @Test
    void delete_sinUsoPeroInexistente_lanzaExcepcion() {
        when(recetaRepository.existsByAlimentosSeleccionadosId(7L)).thenReturn(false);
        when(pedidoRepository.existsByAlimentosAdquiridosId(7L)).thenReturn(false);
        when(baseRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> alimentoService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("7");
    }
}
