package com.menujpa.repositories;
import com.menujpa.entities.Mesero;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface MeseroRepository extends BaseRepository<Mesero, Long> {
    Optional<Mesero> findByUsuario(String usuario);
}
