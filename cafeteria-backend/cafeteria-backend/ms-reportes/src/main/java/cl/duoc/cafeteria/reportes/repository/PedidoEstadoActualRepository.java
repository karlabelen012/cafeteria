package cl.duoc.cafeteria.reportes.repository;

import cl.duoc.cafeteria.reportes.model.PedidoEstadoActual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoEstadoActualRepository extends JpaRepository<PedidoEstadoActual, Long> {

    List<PedidoEstadoActual> findByFechaBetween(String desde, String hasta);
}
