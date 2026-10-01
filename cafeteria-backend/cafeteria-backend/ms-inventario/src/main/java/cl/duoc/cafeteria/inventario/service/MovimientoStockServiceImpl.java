package cl.duoc.cafeteria.inventario.service;

import cl.duoc.cafeteria.inventario.dto.MovimientoStockRequest;
import cl.duoc.cafeteria.inventario.dto.MovimientoStockResponse;
import cl.duoc.cafeteria.inventario.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.inventario.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.inventario.model.Insumo;
import cl.duoc.cafeteria.inventario.model.MovimientoStock;
import cl.duoc.cafeteria.inventario.repository.InsumoRepository;
import cl.duoc.cafeteria.inventario.repository.MovimientoStockRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MovimientoStockServiceImpl implements MovimientoStockService {

    private static final String TIPO_ENTRADA = "ENTRADA";
    private static final String TIPO_SALIDA = "SALIDA";
    private static final String TIPO_AJUSTE = "AJUSTE";

    private final MovimientoStockRepository repository;
    private final InsumoRepository insumoRepository;

    public MovimientoStockServiceImpl(MovimientoStockRepository repository, InsumoRepository insumoRepository) {
        this.repository = repository;
        this.insumoRepository = insumoRepository;
    }

    @Override
    public List<MovimientoStockResponse> listar(Long insumoId) {
        if (!insumoRepository.existsById(insumoId)) {
            throw new RecursoNoEncontradoException("No existe el insumo con id " + insumoId);
        }
        return repository.findByInsumoIdOrderByFechaDesc(insumoId).stream().map(this::aResponse).toList();
    }

    @Override
    @Transactional
    public MovimientoStockResponse registrar(Long insumoId, MovimientoStockRequest request) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el insumo con id " + insumoId));

        aplicarMovimiento(insumo, request.tipo(), request.cantidad());
        insumoRepository.save(insumo);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setInsumoId(insumoId);
        movimiento.setTipo(request.tipo());
        movimiento.setCantidad(request.cantidad());
        movimiento.setMotivo(request.motivo());
        movimiento.setFecha(Instant.now());
        movimiento.setUsuario(usuarioActual());

        return aResponse(repository.save(movimiento));
    }

    // Aplica el movimiento sobre stockActual segun su tipo:
    // - ENTRADA: suma la cantidad.
    // - SALIDA: resta la cantidad; si el resultado seria negativo, se rechaza
    //   todo el movimiento (no se guarda ni el insumo ni el movimiento).
    // - AJUSTE: caso especial que NO suma ni resta: fija stockActual al valor
    //   exacto de "cantidad" (una correccion absoluta, p.ej. tras un conteo
    //   fisico de inventario).
    private void aplicarMovimiento(Insumo insumo, String tipo, Double cantidad) {
        switch (tipo) {
            case TIPO_ENTRADA -> insumo.setStockActual(insumo.getStockActual() + cantidad);
            case TIPO_SALIDA -> {
                double nuevoStock = insumo.getStockActual() - cantidad;
                if (nuevoStock < 0) {
                    throw new ConflictoDeNegocioException("Stock insuficiente para registrar la salida");
                }
                insumo.setStockActual(nuevoStock);
            }
            case TIPO_AJUSTE -> insumo.setStockActual(cantidad);
            default -> throw new ConflictoDeNegocioException("Tipo de movimiento invalido: " + tipo);
        }
    }

    private String usuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            return authentication.getName();
        }
        return "sistema";
    }

    private MovimientoStockResponse aResponse(MovimientoStock movimiento) {
        return new MovimientoStockResponse(
                movimiento.getId(),
                movimiento.getInsumoId(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getMotivo(),
                movimiento.getFecha(),
                movimiento.getUsuario());
    }
}
