package com.udea.mcp.service;

import com.udea.mcp.dto.TareaDto;
import com.udea.mcp.model.Tarea;
import com.udea.mcp.repository.TareaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TareaToolsService {
    private final TareaRepository repo;

    public TareaToolsService(TareaRepository repo) { this.repo = repo; }

    @Tool(description = "Lista todas las tareas, ordenadas por id, con título, fecha y estado.")
    public List<TareaDto> listarTareas() {
        return repo.findAll(Sort.by("id")).stream().map(TareaDto::de).toList();
    }

    @Tool(description = "Lista las tareas pendientes, ordenadas por id.")
    public List<TareaDto> listarPendientes() {
        return repo.findByCompletadoOrderByIdAsc(false).stream().map(TareaDto::de).toList();
    }

    @Tool(description = "Lista las tareas completadas, ordenadas por id.")
    public List<TareaDto> listarCompletadas() {
        return repo.findByCompletadoOrderByIdAsc(true).stream().map(TareaDto::de).toList();
    }

    @Tool(description = "Cuenta todas las tareas almacenadas, pendientes y completadas.")
    public long contarTareas() { return repo.count(); }

    @Tool(description = "Busca tareas por una parte del título, sin distinguir mayúsculas; devuelve coincidencias ordenadas por id.")
    public List<TareaDto> buscarTareas(
            @ToolParam(description = "Texto no vacío de hasta 255 caracteres") String fragmento) {
        return repo.findByTituloContainingIgnoreCaseOrderByIdAsc(Validaciones.texto(fragmento, "Fragmento", 255))
                .stream().map(TareaDto::de).toList();
    }

    @Tool(description = "Crea una tarea pendiente y devuelve su id y datos.")
    @Transactional
    public TareaDto crearTarea(
            @ToolParam(description = "Título no vacío de hasta 255 caracteres") String titulo,
            @ToolParam(description = "Fecha real en formato ISO estricto yyyy-MM-dd, por ejemplo 2026-11-20") String fecha) {
        String nombre = Validaciones.texto(titulo, "Título", 255);
        LocalDate dia = Validaciones.fecha(fecha);
        return TareaDto.de(repo.save(new Tarea(nombre, dia)));
    }

    @Tool(description = "Marca una tarea como completada y devuelve sus datos; repetir no cambia el resultado.")
    @Transactional
    public TareaDto marcarCompletada(@ToolParam(description = "Id positivo de la tarea") Long id) {
        Tarea tarea = buscar(id);
        tarea.completar();
        return TareaDto.de(tarea);
    }

    @Tool(description = "Actualiza el título o fecha de una tarea y devuelve sus datos; los campos omitidos se conservan.")
    @Transactional
    public TareaDto actualizarTarea(
            @ToolParam(description = "Id positivo de la tarea") Long id,
            @ToolParam(description = "Nuevo título no vacío de hasta 255 caracteres; omitir o null para conservar", required = false) String titulo,
            @ToolParam(description = "Nueva fecha real ISO estricta yyyy-MM-dd; omitir o null para conservar", required = false) String fecha) {
        // Validar todo antes de modificar una entidad gestionada.
        String nuevoTitulo = titulo == null ? null : Validaciones.texto(titulo, "Título", 255);
        LocalDate nuevaFecha = fecha == null ? null : Validaciones.fecha(fecha);
        Tarea tarea = buscar(id);
        if (nuevoTitulo != null) tarea.setTitulo(nuevoTitulo);
        if (nuevaFecha != null) tarea.setFecha(nuevaFecha);
        return TareaDto.de(tarea);
    }

    @Tool(description = "Elimina permanentemente una tarea. Verifica antes el id con el usuario; devuelve si se eliminó o no existía.")
    @Transactional
    public String eliminarTarea(@ToolParam(description = "Id positivo de la tarea a eliminar") Long id) {
        Validaciones.id(id);
        return repo.findById(id).map(tarea -> {
            repo.delete(tarea);
            return "Tarea " + id + " eliminada correctamente.";
        }).orElse("La tarea " + id + " no existe.");
    }

    private Tarea buscar(Long id) {
        return repo.findById(Validaciones.id(id))
                .orElseThrow(() -> new IllegalArgumentException("No existe la tarea " + id));
    }
}
