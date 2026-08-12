package com.menujpa.services;

import com.menujpa.entities.Alimento;
import com.menujpa.entities.Cliente;
import com.menujpa.entities.Mesero;
import com.menujpa.entities.Pedido;
import com.menujpa.repositories.AlimentoRepository;
import com.menujpa.repositories.BaseRepository;
import com.menujpa.repositories.ClienteRepository;
import com.menujpa.repositories.MeseroRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PedidoServiceImpl extends BaseServiceImpl<Pedido, Long> implements PedidoService {

    @Autowired private MeseroRepository meseroRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AlimentoRepository alimentoRepository;

    public PedidoServiceImpl(BaseRepository<Pedido, Long> baseRepository) { super(baseRepository); }

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
    public Pedido modificarPedido(Long pedidoId, List<Long> clienteIds, List<Long> alimentoIds) throws Exception {
        try {
            Pedido pedido = baseRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con id: " + pedidoId));
            if (Boolean.TRUE.equals(pedido.getEstado()))
                throw new Exception("No se puede modificar un pedido ya entregado.");

            cargarClientesYAlimentos(pedido, clienteIds, alimentoIds);
            return baseRepository.save(pedido);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public Pedido entregarPedido(Long pedidoId) throws Exception {
        try {
            Pedido pedido = baseRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con id: " + pedidoId));
            if (Boolean.TRUE.equals(pedido.getEstado()))
                throw new Exception("El pedido ya fue entregado.");

            pedido.setEstado(true);
            return baseRepository.save(pedido);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    @Override @Transactional
    public void cancelarPedido(Long pedidoId) throws Exception {
        try {
            Pedido pedido = baseRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con id: " + pedidoId));
            if (Boolean.TRUE.equals(pedido.getEstado()))
                throw new Exception("No se puede cancelar un pedido ya entregado.");

            baseRepository.deleteById(pedidoId);
        } catch (Exception e) { throw new Exception(e.getMessage()); }
    }

    private void cargarClientesYAlimentos(Pedido pedido, List<Long> clienteIds, List<Long> alimentoIds) throws Exception {
        List<Cliente> clientes = new ArrayList<>();
        for (Long clienteId : clienteIds) {
            clientes.add(clienteRepository.findById(clienteId)
                .orElseThrow(() -> new Exception("Cliente no encontrado con id: " + clienteId)));
        }
        pedido.setClientes(clientes);

        List<Alimento> alimentos = new ArrayList<>();
        double total = 0.0;
        for (Long alimentoId : alimentoIds) {
            Alimento alimento = alimentoRepository.findById(alimentoId)
                .orElseThrow(() -> new Exception("Alimento no encontrado con id: " + alimentoId));
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
