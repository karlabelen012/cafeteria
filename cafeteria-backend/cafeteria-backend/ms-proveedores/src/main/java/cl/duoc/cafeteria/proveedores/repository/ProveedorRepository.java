package cl.duoc.cafeteria.proveedores.repository;

import cl.duoc.cafeteria.proveedores.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByRut(String rut);

    Optional<Proveedor> findByRut(String rut);
}
