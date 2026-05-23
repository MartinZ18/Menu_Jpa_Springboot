package com.menujpa.services;
import com.menujpa.entities.Base;
import com.menujpa.repositories.BaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
public abstract class BaseServiceImpl<E extends Base, ID extends Serializable> implements BaseService<E, ID> {
    protected BaseRepository<E, ID> baseRepository;
    public BaseServiceImpl(BaseRepository<E, ID> baseRepository) { this.baseRepository = baseRepository; }
    @Override @Transactional
    public List<E> findAll() throws Exception {
        try { return baseRepository.findAll(); } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public Page<E> findAll(Pageable pageable) throws Exception {
        try { return baseRepository.findAll(pageable); } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public E findById(ID id) throws Exception {
        try {
            Optional<E> entity = baseRepository.findById(id);
            return entity.orElseThrow(() -> new Exception("Entidad no encontrada con id: " + id));
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public E save(E entity) throws Exception {
        try { return baseRepository.save(entity); } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public E update(ID id, E entity) throws Exception {
        try {
            baseRepository.findById(id).orElseThrow(() -> new Exception("Entidad no encontrada con id: " + id));
            return baseRepository.save(entity);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
    @Override @Transactional
    public boolean delete(ID id) throws Exception {
        try {
            if (baseRepository.existsById(id)) { baseRepository.deleteById(id); return true; }
            else throw new Exception("Entidad no encontrada con id: " + id);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }
}
