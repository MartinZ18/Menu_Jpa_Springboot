package com.menujpa.services;
import com.menujpa.entities.Mesero;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class MeseroServiceImpl extends PersonaServiceImpl<Mesero, Long> implements MeseroService {
    @Autowired private PedidoRepository pedidoRepository;
    public MeseroServiceImpl(BaseRepository<Mesero, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (pedidoRepository.existsByMeserosId(id))
            throw new Exception("No se puede eliminar el mesero porque tiene pedidos asociados.");
        return super.delete(id);
    }
}
