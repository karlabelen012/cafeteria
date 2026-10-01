package cl.duoc.cafeteria.notificaciones.repository;

import cl.duoc.cafeteria.notificaciones.model.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, UUID> {
}
