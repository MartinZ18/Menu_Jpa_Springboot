package com.menujpa.services;
import com.menujpa.entities.Cliente;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ClienteServiceImpl extends PersonaServiceImpl<Cliente, Long> implements ClienteService {
    @Autowired private PedidoRepository pedidoRepository;
    public ClienteServiceImpl(BaseRepository<Cliente, Long> baseRepository) { super(baseRepository); }
    @Override
    public boolean delete(Long id) throws Exception {
        if (pedidoRepository.existsByClientesId(id))
            throw new Exception("No se puede eliminar el cliente porque tiene pedidos asociados.");
        return super.delete(id);
    }
}
