package com.udea.mcp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {
    public enum Estado { CREADO, CANCELADO }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 150) private String cliente;
    @Column(nullable = false, length = 500) private String direccion;
    @Column(nullable = false) private Instant creadoEn;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Estado estado;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal total;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("productoId ASC")
    private List<PedidoItem> items = new ArrayList<>();
    @Version private Long version;

    protected Pedido() {}
    public Pedido(String cliente, String direccion) {
        this.cliente = cliente; this.direccion = direccion; creadoEn = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS); estado = Estado.CREADO; total = new BigDecimal("0.00");
    }
    public void agregar(Producto producto, int cantidad) {
        PedidoItem item = new PedidoItem(this, producto, cantidad);
        items.add(item); total = total.add(item.getSubtotal());
        if (total.precision() > 19) throw new IllegalArgumentException("Total del pedido fuera de rango");
    }
    public Long getId() { return id; }
    public String getCliente() { return cliente; }
    public String getDireccion() { return direccion; }
    public Instant getCreadoEn() { return creadoEn; }
    public Estado getEstado() { return estado; }
    public BigDecimal getTotal() { return total; }
    public List<PedidoItem> getItems() { return List.copyOf(items); }
    public void cancelar() { estado = Estado.CANCELADO; }
}
