package com.menujpa.services;
import com.menujpa.entities.Mesa;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class MesaServiceImpl extends BaseServiceImpl<Mesa, Long> implements MesaService {
    @Autowired private ReservaRepository reservaRepository;
    public MesaServiceImpl(BaseRepository<Mesa, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (reservaRepository.existsByMesaIdAndEstado(id, "ACTIVA"))
            throw new Exception("No se puede eliminar la mesa porque tiene reservas activas.");
        return super.delete(id);
    }
}
