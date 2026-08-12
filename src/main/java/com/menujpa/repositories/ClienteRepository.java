package com.menujpa.repositories;
import com.menujpa.entities.Cliente;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {
    Optional<Cliente> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
}
