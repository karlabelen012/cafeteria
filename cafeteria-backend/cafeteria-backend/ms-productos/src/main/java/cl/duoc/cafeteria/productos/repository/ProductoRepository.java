package cl.duoc.cafeteria.productos.repository;

import cl.duoc.cafeteria.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByNombre(String nombre);

    Optional<Producto> findByNombre(String nombre);

    // Usados por el service para el GET publico con filtro opcional por
    // categoria y/o disponibilidad (ver docs/EP2_PLAN.md seccion 5).
    List<Producto> findByCategoria(String categoria);

    List<Producto> findByDisponible(Boolean disponible);

    List<Producto> findByCategoriaAndDisponible(String categoria, Boolean disponible);
}
