package cl.duoc.cafeteria.reportes.repository;

import cl.duoc.cafeteria.reportes.model.VentaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaDiariaRepository extends JpaRepository<VentaDiaria, Long> {
}
