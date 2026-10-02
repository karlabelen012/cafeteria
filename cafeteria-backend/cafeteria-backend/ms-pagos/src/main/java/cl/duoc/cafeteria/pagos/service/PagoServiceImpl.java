package cl.duoc.cafeteria.pagos.service;

import cl.duoc.cafeteria.common.evento.PagoProcesadoEvent;
import cl.duoc.cafeteria.pagos.dto.PagoRequest;
import cl.duoc.cafeteria.pagos.dto.PagoResponse;
import cl.duoc.cafeteria.pagos.exception.ConflictoDeNegocioException;
import cl.duoc.cafeteria.pagos.exception.RecursoNoEncontradoException;
import cl.duoc.cafeteria.pagos.messaging.producer.PagoEventPublisher;
import cl.duoc.cafeteria.pagos.model.Pago;
import cl.duoc.cafeteria.pagos.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PagoServiceImpl implements PagoService {

    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_ANULADO = "ANULADO";

    private final PagoRepository repository;
    private final PagoEventPublisher eventPublisher;

    public PagoServiceImpl(PagoRepository repository, PagoEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<PagoResponse> listar() {
        return repository.findAll().stream().map(this::aResponse).toList();
    }

    @Override
    public PagoResponse obtener(Long id) {
        return aResponse(buscarOFallar(id));
    }

    // Registro de un pago en efectivo en el mostrador (ver docs/EP2_PLAN.md
    // seccion 5): siempre queda APROBADO (lo cobra el cajero en el momento) y
    // siempre publica pago.aprobado, igual que lo hace la simulacion de la
    // pasarela para los pagos con tarjeta.
    @Override
    public PagoResponse crear(PagoRequest request) {
        Pago pago = new Pago();
        aplicarDatos(pago, request);
        pago.setEstado(ESTADO_APROBADO);
        pago.setFecha(Instant.now());
        Pago guardado = repository.save(pago);
        publicarResultado(guardado, true);
        return aResponse(guardado);
    }

    @Override
    public PagoResponse actualizar(Long id, PagoRequest request) {
        Pago pago = buscarOFallar(id);
        aplicarDatos(pago, request);
        return aResponse(repository.save(pago));
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el pago con id " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public PagoResponse anular(Long id) {
        Pago pago = buscarOFallar(id);
        if (ESTADO_ANULADO.equals(pago.getEstado())) {
            throw new ConflictoDeNegocioException("El pago con id " + id + " ya esta anulado");
        }
        pago.setEstado(ESTADO_ANULADO);
        return aResponse(repository.save(pago));
    }

    private Pago buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pago con id " + id));
    }

    private void aplicarDatos(Pago pago, PagoRequest request) {
        pago.setPedidoId(request.pedidoId());
        pago.setMonto(request.monto());
        pago.setMetodoPago(request.metodoPago());
        pago.setUltimos4(request.ultimos4());
    }

    private void publicarResultado(Pago pago, boolean aprobado) {
        eventPublisher.publicar(new PagoProcesadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                1,
                pago.getPedidoId(),
                pago.getId(),
                aprobado,
                pago.getMonto(),
                pago.getMetodoPago()));
    }

    private PagoResponse aResponse(Pago pago) {
        return new PagoResponse(pago.getId(), pago.getPedidoId(), pago.getMonto(),
                pago.getMetodoPago(), pago.getEstado(), pago.getUltimos4(), pago.getFecha());
    }
}
