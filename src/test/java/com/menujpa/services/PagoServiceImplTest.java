package com.menujpa.services;

import com.menujpa.entities.Chef;
import com.menujpa.entities.Mesero;
import com.menujpa.entities.Pago;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ChefRepository;
import com.menujpa.repositories.MeseroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private BaseRepository<Pago, Long> baseRepository;

    @Mock
    private ChefRepository chefRepository;

    @Mock
    private MeseroRepository meseroRepository;

    private PagoServiceImpl pagoService;

    @BeforeEach
    void setUp() {
        pagoService = new PagoServiceImpl(baseRepository);
        ReflectionTestUtils.setField(pagoService, "chefRepository", chefRepository);
        ReflectionTestUtils.setField(pagoService, "meseroRepository", meseroRepository);
    }

    @Test
    void generarPago_paraChefConSalario_generaElPagoPorElMontoDelSalario() throws Exception {
        Chef chef = new Chef();
        chef.setId(1L);
        chef.setNombre("Ana");
        chef.setApellido("Gómez");
        chef.setSalario(850000.0);
        when(chefRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(baseRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago pago = pagoService.generarPago(1L, "CHEF");

        assertThat(pago.getMonto()).isEqualTo(850000.0);
        assertThat(pago.getEmpleadoNombre()).isEqualTo("Gómez, Ana");
        assertThat(pago.getEmpleadoRol()).isEqualTo("CHEF");
        assertThat(pago.getFechaPago()).isNotNull();
    }

    @Test
    void generarPago_paraMeseroConSalario_generaElPago() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setId(2L);
        mesero.setNombre("Luis");
        mesero.setApellido("Pérez");
        mesero.setSalario(600000.0);
        when(meseroRepository.findById(2L)).thenReturn(Optional.of(mesero));
        when(baseRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        Pago pago = pagoService.generarPago(2L, "mesero");

        assertThat(pago.getMonto()).isEqualTo(600000.0);
        assertThat(pago.getEmpleadoRol()).isEqualTo("MESERO");
    }

    @Test
    void generarPago_conRolInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> pagoService.generarPago(1L, "GERENTE"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Rol de empleado invalido");

        verifyNoInteractions(baseRepository);
    }

    @Test
    void generarPago_conChefInexistente_lanzaExcepcion() {
        when(chefRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.generarPago(99L, "CHEF"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("Chef no encontrado con id: 99");
    }

    @Test
    void generarPago_conSalarioNulo_lanzaExcepcion() {
        Chef chef = new Chef();
        chef.setId(3L);
        chef.setSalario(null);
        when(chefRepository.findById(3L)).thenReturn(Optional.of(chef));

        assertThatThrownBy(() -> pagoService.generarPago(3L, "CHEF"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no tiene un salario configurado");

        verify(baseRepository, never()).save(any());
    }
}
