package com.menujpa.services;
import com.menujpa.entities.Menu;
import com.menujpa.entities.Receta;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.MenuRepository;
import com.menujpa.repositories.RecetaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class MenuServiceImpl extends BaseServiceImpl<Menu, Long> implements MenuService {
    @Autowired private MenuRepository menuRepository;
    @Autowired private RecetaRepository recetaRepository;
    public MenuServiceImpl(BaseRepository<Menu, Long> baseRepository) { super(baseRepository); }
    @Override @Transactional
    public Menu agregarReceta(Long menuId, Long recetaId) throws Exception {
        try {
            Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new Exception("Menú no encontrado con id: " + menuId));
            Receta receta = recetaRepository.findById(recetaId).orElseThrow(() -> new Exception("Receta no encontrada con id: " + recetaId));
            if (!menu.getRecetas().contains(receta)) { menu.getRecetas().add(receta); menuRepository.save(menu); }
            return menu;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public Menu quitarReceta(Long menuId, Long recetaId) throws Exception {
        try {
            Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new Exception("Menú no encontrado con id: " + menuId));
            Receta receta = recetaRepository.findById(recetaId).orElseThrow(() -> new Exception("Receta no encontrada con id: " + recetaId));
            menu.getRecetas().remove(receta);
            menuRepository.save(menu);
            return menu;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
}
