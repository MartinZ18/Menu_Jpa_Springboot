package com.menujpa.services;
import com.menujpa.entities.Cliente;
import com.menujpa.repositories.BaseRepository;
import org.springframework.stereotype.Service;
@Service
public class ClienteServiceImpl extends BaseServiceImpl<Cliente, Long> implements ClienteService {
    public ClienteServiceImpl(BaseRepository<Cliente, Long> baseRepository) { super(baseRepository); }
}
