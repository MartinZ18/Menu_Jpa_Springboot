package com.menujpa.repositories;
import com.menujpa.entities.Receta;
import org.springframework.stereotype.Repository;
@Repository
public interface RecetaRepository extends BaseRepository<Receta, Long> {
    boolean existsByAlimentosSeleccionadosId(Long alimentoId);
    boolean existsByNombreChefTexto(String nombreChefTexto);
    long countByNombreChefTexto(String nombreChefTexto);
}
