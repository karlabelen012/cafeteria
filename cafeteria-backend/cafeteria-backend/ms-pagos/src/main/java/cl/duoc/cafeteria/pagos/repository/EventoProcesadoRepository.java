package cl.duoc.cafeteria.pagos.repository;

import cl.duoc.cafeteria.pagos.model.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, UUID> {
}
