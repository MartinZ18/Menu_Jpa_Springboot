package com.menujpa.security;

import com.menujpa.entities.Chef;
import com.menujpa.entities.Cliente;
import com.menujpa.entities.Gerente;
import com.menujpa.entities.Mesero;
import com.menujpa.entities.Persona;
import com.menujpa.repositories.ChefRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.GerenteRepository;
import com.menujpa.repositories.MeseroRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Busca el usuario en las 4 tablas de rol (no hay una tabla "Usuario" unica): cada rol conserva
// sus atributos propios en el modelo de dominio, en vez de forzar una entidad de auth generica.
@Service
public class MultiRoleUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;
    private final ChefRepository chefRepository;
    private final MeseroRepository meseroRepository;
    private final GerenteRepository gerenteRepository;

    public MultiRoleUserDetailsService(ClienteRepository clienteRepository, ChefRepository chefRepository,
                                        MeseroRepository meseroRepository, GerenteRepository gerenteRepository) {
        this.clienteRepository = clienteRepository;
        this.chefRepository = chefRepository;
        this.meseroRepository = meseroRepository;
        this.gerenteRepository = gerenteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Persona persona = clienteRepository.findByUsuario(usuario).map(p -> (Persona) p)
            .or(() -> chefRepository.findByUsuario(usuario).map(p -> (Persona) p))
            .or(() -> meseroRepository.findByUsuario(usuario).map(p -> (Persona) p))
            .or(() -> gerenteRepository.findByUsuario(usuario).map(p -> (Persona) p))
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usuario));

        if (persona.getContrasenia() == null) {
            throw new UsernameNotFoundException("El usuario no tiene credenciales configuradas: " + usuario);
        }

        return User.builder()
            .username(persona.getUsuario())
            .password(persona.getContrasenia())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + resolverRol(persona))))
            .build();
    }

    public String resolverRol(Persona persona) {
        if (persona instanceof Cliente) return "CLIENTE";
        if (persona instanceof Chef) return "CHEF";
        if (persona instanceof Mesero) return "MESERO";
        if (persona instanceof Gerente) return "GERENTE";
        throw new IllegalStateException("Tipo de Persona sin rol definido: " + persona.getClass());
    }
}
