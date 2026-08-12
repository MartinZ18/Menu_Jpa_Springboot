package com.menujpa.services;

import com.menujpa.entities.Chef;
import com.menujpa.entities.Empleado;
import com.menujpa.entities.Mesero;
import com.menujpa.entities.Pago;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ChefRepository;
import com.menujpa.repositories.MeseroRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PagoServiceImpl extends BaseServiceImpl<Pago, Long> implements PagoService {

    @Autowired private ChefRepository chefRepository;
    @Autowired private MeseroRepository meseroRepository;

    public PagoServiceImpl(BaseRepository<Pago, Long> baseRepository) { super(baseRepository); }

    @Override @Transactional
    public Pago generarPago(Long empleadoId, String rol) throws Exception {
        try {
            Empleado empleado = buscarEmpleado(empleadoId, rol);
            if (empleado.getSalario() == null)
                throw new Exception("El empleado no tiene un salario configurado.");

            Pago pago = new Pago();
            pago.setEmpleadoNombre(empleado.getApellido() + ", " + empleado.getNombre());
            pago.setEmpleadoRol(rol.toUpperCase());
            pago.setMonto(empleado.getSalario());
            pago.setFechaPago(new Date());

            return baseRepository.save(pago);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    private Empleado buscarEmpleado(Long empleadoId, String rol) throws Exception {
        if ("CHEF".equalsIgnoreCase(rol)) {
            return chefRepository.findById(empleadoId)
                .map(Empleado.class::cast)
                .orElseThrow(() -> new Exception("Chef no encontrado con id: " + empleadoId));
        }
        if ("MESERO".equalsIgnoreCase(rol)) {
            return meseroRepository.findById(empleadoId)
                .map(Empleado.class::cast)
                .orElseThrow(() -> new Exception("Mesero no encontrado con id: " + empleadoId));
        }
        throw new Exception("Rol de empleado invalido: \"" + rol + "\" (debe ser CHEF o MESERO).");
    }
}
