package com.menujpa.services;
import com.menujpa.entities.Gerente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.GerenteRepository;
import com.menujpa.repositories.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class GerenteServiceImpl extends BaseServiceImpl<Gerente, Long> implements GerenteService {
    @Autowired private GerenteRepository gerenteRepository;
    @Autowired private MenuRepository menuRepository;
    public GerenteServiceImpl(BaseRepository<Gerente, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (menuRepository.existsByGerenteId(id))
            throw new Exception("No se puede eliminar el gerente porque está asignado a uno o más menús.");
        return super.delete(id);
    }
}
