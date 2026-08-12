package com.menujpa.services;
import com.menujpa.entities.Despensa;
public interface DespensaService extends BaseService<Despensa, Long> {
    Despensa agregarIngrediente(Long despensaId, Long ingredienteId) throws Exception;
    Despensa quitarIngrediente(Long despensaId, Long ingredienteId) throws Exception;
}
