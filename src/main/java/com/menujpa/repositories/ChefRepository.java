package com.menujpa.repositories;
import com.menujpa.entities.Chef;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ChefRepository extends BaseRepository<Chef, Long> {
    Optional<Chef> findByUsuario(String usuario);
}
