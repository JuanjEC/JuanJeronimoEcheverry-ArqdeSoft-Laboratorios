package com.udea.mcp.repository;

import com.udea.mcp.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    java.util.List<Producto> findByNombreContainingIgnoreCaseOrderByIdAsc(String fragmento);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> bloquear(@Param("id") Long id);
}
