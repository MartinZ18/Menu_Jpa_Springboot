package com.menujpa.services;
import com.menujpa.entities.Chef;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ChefRepository;
import com.menujpa.repositories.RecetaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ChefServiceImpl extends BaseServiceImpl<Chef, Long> implements ChefService {
    @Autowired private ChefRepository chefRepository;
    @Autowired private RecetaRepository recetaRepository;
    public ChefServiceImpl(BaseRepository<Chef, Long> baseRepository) { super(baseRepository); }
    @Override @Transactional
    public boolean delete(Long id) throws Exception {
        Chef chef = chefRepository.findById(id).orElseThrow(() -> new Exception("Chef no encontrado con id: " + id));
        String nombreChef = chef.getApellido() + ", " + chef.getNombre();
        long cantidad = recetaRepository.countByNombreChefTexto(nombreChef);
        if (cantidad > 0)
            throw new Exception("No se puede eliminar al chef \"" + nombreChef + "\" porque tiene " +
                cantidad + " receta" + (cantidad == 1 ? "" : "s") + " asignada" + (cantidad == 1 ? "" : "s") +
                ". Reasigná o eliminá las recetas antes de continuar.");
        chefRepository.deleteById(id);
        return true;
    }
}
