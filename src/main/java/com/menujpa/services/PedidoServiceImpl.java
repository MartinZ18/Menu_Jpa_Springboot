package com.menujpa.services;
import com.menujpa.entities.Pedido;
import com.menujpa.repositories.BaseRepository;
import org.springframework.stereotype.Service;
@Service
public class PedidoServiceImpl extends BaseServiceImpl<Pedido, Long> implements PedidoService {
    public PedidoServiceImpl(BaseRepository<Pedido, Long> baseRepository) { super(baseRepository); }
}
