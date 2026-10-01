package cl.duoc.cafeteria.pedidos.repository;

import cl.duoc.cafeteria.pedidos.model.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventoProcesadoRepository extends JpaRepository<EventoProcesado, UUID> {
}
