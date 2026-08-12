package com.menujpa.services;
import com.menujpa.entities.Ingrediente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.DespensaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class IngredienteServiceImpl extends BaseServiceImpl<Ingrediente, Long> implements IngredienteService {
    @Autowired private DespensaRepository despensaRepository;
    public IngredienteServiceImpl(BaseRepository<Ingrediente, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (despensaRepository.existsByIngredientesId(id))
            throw new Exception("No se puede eliminar el ingrediente porque está incluido en una o más despensas.");
        return super.delete(id);
    }
}
