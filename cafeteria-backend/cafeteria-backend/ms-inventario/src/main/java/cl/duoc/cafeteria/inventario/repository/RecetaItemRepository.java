package cl.duoc.cafeteria.inventario.repository;

import cl.duoc.cafeteria.inventario.model.RecetaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaItemRepository extends JpaRepository<RecetaItem, Long> {

    List<RecetaItem> findByProductoId(Long productoId);
}
