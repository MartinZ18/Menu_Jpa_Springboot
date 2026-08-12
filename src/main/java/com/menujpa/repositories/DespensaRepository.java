package com.menujpa.repositories;
import com.menujpa.entities.Despensa;
import org.springframework.stereotype.Repository;
@Repository
public interface DespensaRepository extends BaseRepository<Despensa, Long> {
    boolean existsByIngredientesId(Long ingredienteId);
}
