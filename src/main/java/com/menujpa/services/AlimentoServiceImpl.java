package com.menujpa.services;
import com.menujpa.entities.Alimento;
import com.menujpa.repositories.AlimentoRepository;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class AlimentoServiceImpl extends BaseServiceImpl<Alimento, Long> implements AlimentoService {
    @Autowired private AlimentoRepository alimentoRepository;
    @Autowired private RecetaRepository recetaRepository;
    public AlimentoServiceImpl(BaseRepository<Alimento, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (recetaRepository.existsByAlimentosSeleccionadosId(id))
            throw new Exception("No se puede eliminar el alimento porque está usado en una o más recetas.");
        return super.delete(id);
    }
}
