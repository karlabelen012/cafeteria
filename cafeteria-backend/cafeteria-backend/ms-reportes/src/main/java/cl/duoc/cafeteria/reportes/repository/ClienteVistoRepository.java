package cl.duoc.cafeteria.reportes.repository;

import cl.duoc.cafeteria.reportes.model.ClienteVisto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteVistoRepository extends JpaRepository<ClienteVisto, String> {

    long countByPrimeraFecha(String fecha);
}
