package com.menujpa.services;

import com.menujpa.entities.Gerente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.MenuRepository;
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
class GerenteServiceImplTest {

    @Mock
    private BaseRepository<Gerente, Long> baseRepository;

    @Mock
    private MenuRepository menuRepository;

    private GerenteServiceImpl gerenteService;

    @BeforeEach
    void setUp() {
        gerenteService = new GerenteServiceImpl(baseRepository);
        ReflectionTestUtils.setField(gerenteService, "menuRepository", menuRepository);
    }

    @Test
    void findAll_devuelveTodosLosGerentes() throws Exception {
        Gerente gerente = new Gerente();
        gerente.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(gerente));

        assertThat(gerenteService.findAll()).containsExactly(gerente);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gerenteService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Gerente gerente = new Gerente();
        when(baseRepository.save(gerente)).thenReturn(gerente);

        assertThat(gerenteService.save(gerente)).isEqualTo(gerente);
        verify(baseRepository).save(gerente);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gerenteService.update(404L, new Gerente()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_asignadoAUnMenu_lanzaExcepcionYNoBorra() {
        when(menuRepository.existsByGerenteId(5L)).thenReturn(true);

        assertThatThrownBy(() -> gerenteService.delete(5L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("asignado a uno o más menús");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinAsignacionYExistente_loBorra() throws Exception {
        when(menuRepository.existsByGerenteId(6L)).thenReturn(false);
        when(baseRepository.existsById(6L)).thenReturn(true);

        boolean resultado = gerenteService.delete(6L);

        assertThat(resultado).isTrue();
        verify(baseRepository).deleteById(6L);
    }

    @Test
    void delete_sinAsignacionPeroInexistente_lanzaExcepcion() {
        when(menuRepository.existsByGerenteId(7L)).thenReturn(false);
        when(baseRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> gerenteService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("7");
    }
}
