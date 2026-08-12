package com.menujpa.repositories;
import com.menujpa.entities.Pedido;
import org.springframework.stereotype.Repository;
@Repository
public interface PedidoRepository extends BaseRepository<Pedido, Long> {
    boolean existsByClientesId(Long clienteId);
    boolean existsByMeserosId(Long meseroId);
    boolean existsByAlimentosAdquiridosId(Long alimentoId);
}
