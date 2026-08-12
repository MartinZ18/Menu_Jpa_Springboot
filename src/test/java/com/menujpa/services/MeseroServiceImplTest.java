package com.menujpa.services;

import com.menujpa.entities.Mesero;
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
class MeseroServiceImplTest {

    @Mock
    private BaseRepository<Mesero, Long> baseRepository;

    private MeseroServiceImpl meseroService;

    @BeforeEach
    void setUp() {
        meseroService = new MeseroServiceImpl(baseRepository);
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
    void delete_conIdExistente_loBorra() throws Exception {
        when(baseRepository.existsById(5L)).thenReturn(true);

        assertThat(meseroService.delete(5L)).isTrue();
        verify(baseRepository).deleteById(5L);
    }

    @Test
    void delete_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.existsById(6L)).thenReturn(false);

        assertThatThrownBy(() -> meseroService.delete(6L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("6");
    }
}
