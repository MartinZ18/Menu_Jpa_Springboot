package com.menujpa.services;
import com.menujpa.entities.Receta;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.MenuRepository;
import com.menujpa.repositories.RecetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class RecetaServiceImpl extends BaseServiceImpl<Receta, Long> implements RecetaService {
    @Autowired private RecetaRepository recetaRepository;
    @Autowired private MenuRepository menuRepository;
    public RecetaServiceImpl(BaseRepository<Receta, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (menuRepository.existsByRecetasId(id))
            throw new Exception("No se puede eliminar la receta porque está incluida en uno o más menús.");
        return super.delete(id);
    }
}
