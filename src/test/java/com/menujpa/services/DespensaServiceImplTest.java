package com.menujpa.services;

import com.menujpa.entities.Despensa;
import com.menujpa.entities.Ingrediente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.DespensaRepository;
import com.menujpa.repositories.IngredienteRepository;
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
class DespensaServiceImplTest {

    @Mock
    private BaseRepository<Despensa, Long> baseRepository;

    @Mock
    private DespensaRepository despensaRepository;

    @Mock
    private IngredienteRepository ingredienteRepository;

    private DespensaServiceImpl despensaService;

    @BeforeEach
    void setUp() {
        despensaService = new DespensaServiceImpl(baseRepository);
        ReflectionTestUtils.setField(despensaService, "despensaRepository", despensaRepository);
        ReflectionTestUtils.setField(despensaService, "ingredienteRepository", ingredienteRepository);
    }

    @Test
    void findAll_devuelveTodasLasDespensas() throws Exception {
        Despensa despensa = new Despensa();
        despensa.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(despensa));

        assertThat(despensaService.findAll()).containsExactly(despensa);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despensaService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Despensa despensa = new Despensa();
        when(baseRepository.save(despensa)).thenReturn(despensa);

        assertThat(despensaService.save(despensa)).isEqualTo(despensa);
        verify(baseRepository).save(despensa);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despensaService.update(404L, new Despensa()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void agregarIngrediente_conDespensaEIngredienteExistentes_loAgregaYGuarda() throws Exception {
        Despensa despensa = new Despensa();
        despensa.setId(1L);
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(2L);

        when(despensaRepository.findById(1L)).thenReturn(Optional.of(despensa));
        when(ingredienteRepository.findById(2L)).thenReturn(Optional.of(ingrediente));

        Despensa resultado = despensaService.agregarIngrediente(1L, 2L);

        assertThat(resultado.getIngredientes()).containsExactly(ingrediente);
        verify(despensaRepository).save(despensa);
    }

    @Test
    void agregarIngrediente_yaIncluidoEnLaDespensa_noLoDuplicaNiGuardaDeNuevo() throws Exception {
        Despensa despensa = new Despensa();
        despensa.setId(1L);
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(2L);
        despensa.getIngredientes().add(ingrediente);

        when(despensaRepository.findById(1L)).thenReturn(Optional.of(despensa));
        when(ingredienteRepository.findById(2L)).thenReturn(Optional.of(ingrediente));

        Despensa resultado = despensaService.agregarIngrediente(1L, 2L);

        assertThat(resultado.getIngredientes()).containsExactly(ingrediente);
        verify(despensaRepository, never()).save(any());
    }

    @Test
    void agregarIngrediente_conDespensaInexistente_lanzaExcepcion() {
        when(despensaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despensaService.agregarIngrediente(1L, 2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Despensa no encontrada con id: 1");

        verifyNoInteractions(ingredienteRepository);
    }

    @Test
    void agregarIngrediente_conIngredienteInexistente_lanzaExcepcion() {
        when(despensaRepository.findById(1L)).thenReturn(Optional.of(new Despensa()));
        when(ingredienteRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despensaService.agregarIngrediente(1L, 2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Ingrediente no encontrado con id: 2");
    }

    @Test
    void quitarIngrediente_conDespensaEIngredienteExistentes_loQuitaYGuarda() throws Exception {
        Despensa despensa = new Despensa();
        despensa.setId(1L);
        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setId(2L);
        despensa.getIngredientes().add(ingrediente);

        when(despensaRepository.findById(1L)).thenReturn(Optional.of(despensa));
        when(ingredienteRepository.findById(2L)).thenReturn(Optional.of(ingrediente));

        Despensa resultado = despensaService.quitarIngrediente(1L, 2L);

        assertThat(resultado.getIngredientes()).isEmpty();
        verify(despensaRepository).save(despensa);
    }

    @Test
    void quitarIngrediente_conDespensaInexistente_lanzaExcepcion() {
        when(despensaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> despensaService.quitarIngrediente(1L, 2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Despensa no encontrada con id: 1");
    }
}
