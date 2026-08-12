package com.menujpa.repositories;
import com.menujpa.entities.Cliente;
import org.springframework.stereotype.Repository;
@Repository
public interface ClienteRepository extends BaseRepository<Cliente, Long> {}
