package com.menujpa.services;

import com.menujpa.entities.Ingrediente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.DespensaRepository;
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
class IngredienteServiceImplTest {

    @Mock
    private BaseRepository<Ingrediente, Long> baseRepository;

    @Mock
    private DespensaRepository despensaRepository;

    private IngredienteServiceImpl ingredienteService;

    @BeforeEach
    void setUp() {
        ingredienteService = new IngredienteServiceImpl(baseRepository);
        ReflectionTestUtils.setField(ingredienteService, "despensaRepository", despensaRepository);
    }

    @Test
    void findAll_devuelveTodosLosIngredientes() throws Exception {
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(ingrediente));

        assertThat(ingredienteService.findAll()).containsExactly(ingrediente);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setDescripcion("Harina 000");
        when(baseRepository.save(ingrediente)).thenReturn(ingrediente);

        assertThat(ingredienteService.save(ingrediente)).isEqualTo(ingrediente);
        verify(baseRepository).save(ingrediente);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredienteService.update(404L, new Ingrediente()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_incluidoEnUnaDespensa_lanzaExcepcionYNoBorra() {
        when(despensaRepository.existsByIngredientesId(5L)).thenReturn(true);

        assertThatThrownBy(() -> ingredienteService.delete(5L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("incluido en una o más despensas");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinDespensaYExistente_loBorra() throws Exception {
        when(despensaRepository.existsByIngredientesId(6L)).thenReturn(false);
        when(baseRepository.existsById(6L)).thenReturn(true);

        assertThat(ingredienteService.delete(6L)).isTrue();
        verify(baseRepository).deleteById(6L);
    }

    @Test
    void delete_sinDespensaPeroInexistente_lanzaExcepcion() {
        when(despensaRepository.existsByIngredientesId(7L)).thenReturn(false);
        when(baseRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> ingredienteService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("7");
    }
}
