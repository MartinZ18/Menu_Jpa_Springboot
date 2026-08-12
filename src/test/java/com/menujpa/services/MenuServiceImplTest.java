package com.menujpa.services;

import com.menujpa.entities.Menu;
import com.menujpa.entities.Receta;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.MenuRepository;
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

// Igual que en RecetaServiceImplTest: constructor + campos @Autowired, se arma a mano.
@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private BaseRepository<Menu, Long> baseRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RecetaRepository recetaRepository;

    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuServiceImpl(baseRepository);
        ReflectionTestUtils.setField(menuService, "menuRepository", menuRepository);
        ReflectionTestUtils.setField(menuService, "recetaRepository", recetaRepository);
    }

    @Test
    void findAll_devuelveTodosLosMenus() throws Exception {
        Menu menu = new Menu();
        menu.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(menu));

        List<Menu> resultado = menuService.findAll();

        assertThat(resultado).containsExactly(menu);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Menu menu = new Menu();
        menu.setNombre("Menú del día");
        when(baseRepository.save(menu)).thenReturn(menu);

        Menu resultado = menuService.save(menu);

        assertThat(resultado).isEqualTo(menu);
        verify(baseRepository).save(menu);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.update(404L, new Menu()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");

        verify(baseRepository, never()).save(any());
    }

    @Test
    void agregarReceta_conMenuYRecetaExistentes_laAgregaYGuarda() throws Exception {
        Menu menu = new Menu();
        menu.setId(1L);
        Receta receta = new Receta();
        receta.setId(2L);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(recetaRepository.findById(2L)).thenReturn(Optional.of(receta));

        Menu resultado = menuService.agregarReceta(1L, 2L);

        assertThat(resultado.getRecetas()).containsExactly(receta);
        verify(menuRepository).save(menu);
    }

    @Test
    void agregarReceta_yaIncluidaEnElMenu_noLaDuplicaNiGuardaDeNuevo() throws Exception {
        Menu menu = new Menu();
        menu.setId(1L);
        Receta receta = new Receta();
        receta.setId(2L);
        menu.getRecetas().add(receta);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(recetaRepository.findById(2L)).thenReturn(Optional.of(receta));

        Menu resultado = menuService.agregarReceta(1L, 2L);

        assertThat(resultado.getRecetas()).containsExactly(receta);
        verify(menuRepository, never()).save(any());
    }

    @Test
    void agregarReceta_conMenuInexistente_lanzaExcepcion() {
        when(menuRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.agregarReceta(1L, 2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Menú no encontrado con id: 1");

        verifyNoInteractions(recetaRepository);
    }

    @Test
    void agregarReceta_conRecetaInexistente_lanzaExcepcion() {
        when(menuRepository.findById(1L)).thenReturn(Optional.of(new Menu()));
        when(recetaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.agregarReceta(1L, 2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Receta no encontrada con id: 2");
    }

    @Test
    void quitarReceta_conMenuYRecetaExistentes_laQuitaYGuarda() throws Exception {
        Menu menu = new Menu();
        menu.setId(1L);
        Receta receta = new Receta();
        receta.setId(2L);
        menu.getRecetas().add(receta);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(recetaRepository.findById(2L)).thenReturn(Optional.of(receta));

        Menu resultado = menuService.quitarReceta(1L, 2L);

        assertThat(resultado.getRecetas()).isEmpty();
        verify(menuRepository).save(menu);
    }

    @Test
    void quitarReceta_conMenuInexistente_lanzaExcepcion() {
        when(menuRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.quitarReceta(1L, 2L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Menú no encontrado con id: 1");
    }
}
