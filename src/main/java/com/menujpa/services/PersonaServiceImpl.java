package com.menujpa.services;

import com.menujpa.entities.Persona;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.security.MultiRoleUserDetailsService;
import com.menujpa.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Objects;

public abstract class PersonaServiceImpl<E extends Persona, ID extends Serializable> extends BaseServiceImpl<E, ID> {

    private MultiRoleUserDetailsService usuarios;

    protected PersonaServiceImpl(BaseRepository<E, ID> baseRepository) { super(baseRepository); }

    // Mismo chequeo cruzado que el registro (AuthController): "usuario" no tiene UNIQUE global,
    // asi que sin esto el gerente podria dar de alta personal con el usuario de otro rol y el
    // login tapara una de las dos cuentas. Setter para que los tests unitarios puedan pasar un mock.
    @Autowired
    public void setUsuarios(MultiRoleUserDetailsService usuarios) { this.usuarios = usuarios; }

    @Override
    public E save(E entity) throws Exception {
        if (entity.getContrasenia() == null || entity.getContrasenia().isBlank()) {
            throw new Exception("La contraseña es obligatoria");
        }
        rechazarUsuarioEnUso(entity.getUsuario());
        entity.setContrasenia(PasswordHasher.hashIfNeeded(entity.getContrasenia()));
        return super.save(entity);
    }

    // El update generico no puede tocar la contrasenia: aunque ahora se puede enviar en el
    // POST (WRITE_ONLY), un PUT normal no deberia poder cambiarla. Se preserva la existente
    // y el cambio de contrasenia se hace por un endpoint dedicado, no por acá.
    @Override
    public E update(ID id, E entity) throws Exception {
        E existente = baseRepository.findById(id)
            .orElseThrow(() -> new Exception("Entidad no encontrada con id: " + id));
        // Conservar su propio usuario no es un conflicto; solo se chequea si lo cambia.
        if (!Objects.equals(entity.getUsuario(), existente.getUsuario())) {
            rechazarUsuarioEnUso(entity.getUsuario());
        }
        entity.setContrasenia(existente.getContrasenia());
        return super.update(id, entity);
    }

    private void rechazarUsuarioEnUso(String usuario) throws Exception {
        if (usuario != null && !usuario.isBlank() && usuarios.existeUsuario(usuario)) {
            throw new Exception("El usuario \"" + usuario + "\" ya está en uso.");
        }
    }
}
