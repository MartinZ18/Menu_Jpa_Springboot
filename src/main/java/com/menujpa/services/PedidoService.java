package com.menujpa.services;
import com.menujpa.entities.Pedido;
import java.util.List;

public interface PedidoService extends BaseService<Pedido, Long> {
    Pedido tomarPedido(String meseroUsuario, List<Long> clienteIds, List<Long> alimentoIds) throws Exception;
    Pedido modificarPedido(Long pedidoId, List<Long> clienteIds, List<Long> alimentoIds,
                           String usuarioSolicitante, boolean esGerente) throws Exception;
    Pedido entregarPedido(Long pedidoId, String usuarioSolicitante, boolean esGerente) throws Exception;
    void cancelarPedido(Long pedidoId, String usuarioSolicitante, boolean esGerente) throws Exception;
}
