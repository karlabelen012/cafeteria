package cl.duoc.cafeteria.reportes.repository;

import cl.duoc.cafeteria.reportes.model.VentaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaDiariaRepository extends JpaRepository<VentaDiaria, Long> {

    Optional<VentaDiaria> findByFecha(String fecha);

    List<VentaDiaria> findByFechaBetweenOrderByFecha(String desde, String hasta);
}
