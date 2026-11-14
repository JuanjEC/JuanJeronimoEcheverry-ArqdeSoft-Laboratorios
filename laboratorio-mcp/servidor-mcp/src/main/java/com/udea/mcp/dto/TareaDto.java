package com.udea.mcp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.udea.mcp.model.Tarea;
import java.time.LocalDate;

public record TareaDto(Long id, String titulo, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate fecha, boolean completado) {
    public static TareaDto de(Tarea tarea) {
        return new TareaDto(tarea.getId(), tarea.getTitulo(), tarea.getFecha(), tarea.isCompletado());
    }
}
