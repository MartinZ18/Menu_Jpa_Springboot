package com.menujpa.services;
import com.menujpa.entities.Ingrediente;
import com.menujpa.repositories.BaseRepository;
import org.springframework.stereotype.Service;
@Service
public class IngredienteServiceImpl extends BaseServiceImpl<Ingrediente, Long> implements IngredienteService {
    public IngredienteServiceImpl(BaseRepository<Ingrediente, Long> baseRepository) { super(baseRepository); }
}
