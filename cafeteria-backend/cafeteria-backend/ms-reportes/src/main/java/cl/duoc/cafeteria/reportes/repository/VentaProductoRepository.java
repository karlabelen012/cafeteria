package cl.duoc.cafeteria.reportes.repository;

import cl.duoc.cafeteria.reportes.model.VentaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaProductoRepository extends JpaRepository<VentaProducto, Long> {

    Optional<VentaProducto> findByFechaAndProductoId(String fecha, Long productoId);

    List<VentaProducto> findByFechaBetween(String desde, String hasta);
}
