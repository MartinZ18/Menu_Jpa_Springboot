package com.menujpa.services;
import com.menujpa.entities.Menu;
public interface MenuService extends BaseService<Menu, Long> {
    Menu agregarReceta(Long menuId, Long recetaId) throws Exception;
    Menu quitarReceta(Long menuId, Long recetaId) throws Exception;
}
