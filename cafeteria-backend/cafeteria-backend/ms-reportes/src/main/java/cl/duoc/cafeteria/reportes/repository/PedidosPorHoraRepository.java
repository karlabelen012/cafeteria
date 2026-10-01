package cl.duoc.cafeteria.reportes.repository;

import cl.duoc.cafeteria.reportes.model.PedidosPorHora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidosPorHoraRepository extends JpaRepository<PedidosPorHora, Long> {

    Optional<PedidosPorHora> findByFechaAndHora(String fecha, Integer hora);

    List<PedidosPorHora> findByFecha(String fecha);

    List<PedidosPorHora> findByFechaBetween(String desde, String hasta);
}
