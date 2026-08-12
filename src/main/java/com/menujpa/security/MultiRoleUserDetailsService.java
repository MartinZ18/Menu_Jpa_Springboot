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
        Persona persona = buscarPersonaPorUsuario(usuario);
        if (persona == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + usuario);
        }

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

    // Busca la Persona (cualquier rol) duena de un usuario. Usado tanto para login como para
    // operaciones cross-rol que no tienen su propio repositorio (cambio de contrasenia, chequeo
    // de unicidad de usuario en el alta).
    public Persona buscarPersonaPorUsuario(String usuario) {
        return clienteRepository.findByUsuario(usuario).map(p -> (Persona) p)
            .or(() -> chefRepository.findByUsuario(usuario).map(p -> (Persona) p))
            .or(() -> meseroRepository.findByUsuario(usuario).map(p -> (Persona) p))
            .or(() -> gerenteRepository.findByUsuario(usuario).map(p -> (Persona) p))
            .orElse(null);
    }

    // El "usuario" no tiene una restriccion UNIQUE global: cada rol es una tabla separada, asi
    // que sin este chequeo cruzado un Cliente podria registrarse con el mismo usuario que ya
    // tiene un Chef/Mesero/Gerente, y el login solo alcanzaria al primero segun el orden de
    // busqueda de arriba (Cliente > Chef > Mesero > Gerente), dejando al otro sin poder entrar.
    public boolean existeUsuario(String usuario) {
        return buscarPersonaPorUsuario(usuario) != null;
    }

    public void cambiarContrasenia(String usuario, String contraseniaActual, String contraseniaNueva) throws Exception {
        Persona persona = buscarPersonaPorUsuario(usuario);
        if (persona == null) {
            throw new Exception("Usuario no encontrado: " + usuario);
        }
        if (!PasswordHasher.matches(contraseniaActual, persona.getContrasenia())) {
            throw new Exception("La contraseña actual no es correcta.");
        }

        persona.setContrasenia(PasswordHasher.hashIfNeeded(contraseniaNueva));
        if (persona instanceof Cliente c) clienteRepository.save(c);
        else if (persona instanceof Chef c) chefRepository.save(c);
        else if (persona instanceof Mesero m) meseroRepository.save(m);
        else if (persona instanceof Gerente g) gerenteRepository.save(g);
    }
}
