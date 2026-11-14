package com.udea.mcp.config;

import com.udea.mcp.model.Producto;
import com.udea.mcp.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {"app.seed-demo=true", "spring.datasource.url=jdbc:h2:mem:seed-test;MODE=MySQL;DB_CLOSE_DELAY=-1"})
@ActiveProfiles("test")
class DemoSeederTest {
    @Autowired DemoSeeder seeder;
    @Autowired ProductoRepository productos;
    @Autowired TareaRepository tareas;

    @Test void seedExplicitoNoBorraNiReiniciaInventario() {
        assertThat(productos.count()).isEqualTo(4);
        assertThat(tareas.count()).isEqualTo(3);
        Producto producto = productos.findAll().getFirst(); producto.descontar(1); productos.saveAndFlush(producto);
        int restante = producto.getStock();
        seeder.run(null);
        assertThat(productos.count()).isEqualTo(4);
        assertThat(tareas.count()).isEqualTo(3);
        assertThat(productos.findById(producto.getId()).orElseThrow().getStock()).isEqualTo(restante);
    }
}
