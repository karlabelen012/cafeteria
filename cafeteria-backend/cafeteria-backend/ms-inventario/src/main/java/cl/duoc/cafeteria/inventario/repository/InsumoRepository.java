package cl.duoc.cafeteria.inventario.repository;

import cl.duoc.cafeteria.inventario.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    boolean existsByNombre(String nombre);

    // No se puede comparar dos columnas de la misma entidad con un metodo
    // derivado (findByStockActualLessThanEqual(stockMinimo) compararia contra
    // un valor fijo, no contra la otra columna), por eso se usa JPQL explicito.
    @Query("SELECT i FROM Insumo i WHERE i.stockActual <= i.stockMinimo")
    List<Insumo> findConStockBajo();
}
