package com.menujpa.services;

import com.menujpa.entities.Chef;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ChefRepository;
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
class ChefServiceImplTest {

    @Mock
    private BaseRepository<Chef, Long> baseRepository;

    @Mock
    private ChefRepository chefRepository;

    @Mock
    private RecetaRepository recetaRepository;

    private ChefServiceImpl chefService;

    @BeforeEach
    void setUp() {
        chefService = new ChefServiceImpl(baseRepository);
        ReflectionTestUtils.setField(chefService, "chefRepository", chefRepository);
        ReflectionTestUtils.setField(chefService, "recetaRepository", recetaRepository);
    }

    @Test
    void findAll_devuelveTodosLosChefs() throws Exception {
        Chef chef = new Chef();
        chef.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(chef));

        assertThat(chefService.findAll()).containsExactly(chef);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chefService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Chef chef = new Chef();
        when(baseRepository.save(chef)).thenReturn(chef);

        assertThat(chefService.save(chef)).isEqualTo(chef);
        verify(baseRepository).save(chef);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chefService.update(404L, new Chef()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void delete_conIdInexistente_lanzaExcepcion() {
        when(chefRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chefService.delete(1L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Chef no encontrado con id: 1");
    }

    @Test
    void delete_conUnaRecetaAsignada_lanzaExcepcionEnSingular() {
        Chef chef = new Chef();
        chef.setId(2L);
        chef.setNombre("Ana");
        chef.setApellido("Gómez");
        when(chefRepository.findById(2L)).thenReturn(Optional.of(chef));
        when(recetaRepository.countByNombreChefTexto("Gómez, Ana")).thenReturn(1L);

        assertThatThrownBy(() -> chefService.delete(2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("1 receta asignada");

        verify(chefRepository, never()).deleteById(any());
    }

    @Test
    void delete_conVariasRecetasAsignadas_lanzaExcepcionEnPlural() {
        Chef chef = new Chef();
        chef.setId(3L);
        chef.setNombre("Luis");
        chef.setApellido("Pérez");
        when(chefRepository.findById(3L)).thenReturn(Optional.of(chef));
        when(recetaRepository.countByNombreChefTexto("Pérez, Luis")).thenReturn(2L);

        assertThatThrownBy(() -> chefService.delete(3L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("2 recetas asignadas");

        verify(chefRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinRecetasAsignadas_loBorra() throws Exception {
        Chef chef = new Chef();
        chef.setId(4L);
        chef.setNombre("Rosa");
        chef.setApellido("Díaz");
        when(chefRepository.findById(4L)).thenReturn(Optional.of(chef));
        when(recetaRepository.countByNombreChefTexto("Díaz, Rosa")).thenReturn(0L);

        boolean resultado = chefService.delete(4L);

        assertThat(resultado).isTrue();
        verify(chefRepository).deleteById(4L);
    }
}
