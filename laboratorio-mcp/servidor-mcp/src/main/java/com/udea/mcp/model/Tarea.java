package com.udea.mcp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tarea")
public class Tarea {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String titulo;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false)
    private boolean completado;
    @Version private Long version;

    protected Tarea() {}
    public Tarea(String titulo, LocalDate fecha) { this.titulo = titulo; this.fecha = fecha; }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public LocalDate getFecha() { return fecha; }
    public boolean isCompletado() { return completado; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void completar() { completado = true; }
}
