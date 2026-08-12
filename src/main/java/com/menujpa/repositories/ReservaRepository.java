package com.menujpa.repositories;
import com.menujpa.entities.Reserva;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
@Repository
public interface ReservaRepository extends BaseRepository<Reserva, Long> {
    List<Reserva> findByMesaIdAndFechaAndEstado(Long mesaId, Date fecha, String estado);
    boolean existsByMesaIdAndEstado(Long mesaId, String estado);
    List<Reserva> findByClienteUsuario(String usuario);
}
