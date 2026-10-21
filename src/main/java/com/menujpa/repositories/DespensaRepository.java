package com.menujpa.repositories;
import com.menujpa.entities.Despensa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DespensaRepository extends BaseRepository<Despensa, Long> {
    boolean existsByIngredientesId(Long ingredienteId);

    // Listado sin N+1: despensas + ingredientes en una sola query.
    @Override
    @Query("SELECT DISTINCT d FROM Despensa d LEFT JOIN FETCH d.ingredientes ORDER BY d.id")
    List<Despensa> findAll();
}
