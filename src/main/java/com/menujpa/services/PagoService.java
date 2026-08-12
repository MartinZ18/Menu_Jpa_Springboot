package com.menujpa.services;
import com.menujpa.entities.Pago;
public interface PagoService extends BaseService<Pago, Long> {
    Pago generarPago(Long empleadoId, String rol) throws Exception;
}
