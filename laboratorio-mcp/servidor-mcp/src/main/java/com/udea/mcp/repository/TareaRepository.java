package com.udea.mcp.repository;

import com.udea.mcp.model.Tarea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByCompletadoOrderByIdAsc(boolean completado);
    List<Tarea> findByTituloContainingIgnoreCaseOrderByIdAsc(String fragmento);
}
