package com.udea.mcp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_item", uniqueConstraints = @UniqueConstraint(columnNames = {"pedido_id", "producto_id"}))
public class PedidoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
    @Column(name = "producto_id", nullable = false) private Long productoId;
    @Column(nullable = false, length = 150) private String nombre;
    @Column(nullable = false) private int cantidad;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal precioUnitario;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal subtotal;

    protected PedidoItem() {}
    public PedidoItem(Pedido pedido, Producto producto, int cantidad) {
        if (pedido == null || producto == null || cantidad <= 0) throw new IllegalArgumentException("Línea de pedido inválida");
        this.pedido = pedido; productoId = producto.getId(); nombre = producto.getNombre();
        this.cantidad = cantidad; precioUnitario = producto.getPrecio(); subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
    public Long getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
