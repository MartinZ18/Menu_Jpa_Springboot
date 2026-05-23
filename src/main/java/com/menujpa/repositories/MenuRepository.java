package com.menujpa.repositories;
import com.menujpa.entities.Menu;
import org.springframework.stereotype.Repository;
@Repository
public interface MenuRepository extends BaseRepository<Menu, Long> {
    boolean existsByGerenteId(Long gerenteId);
    boolean existsByRecetasId(Long recetaId);
}
