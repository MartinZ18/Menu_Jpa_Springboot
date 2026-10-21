package com.menujpa.repositories;
import com.menujpa.entities.Receta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RecetaRepository extends BaseRepository<Receta, Long> {
    boolean existsByAlimentosSeleccionadosId(Long alimentoId);
    boolean existsByNombreChefTexto(String nombreChefTexto);
    long countByNombreChefTexto(String nombreChefTexto);

    // Listado sin N+1: recetas + alimentos en una sola query.
    @Override
    @Query("SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.alimentosSeleccionados ORDER BY r.id")
    List<Receta> findAll();

    // Completa los alimentos de las recetas que ya trajo MenuRepository.findAll() dentro de la misma
    // transaccion: la query inicializa la coleccion sobre las mismas instancias de Receta.
    @Query("SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.alimentosSeleccionados "
        + "WHERE r.id IN (SELECT rm.id FROM Menu m JOIN m.recetas rm)")
    List<Receta> findAllFetchAlimentosDeMenus();
}
