package com.menujpa.repositories;
import com.menujpa.entities.Reserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
@Repository
public interface ReservaRepository extends BaseRepository<Reserva, Long> {
    List<Reserva> findByMesaIdAndFechaAndEstado(Long mesaId, Date fecha, String estado);
    boolean existsByMesaIdAndEstado(Long mesaId, String estado);

    // cliente y mesa son LAZY y se serializan siempre: se traen con un JOIN en la misma query
    // (y no como proxies, que Jackson no sabe serializar) en todos los caminos que devuelven reservas.
    @EntityGraph(attributePaths = {"cliente", "mesa"})
    List<Reserva> findByClienteUsuario(String usuario);

    @Override
    @EntityGraph(attributePaths = {"cliente", "mesa"})
    List<Reserva> findAll();

    @Override
    @EntityGraph(attributePaths = {"cliente", "mesa"})
    Optional<Reserva> findById(Long id);
}
