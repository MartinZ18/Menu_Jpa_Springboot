package com.menujpa.services;

import com.menujpa.entities.Persona;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.security.PasswordHasher;

import java.io.Serializable;

public abstract class PersonaServiceImpl<E extends Persona, ID extends Serializable> extends BaseServiceImpl<E, ID> {

    protected PersonaServiceImpl(BaseRepository<E, ID> baseRepository) { super(baseRepository); }

    @Override
    public E save(E entity) throws Exception {
        entity.setContrasenia(PasswordHasher.hashIfNeeded(entity.getContrasenia()));
        return super.save(entity);
    }

    // El update generico no puede tocar la contrasenia: el cliente nunca la recibe (@JsonIgnore),
    // asi que si se la dejara pasar, un PUT normal la pisaria con null. Se preserva la existente
    // y el cambio de contrasenia se hace por un endpoint dedicado, no por acá.
    @Override
    public E update(ID id, E entity) throws Exception {
        E existente = baseRepository.findById(id)
            .orElseThrow(() -> new Exception("Entidad no encontrada con id: " + id));
        entity.setContrasenia(existente.getContrasenia());
        return super.update(id, entity);
    }
}
