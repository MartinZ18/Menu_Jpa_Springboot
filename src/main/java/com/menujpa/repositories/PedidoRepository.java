package com.menujpa.repositories;
import com.menujpa.entities.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface PedidoRepository extends BaseRepository<Pedido, Long> {
    boolean existsByClientesId(Long clienteId);
    boolean existsByMeserosId(Long meseroId);
    boolean existsByAlimentosAdquiridosId(Long alimentoId);

    // Hibernate no permite JOIN FETCH de dos colecciones List a la vez (MultipleBagFetchException),
    // asi que se traen en dos queries separadas dentro de la misma transaccion/persistence context:
    // la segunda popula "meseros" sobre las mismas instancias de Pedido que ya trajo la primera.
    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.clientes ORDER BY p.id")
    List<Pedido> findAllFetchClientes();

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.meseros")
    List<Pedido> findAllFetchMeseros();

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.alimentosAdquiridos")
    List<Pedido> findAllFetchAlimentos();
}
