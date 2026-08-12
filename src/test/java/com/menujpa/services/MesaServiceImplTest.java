package com.menujpa.services;

import com.menujpa.entities.Mesa;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ReservaRepository;
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
class MesaServiceImplTest {

    @Mock
    private BaseRepository<Mesa, Long> baseRepository;

    @Mock
    private ReservaRepository reservaRepository;

    private MesaServiceImpl mesaService;

    @BeforeEach
    void setUp() {
        mesaService = new MesaServiceImpl(baseRepository);
        ReflectionTestUtils.setField(mesaService, "reservaRepository", reservaRepository);
    }

    @Test
    void findAll_devuelveTodasLasMesas() throws Exception {
        Mesa mesa = new Mesa();
        mesa.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(mesa));

        assertThat(mesaService.findAll()).containsExactly(mesa);
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Mesa mesa = new Mesa();
        mesa.setNumero(5);
        mesa.setCapacidad(4);
        when(baseRepository.save(mesa)).thenReturn(mesa);

        assertThat(mesaService.save(mesa)).isEqualTo(mesa);
        verify(baseRepository).save(mesa);
    }

    @Test
    void delete_conReservasActivas_lanzaExcepcionYNoBorra() {
        when(reservaRepository.existsByMesaIdAndEstado(1L, "ACTIVA")).thenReturn(true);

        assertThatThrownBy(() -> mesaService.delete(1L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("tiene reservas activas");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinReservasActivasYExistente_laBorra() throws Exception {
        when(reservaRepository.existsByMesaIdAndEstado(2L, "ACTIVA")).thenReturn(false);
        when(baseRepository.existsById(2L)).thenReturn(true);

        assertThat(mesaService.delete(2L)).isTrue();
        verify(baseRepository).deleteById(2L);
    }
}
