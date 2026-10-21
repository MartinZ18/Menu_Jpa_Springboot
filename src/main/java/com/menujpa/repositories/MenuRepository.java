package com.menujpa.repositories;
import com.menujpa.entities.Menu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface MenuRepository extends BaseRepository<Menu, Long> {
    boolean existsByGerenteId(Long gerenteId);
    boolean existsByRecetasId(Long recetaId);

    // Listado sin N+1: menus + gerente + recetas en una sola query. Los alimentos de cada receta
    // se completan despues con RecetaRepository.findAllFetchAlimentosDeMenus() (dos colecciones List
    // anidadas no se pueden traer en el mismo JOIN FETCH: MultipleBagFetchException).
    @Override
    @Query("SELECT DISTINCT m FROM Menu m LEFT JOIN FETCH m.gerente LEFT JOIN FETCH m.recetas ORDER BY m.id")
    List<Menu> findAll();
}
