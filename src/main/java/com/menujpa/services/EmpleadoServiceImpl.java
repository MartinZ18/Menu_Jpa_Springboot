package com.menujpa.services;

import com.menujpa.entities.Empleado;
import com.menujpa.repositories.BaseRepository;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public abstract class EmpleadoServiceImpl<E extends Empleado, ID extends Serializable> extends PersonaServiceImpl<E, ID> {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    protected EmpleadoServiceImpl(BaseRepository<E, ID> baseRepository) { super(baseRepository); }

    public E registrarEntrada(ID id) throws Exception {
        E empleado = baseRepository.findById(id)
            .orElseThrow(() -> new Exception("Entidad no encontrada con id: " + id));
        empleado.setHoraIngreso(LocalTime.now().format(FORMATO_HORA));
        return baseRepository.save(empleado);
    }

    public E registrarSalida(ID id) throws Exception {
        E empleado = baseRepository.findById(id)
            .orElseThrow(() -> new Exception("Entidad no encontrada con id: " + id));
        empleado.setHoraSalida(LocalTime.now().format(FORMATO_HORA));
        return baseRepository.save(empleado);
    }
}
