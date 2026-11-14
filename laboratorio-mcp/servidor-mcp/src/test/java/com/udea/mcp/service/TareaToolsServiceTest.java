package com.udea.mcp.service;

import com.udea.mcp.dto.TareaDto;
import com.udea.mcp.repository.TareaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TareaToolsServiceTest {
    @Autowired TareaToolsService tools;
    @Autowired TareaRepository tareas;

    @BeforeEach void limpiar() { tareas.deleteAll(); }

    @Test void cicloDeVidaYConsultas() {
        TareaDto primera = tools.crearTarea("  Preparar Slides  ", "2028-02-29");
        TareaDto segunda = tools.crearTarea("Probar MCP", "2026-11-15");
        assertThat(primera.titulo()).isEqualTo("Preparar Slides");
        assertThat(tools.contarTareas()).isEqualTo(2);
        assertThat(tools.listarTareas()).extracting(TareaDto::id).containsExactly(primera.id(), segunda.id());
        assertThat(tools.buscarTareas("sLiDeS")).extracting(TareaDto::id).containsExactly(primera.id());
        assertThat(tools.marcarCompletada(primera.id()).completado()).isTrue();
        assertThat(tools.marcarCompletada(primera.id()).completado()).isTrue();
        assertThat(tools.listarPendientes()).extracting(TareaDto::id).containsExactly(segunda.id());
        assertThat(tools.listarCompletadas()).extracting(TareaDto::id).containsExactly(primera.id());
        assertThat(tools.actualizarTarea(segunda.id(), "  Nuevo título ", null).titulo()).isEqualTo("Nuevo título");
        assertThat(tools.actualizarTarea(segunda.id(), null, "2026-12-01").fecha()).hasToString("2026-12-01");
        assertThat(tools.actualizarTarea(segunda.id(), null, null).titulo()).isEqualTo("Nuevo título");
        assertThat(tools.eliminarTarea(primera.id())).contains("eliminada");
        assertThat(tools.eliminarTarea(primera.id())).contains("no existe");
        assertThat(tools.contarTareas()).isEqualTo(1);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2026-2-03", "2026-02-30", "2025-02-29", "2026-13-01", "0000-01-01", "+2026-11-15", " 2026-11-15", "2026-11-15 ", "2026-11-15T00:00:00", "15/11/2026"})
    void rechazaFechasNoIsoEstrictas(String fecha) {
        assertThatIllegalArgumentException().isThrownBy(() -> tools.crearTarea("Prueba", fecha));
        assertThat(tareas.count()).isZero();
    }

    @Test void validaTitulosIdsYFragmentos() {
        for (String titulo : new String[]{null, "", "   ", "x".repeat(256)})
            assertThatIllegalArgumentException().isThrownBy(() -> tools.crearTarea(titulo, "2026-11-15"));
        for (Long id : new Long[]{null, 0L, -1L, Long.MAX_VALUE}) {
            assertThatIllegalArgumentException().isThrownBy(() -> tools.marcarCompletada(id));
            assertThatIllegalArgumentException().isThrownBy(() -> tools.actualizarTarea(id, null, null));
        }
        for (String fragmento : new String[]{null, " ", "x".repeat(256)})
            assertThatIllegalArgumentException().isThrownBy(() -> tools.buscarTareas(fragmento));
        assertThatIllegalArgumentException().isThrownBy(() -> tools.eliminarTarea(0L));
    }

    @Test void actualizacionInvalidaNoModificaTituloNiFecha() {
        TareaDto original = tools.crearTarea("Original", "2026-11-15");
        assertThatIllegalArgumentException().isThrownBy(() -> tools.actualizarTarea(original.id(), "Modificado", "2026-02-30"));
        assertThatIllegalArgumentException().isThrownBy(() -> tools.actualizarTarea(original.id(), "  ", null));
        assertThat(tools.listarTareas()).containsExactly(original);
    }
}
