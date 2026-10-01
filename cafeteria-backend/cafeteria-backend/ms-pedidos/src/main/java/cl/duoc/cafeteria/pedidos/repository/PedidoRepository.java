package cl.duoc.cafeteria.pedidos.repository;

import cl.duoc.cafeteria.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Usado por las rutas publicas (/api/public/**): nunca se busca por id
    // secuencial ahi, solo por este codigo (ver docs/EP2_PLAN.md seccion 2).
    Optional<Pedido> findByCodigoSeguimiento(String codigoSeguimiento);
}
