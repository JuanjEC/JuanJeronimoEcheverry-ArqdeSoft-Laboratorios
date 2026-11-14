package com.udea.mcp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "producto")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 150) private String nombre;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal precio;
    @Column(nullable = false) private int stock;
    @Version private Long version;

    protected Producto() {}
    public Producto(String nombre, BigDecimal precio, int stock) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 150 || precio == null || precio.signum() <= 0 || stock < 0)
            throw new IllegalArgumentException("Producto inválido");
        this.nombre = nombre.strip();
        try { this.precio = precio.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException ex) { throw new IllegalArgumentException("El precio admite hasta dos decimales", ex); }
        if (this.precio.precision() > 19) throw new IllegalArgumentException("Precio fuera de rango");
        this.stock = stock;
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public void descontar(int cantidad) {
        if (cantidad <= 0 || cantidad > stock) throw new IllegalArgumentException("Stock insuficiente para producto " + id);
        stock -= cantidad;
    }
    public void reponer(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        stock = Math.addExact(stock, cantidad);
    }
}
