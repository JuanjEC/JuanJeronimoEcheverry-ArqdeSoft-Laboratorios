package com.udea.mcp.config;

import com.udea.mcp.model.Producto;
import com.udea.mcp.model.Tarea;
import com.udea.mcp.repository.ProductoRepository;
import com.udea.mcp.repository.TareaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Solo para demostración explícita. Nunca vacía tablas ni repone inventario existente. */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
public class DemoSeeder implements ApplicationRunner {
    private final ProductoRepository productos;
    private final TareaRepository tareas;

    public DemoSeeder(ProductoRepository productos, TareaRepository tareas) {
        this.productos = productos; this.tareas = tareas;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productos.count() == 0) productos.saveAll(List.of(
                new Producto("Arroz 1 kg", new BigDecimal("4500.00"), 30),
                new Producto("Leche 1 L", new BigDecimal("3800.00"), 20),
                new Producto("Huevos docena", new BigDecimal("12000.00"), 15),
                new Producto("Pan integral", new BigDecimal("6500.00"), 12)));
        if (tareas.count() == 0) {
            Tarea completada = new Tarea("Probar demo en vivo", LocalDate.of(2026, 11, 15));
            completada.completar();
            tareas.saveAll(List.of(new Tarea("Preparar charla MCP", LocalDate.of(2026, 11, 14)),
                    new Tarea("Revisar slides", LocalDate.of(2026, 11, 15)), completada));
        }
    }
}
