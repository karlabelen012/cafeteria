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

    @Override
    @Transactional
    public MovimientoStockResponse registrarSalidaForzada(Long insumoId, Double cantidad, String motivo) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el insumo con id " + insumoId));

        double nuevoStock = insumo.getStockActual() - cantidad;
        String tipoRegistrado;
        double cantidadRegistrada;
        if (nuevoStock < 0) {
            // Excepcion explicita a la regla general de "SALIDA nunca deja
            // negativo" (ver aplicarMovimiento): este metodo solo lo usa el
            // descuento automatico por pedido, que segun docs/EP2_PLAN.md
            // seccion 5 debe dejar el stock en 0 (no fallar) cuando no alcanza,
            // y auditar ese ajuste como AJUSTE (no SALIDA), ya que el valor
            // final no es "stock - cantidad" sino un piso forzado a 0.
            insumo.setStockActual(0.0);
            tipoRegistrado = TIPO_AJUSTE;
            cantidadRegistrada = 0.0;
        } else {
            insumo.setStockActual(nuevoStock);
            tipoRegistrado = TIPO_SALIDA;
            cantidadRegistrada = cantidad;
        }
        insumoRepository.save(insumo);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setInsumoId(insumoId);
        movimiento.setTipo(tipoRegistrado);
        movimiento.setCantidad(cantidadRegistrada);
        movimiento.setMotivo(motivo);
        movimiento.setFecha(Instant.now());
        movimiento.setUsuario(usuarioActual());

        return aResponse(repository.save(movimiento));
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
