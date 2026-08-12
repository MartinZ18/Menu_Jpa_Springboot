package com.menujpa.services;

import com.menujpa.entities.Receta;
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

// Mezcla inyección por constructor y por campo, así que se arma a mano en vez de con @InjectMocks.
@ExtendWith(MockitoExtension.class)
class RecetaServiceImplTest {

    @Mock
    private BaseRepository<Receta, Long> baseRepository;

    @Mock
    private MenuRepository menuRepository;

    private RecetaServiceImpl recetaService;

    @BeforeEach
    void setUp() {
        recetaService = new RecetaServiceImpl(baseRepository);
        ReflectionTestUtils.setField(recetaService, "menuRepository", menuRepository);
    }

    @Test
    void findAll_devuelveTodasLasRecetas() throws Exception {
        Receta receta = new Receta();
        receta.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(receta));

        List<Receta> resultado = recetaService.findAll();

        assertThat(resultado).containsExactly(receta);
    }

    @Test
    void findById_conIdExistente_devuelveLaReceta() throws Exception {
        Receta receta = new Receta();
        receta.setId(5L);
        when(baseRepository.findById(5L)).thenReturn(Optional.of(receta));

        Receta resultado = recetaService.findById(5L);

        assertThat(resultado).isEqualTo(receta);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Receta receta = new Receta();
        receta.setNombreReceta("Milanesa napolitana");
        when(baseRepository.save(receta)).thenReturn(receta);

        Receta resultado = recetaService.save(receta);

        assertThat(resultado).isEqualTo(receta);
        verify(baseRepository).save(receta);
    }

    @Test
    void update_conIdExistente_guardaLosCambios() throws Exception {
        Receta existente = new Receta();
        existente.setId(3L);
        Receta cambios = new Receta();
        cambios.setId(3L);
        cambios.setNombreReceta("Receta actualizada");

        when(baseRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(baseRepository.save(cambios)).thenReturn(cambios);

        Receta resultado = recetaService.update(3L, cambios);

        assertThat(resultado.getNombreReceta()).isEqualTo("Receta actualizada");
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.update(404L, new Receta()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");

        verify(baseRepository, never()).save(any());
    }

    @Test
    void delete_conRecetaIncluidaEnUnMenu_lanzaExcepcionYNoBorra() {
        when(menuRepository.existsByRecetasId(7L)).thenReturn(true);

        assertThatThrownBy(() -> recetaService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("incluida en uno o más menús");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_conRecetaSinMenuYExistente_laBorra() throws Exception {
        when(menuRepository.existsByRecetasId(8L)).thenReturn(false);
        when(baseRepository.existsById(8L)).thenReturn(true);

        boolean resultado = recetaService.delete(8L);

        assertThat(resultado).isTrue();
        verify(baseRepository).deleteById(8L);
    }

    @Test
    void delete_conRecetaSinMenuPeroInexistente_lanzaExcepcion() {
        when(menuRepository.existsByRecetasId(9L)).thenReturn(false);
        when(baseRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> recetaService.delete(9L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("9");

        verify(baseRepository, never()).deleteById(any());
    }
}
