package com.menujpa.services;
import com.menujpa.entities.Mesero;
import com.menujpa.repositories.BaseRepository;
import org.springframework.stereotype.Service;
@Service
public class MeseroServiceImpl extends BaseServiceImpl<Mesero, Long> implements MeseroService {
    public MeseroServiceImpl(BaseRepository<Mesero, Long> baseRepository) { super(baseRepository); }
}
