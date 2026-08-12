package com.menujpa.security;

import com.menujpa.entities.Chef;
import com.menujpa.entities.Cliente;
import com.menujpa.entities.Gerente;
import com.menujpa.entities.Mesero;
import com.menujpa.repositories.ChefRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.GerenteRepository;
import com.menujpa.repositories.MeseroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiRoleUserDetailsServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private ChefRepository chefRepository;
    @Mock private MeseroRepository meseroRepository;
    @Mock private GerenteRepository gerenteRepository;

    private MultiRoleUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new MultiRoleUserDetailsService(clienteRepository, chefRepository, meseroRepository, gerenteRepository);
    }

    // --- loadUserByUsername ---

    @Test
    void loadUserByUsername_clienteExistente_devuelveUserDetailsConRolCliente() {
        Cliente cliente = new Cliente();
        cliente.setUsuario("marta");
        cliente.setContrasenia("hash");
        when(clienteRepository.findByUsuario("marta")).thenReturn(Optional.of(cliente));

        UserDetails resultado = service.loadUserByUsername("marta");

        assertThat(resultado.getUsername()).isEqualTo("marta");
        assertThat(resultado.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_CLIENTE");
    }

    @Test
    void loadUserByUsername_noEncontradoEnNingunaTabla_lanzaExcepcion() {
        when(clienteRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());
        when(chefRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());
        when(meseroRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());
        when(gerenteRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("fantasma"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    // --- existeUsuario ---

    @Test
    void existeUsuario_soloEnTablaChef_devuelveTrue() {
        Chef chef = new Chef();
        chef.setUsuario("juan");
        when(clienteRepository.findByUsuario("juan")).thenReturn(Optional.empty());
        when(chefRepository.findByUsuario("juan")).thenReturn(Optional.of(chef));

        assertThat(service.existeUsuario("juan")).isTrue();
    }

    @Test
    void existeUsuario_enNingunaTabla_devuelveFalse() {
        when(clienteRepository.findByUsuario("nuevo")).thenReturn(Optional.empty());
        when(chefRepository.findByUsuario("nuevo")).thenReturn(Optional.empty());
        when(meseroRepository.findByUsuario("nuevo")).thenReturn(Optional.empty());
        when(gerenteRepository.findByUsuario("nuevo")).thenReturn(Optional.empty());

        assertThat(service.existeUsuario("nuevo")).isFalse();
    }

    // --- cambiarContrasenia ---

    @Test
    void cambiarContrasenia_conActualCorrecta_actualizaYHashea() throws Exception {
        Mesero mesero = new Mesero();
        mesero.setUsuario("pedro");
        mesero.setContrasenia(PasswordHasher.hashIfNeeded("actual123"));
        when(clienteRepository.findByUsuario("pedro")).thenReturn(Optional.empty());
        when(chefRepository.findByUsuario("pedro")).thenReturn(Optional.empty());
        when(meseroRepository.findByUsuario("pedro")).thenReturn(Optional.of(mesero));

        service.cambiarContrasenia("pedro", "actual123", "nuevaClave456");

        verify(meseroRepository).save(mesero);
        assertThat(PasswordHasher.matches("nuevaClave456", mesero.getContrasenia())).isTrue();
    }

    @Test
    void cambiarContrasenia_conActualIncorrecta_lanzaExcepcionYNoGuarda() {
        Gerente gerente = new Gerente();
        gerente.setUsuario("ana");
        gerente.setContrasenia(PasswordHasher.hashIfNeeded("actual123"));
        when(clienteRepository.findByUsuario("ana")).thenReturn(Optional.empty());
        when(chefRepository.findByUsuario("ana")).thenReturn(Optional.empty());
        when(meseroRepository.findByUsuario("ana")).thenReturn(Optional.empty());
        when(gerenteRepository.findByUsuario("ana")).thenReturn(Optional.of(gerente));

        assertThatThrownBy(() -> service.cambiarContrasenia("ana", "incorrecta", "nuevaClave456"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no es correcta");

        verify(gerenteRepository, never()).save(any());
    }

    @Test
    void cambiarContrasenia_usuarioInexistente_lanzaExcepcion() {
        when(clienteRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());
        when(chefRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());
        when(meseroRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());
        when(gerenteRepository.findByUsuario("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarContrasenia("fantasma", "x", "nuevaClave456"))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("no encontrado");
    }
}
