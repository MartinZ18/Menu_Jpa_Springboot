package com.menujpa.services;

import com.menujpa.entities.Alimento;
import com.menujpa.entities.Cliente;
import com.menujpa.entities.Mesero;
import com.menujpa.entities.Pedido;
import com.menujpa.repositories.AlimentoRepository;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.MeseroRepository;
import com.menujpa.repositories.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl extends BaseServiceImpl<Pedido, Long> implements PedidoService {

    @Autowired private MeseroRepository meseroRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AlimentoRepository alimentoRepository;
    @Autowired private PedidoRepository pedidoRepository;

    public PedidoServiceImpl(BaseRepository<Pedido, Long> baseRepository) { super(baseRepository); }

    // Elimina el N+1 sobre clientes/meseros/alimentos al listar: tres queries con JOIN FETCH en la
    // misma transaccion en vez de dejar que Hibernate resuelva cada coleccion LAZY al recorrerla.
    // Las queries 2 y 3 completan "meseros" y "alimentosAdquiridos" sobre las mismas instancias que
    // trajo la primera, porque todas corren contra el mismo persistence context (misma transaccion).
    @Override @Transactional
    public List<Pedido> findAll() throws Exception {
        try {
            List<Pedido> pedidos = pedidoRepository.findAllFetchClientes();
            pedidoRepository.findAllFetchMeseros();
            pedidoRepository.findAllFetchAlimentos();
            return pedidos;
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public Pedido tomarPedido(String meseroUsuario, List<Long> clienteIds, List<Long> alimentoIds) throws Exception {
        try {
            Mesero mesero = meseroRepository.findByUsuario(meseroUsuario)
                .orElseThrow(() -> new Exception("Mesero no encontrado: " + meseroUsuario));

            Pedido pedido = new Pedido();
            pedido.getMeseros().add(mesero);
            cargarClientesYAlimentos(pedido, clienteIds, alimentoIds);
            pedido.setFechaPedido(new Date());
            pedido.setHoraPedido(horaActual());
            pedido.setEstado(false);

            return baseRepository.save(pedido);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public Pedido modificarPedido(Long pedidoId, List<Long> clienteIds, List<Long> alimentoIds,
                                   String usuarioSolicitante, boolean esGerente) throws Exception {
        try {
            Pedido pedido = baseRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con id: " + pedidoId));
            verificarPropiedad(pedido, usuarioSolicitante, esGerente);
            if (Boolean.TRUE.equals(pedido.getEstado()))
                throw new Exception("No se puede modificar un pedido ya entregado.");

            cargarClientesYAlimentos(pedido, clienteIds, alimentoIds);
            return baseRepository.save(pedido);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public Pedido entregarPedido(Long pedidoId, String usuarioSolicitante, boolean esGerente) throws Exception {
        try {
            Pedido pedido = baseRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con id: " + pedidoId));
            verificarPropiedad(pedido, usuarioSolicitante, esGerente);
            if (Boolean.TRUE.equals(pedido.getEstado()))
                throw new Exception("El pedido ya fue entregado.");

            pedido.setEstado(true);
            return baseRepository.save(pedido);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public void cancelarPedido(Long pedidoId, String usuarioSolicitante, boolean esGerente) throws Exception {
        try {
            Pedido pedido = baseRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con id: " + pedidoId));
            verificarPropiedad(pedido, usuarioSolicitante, esGerente);
            if (Boolean.TRUE.equals(pedido.getEstado()))
                throw new Exception("No se puede cancelar un pedido ya entregado.");

            baseRepository.deleteById(pedidoId);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    private void verificarPropiedad(Pedido pedido, String usuarioSolicitante, boolean esGerente) throws Exception {
        if (esGerente) return;
        boolean esElMeseroQueLoTomo = pedido.getMeseros().stream()
            .anyMatch(m -> usuarioSolicitante.equals(m.getUsuario()));
        if (!esElMeseroQueLoTomo)
            throw new Exception("No podés modificar un pedido que no tomaste vos.");
    }

    // Una sola query por tipo (WHERE id IN (...)) en vez de un findById por cada id. Se conserva el
    // orden y las repeticiones de la lista original (un alimento pedido dos veces cuenta dos veces)
    // y el primer id inexistente, en ese orden, es el que se informa.
    private void cargarClientesYAlimentos(Pedido pedido, List<Long> clienteIds, List<Long> alimentoIds) throws Exception {
        Map<Long, Cliente> clientesPorId = clienteRepository.findAllById(new LinkedHashSet<>(clienteIds)).stream()
            .collect(Collectors.toMap(Cliente::getId, Function.identity()));
        List<Cliente> clientes = new ArrayList<>();
        for (Long clienteId : clienteIds) {
            Cliente cliente = clientesPorId.get(clienteId);
            if (cliente == null) throw new Exception("Cliente no encontrado con id: " + clienteId);
            clientes.add(cliente);
        }
        pedido.setClientes(clientes);

        Map<Long, Alimento> alimentosPorId = alimentoRepository.findAllById(new LinkedHashSet<>(alimentoIds)).stream()
            .collect(Collectors.toMap(Alimento::getId, Function.identity()));
        List<Alimento> alimentos = new ArrayList<>();
        double total = 0.0;
        for (Long alimentoId : alimentoIds) {
            Alimento alimento = alimentosPorId.get(alimentoId);
            if (alimento == null) throw new Exception("Alimento no encontrado con id: " + alimentoId);
            alimentos.add(alimento);
            total += alimento.getPrecio() != null ? alimento.getPrecio() : 0.0;
        }
        pedido.setAlimentosAdquiridos(alimentos);
        pedido.setPrecioTotalPedido(total);
    }

    private String horaActual() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
