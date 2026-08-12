package com.menujpa.repositories;
import com.menujpa.entities.Mesa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface MesaRepository extends BaseRepository<Mesa, Long> {
    // SELECT ... FOR UPDATE: usado solo en reservarMesa para serializar el chequeo de
    // solapamiento + insert contra la misma mesa (ver ReservaServiceImpl).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mesa m WHERE m.id = :id")
    Optional<Mesa> findByIdForUpdate(@Param("id") Long id);
}
