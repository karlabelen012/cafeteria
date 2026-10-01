package cl.duoc.cafeteria.notificaciones.repository;

import cl.duoc.cafeteria.notificaciones.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoSeguimiento(String codigoSeguimiento);
}
