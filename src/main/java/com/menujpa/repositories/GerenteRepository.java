package com.menujpa.repositories;
import com.menujpa.entities.Gerente;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface GerenteRepository extends BaseRepository<Gerente, Long> {
    Optional<Gerente> findByUsuario(String usuario);
}
