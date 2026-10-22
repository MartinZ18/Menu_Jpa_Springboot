package com.menujpa.repositories;
import com.menujpa.entities.Pedido;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Pedidos en los que participa un cliente: sus colecciones se completan por lotes
    // (default_batch_fetch_size), no una query por pedido.
    @Query("SELECT DISTINCT p FROM Pedido p JOIN p.clientes c WHERE c.usuario = :usuario ORDER BY p.id")
    List<Pedido> findByClienteUsuario(@Param("usuario") String usuario);

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.meseros")
    List<Pedido> findAllFetchMeseros();

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.alimentosAdquiridos")
    List<Pedido> findAllFetchAlimentos();
}
