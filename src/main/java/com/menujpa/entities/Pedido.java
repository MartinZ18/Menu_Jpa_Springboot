package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "pedido")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Pedido extends Base {

    // Relación con Cliente: "Manipula" en el diagrama, cliente : Cliente (composición)
    // @BatchSize: al listar varios Pedido, Hibernate trae estas colecciones LAZY con un IN (...)
    // por lote de 20 pedidos en vez de una query por fila (N+1) al recorrerlas.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "pedido_cliente",
        joinColumns = @JoinColumn(name = "idPedido"),
        inverseJoinColumns = @JoinColumn(name = "idCliente")
    )
    @BatchSize(size = 20)
    private List<Cliente> clientes = new ArrayList<>();

    // Relación con Mesero: miMesero : 1..* en el diagrama
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "pedido_mesero",
        joinColumns = @JoinColumn(name = "idPedido"),
        inverseJoinColumns = @JoinColumn(name = "idMesero")
    )
    @BatchSize(size = 20)
    private List<Mesero> meseros = new ArrayList<>();

    // Relación con Alimento: alimentosAdquiridos — composición (diamante lleno en diagrama)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "pedido_alimento",
        joinColumns = @JoinColumn(name = "idPedido"),
        inverseJoinColumns = @JoinColumn(name = "idAlimento")
    )
    private List<Alimento> alimentosAdquiridos = new ArrayList<>();

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha_pedido")
    private Date fechaPedido;

    @Column(name = "hora_pedido", length = 10)
    private String horaPedido;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio total no puede ser negativo")
    @Column(name = "precio_total_pedido")
    private Double precioTotalPedido;

    @Column(name = "estado", nullable = false)
    private Boolean estado = false;

    @Override
    public String toString() {
        return "Pedido #" + getId();
    }
}
