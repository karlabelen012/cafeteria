package cl.duoc.cafeteria.inventario.repository;

import cl.duoc.cafeteria.inventario.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {
}
