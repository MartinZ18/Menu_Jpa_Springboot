package com.menujpa.services;
import com.menujpa.entities.Despensa;
import com.menujpa.entities.Ingrediente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.DespensaRepository;
import com.menujpa.repositories.IngredienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class DespensaServiceImpl extends BaseServiceImpl<Despensa, Long> implements DespensaService {
    @Autowired private DespensaRepository despensaRepository;
    @Autowired private IngredienteRepository ingredienteRepository;
    public DespensaServiceImpl(BaseRepository<Despensa, Long> baseRepository) { super(baseRepository); }
    @Override @Transactional
    public Despensa agregarIngrediente(Long despensaId, Long ingredienteId) throws Exception {
        try {
            Despensa despensa = despensaRepository.findById(despensaId).orElseThrow(() -> new Exception("Despensa no encontrada con id: " + despensaId));
            Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId).orElseThrow(() -> new Exception("Ingrediente no encontrado con id: " + ingredienteId));
            if (!despensa.getIngredientes().contains(ingrediente)) { despensa.getIngredientes().add(ingrediente); despensaRepository.save(despensa); }
            return despensa;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public Despensa quitarIngrediente(Long despensaId, Long ingredienteId) throws Exception {
        try {
            Despensa despensa = despensaRepository.findById(despensaId).orElseThrow(() -> new Exception("Despensa no encontrada con id: " + despensaId));
            Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId).orElseThrow(() -> new Exception("Ingrediente no encontrado con id: " + ingredienteId));
            despensa.getIngredientes().remove(ingrediente);
            despensaRepository.save(despensa);
            return despensa;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
}
