package com.menujpa.services;

import com.menujpa.entities.Cliente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private BaseRepository<Cliente, Long> baseRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private ClienteServiceImpl clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteServiceImpl(baseRepository);
        ReflectionTestUtils.setField(clienteService, "pedidoRepository", pedidoRepository);
    }

    @Test
    void findAll_devuelveTodosLosClientes() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(baseRepository.findAll()).thenReturn(List.of(cliente));

        assertThat(clienteService.findAll()).containsExactly(cliente);
    }

    @Test
    void findById_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(99L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("99");
    }

    @Test
    void save_delegaEnElRepositorio() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("jperez");
        when(baseRepository.save(cliente)).thenReturn(cliente);

        assertThat(clienteService.save(cliente)).isEqualTo(cliente);
        verify(baseRepository).save(cliente);
    }

    @Test
    void update_conIdInexistente_lanzaExcepcion() {
        when(baseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.update(404L, new Cliente()))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("404");
    }

    @Test
    void save_conContraseniaEnTextoPlano_laHasheaAntesDeGuardar() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setUsuario("jperez");
        cliente.setContrasenia("miClave123");
        when(baseRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.save(cliente);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(baseRepository).save(captor.capture());
        String contraseniaGuardada = captor.getValue().getContrasenia();

        assertThat(contraseniaGuardada).isNotEqualTo("miClave123");
        assertThat(new BCryptPasswordEncoder().matches("miClave123", contraseniaGuardada)).isTrue();
    }

    @Test
    void update_ignoraLaContraseniaEnviadaYPreservaLaExistente() throws Exception {
        Cliente existente = new Cliente();
        existente.setId(1L);
        existente.setContrasenia("$2a$10$hashOriginalYaGuardadoEnLaBase1234567890123456789012");

        Cliente cambios = new Cliente();
        cambios.setId(1L);
        cambios.setNombre("Nombre actualizado");
        // El cliente real nunca puede enviar la contrasenia (@JsonIgnore), pero probamos
        // igual que aunque llegara algo distinto, el update la ignora por completo.
        cambios.setContrasenia("lo-que-sea-que-llegue-se-ignora");

        when(baseRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(baseRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        clienteService.update(1L, cambios);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(baseRepository).save(captor.capture());
        assertThat(captor.getValue().getContrasenia()).isEqualTo(existente.getContrasenia());
    }

    @Test
    void delete_conPedidosAsociados_lanzaExcepcionYNoBorra() {
        when(pedidoRepository.existsByClientesId(5L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.delete(5L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("tiene pedidos asociados");

        verify(baseRepository, never()).deleteById(any());
    }

    @Test
    void delete_sinPedidosYExistente_loBorra() throws Exception {
        when(pedidoRepository.existsByClientesId(6L)).thenReturn(false);
        when(baseRepository.existsById(6L)).thenReturn(true);

        assertThat(clienteService.delete(6L)).isTrue();
        verify(baseRepository).deleteById(6L);
    }

    @Test
    void delete_sinPedidosPeroInexistente_lanzaExcepcion() {
        when(pedidoRepository.existsByClientesId(7L)).thenReturn(false);
        when(baseRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> clienteService.delete(7L))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("7");
    }
}
